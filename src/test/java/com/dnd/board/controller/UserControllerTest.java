package com.dnd.board.controller;

import com.dnd.board.entity.Authority;
import com.dnd.board.entity.User;
import com.dnd.board.http.request.UserRequest;
import com.dnd.board.repository.UserRepository;
import com.dnd.board.service.UserService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.assertj.core.api.Assert;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.spring.web.json.Json;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @MockBean
    PasswordEncoder passwordEncoder;

    private static String SIGN_UP_URL = "/api/signup";

    private static String username = "dnd2dnd2";
    private static String password = "dnddnd123@";
    private static String nickname = "웅이";

    @Autowired
    private WebApplicationContext ctx;

    @BeforeEach
    void setup() throws Exception {
        // mockMvc 설정
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @AfterEach //  Test가 하나 끝날때 마다 repository를 비워줌
    public void afterEach() {
        userRepository.deleteAll();
    }

    private MvcResult signUp(String userRequest) throws Exception {
        return mockMvc.perform(post(SIGN_UP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andReturn();
    }

    private UserRequest getUserRequest(String username, String password, String nickname){
        UserRequest userRequest = UserRequest.builder()
                .username(username)
                .password(password)
                .nickname(nickname)
                .build();
        return userRequest;
    }

    @Test
    public void 회원가입_성공() throws Exception {
        // given
        UserRequest userRequest = getUserRequest(username, password, nickname);
        String userData = mapper.writeValueAsString(userRequest);

        // when
        MvcResult result = signUp(userData);

        // then
        String response = result.getResponse().getContentAsString();
        assertThat("사용자 등록에 성공하였습니다.").isEqualTo(JsonPath.parse(response).read("$.message"));
    }

    @Test
    public void 회원가입_중복() throws Exception {
        // given
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .authorities(Collections.singleton(Authority.builder().authorityName("ROLE_USER").build()))
                .activated(true)
                .build();
        userRepository.save(user);
        String userData = mapper.writeValueAsString(getUserRequest(username, password, nickname));

        // when, then
        assertThatThrownBy(() -> mockMvc.perform(post(SIGN_UP_URL)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(userData))
                .andDo(print())
                .andExpect(status().isOk()))
                .hasCause(new RuntimeException("이미 가입되어 있는 유저입니다."));
    }

    @Test
    public void 회원가입_아이디_오류() throws Exception {
        // given
        String userData = mapper.writeValueAsString(getUserRequest("가나다", password, nickname));

        // when, then
        assertThatThrownBy(() -> mockMvc.perform(post(SIGN_UP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userData))
                .andDo(print())
                .andExpect(status().isOk()))
                .hasCause(new IllegalArgumentException("잘못된 접근입니다. username을 확인해주세요."));
    }

    @Test
    public void 회원가입_비밀번호_오류() throws Exception {
        // given
        String userData = mapper.writeValueAsString(getUserRequest(username, "가나다", nickname));

        // when, then
        assertThatThrownBy(() -> mockMvc.perform(post(SIGN_UP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userData))
                .andDo(print())
                .andExpect(status().isOk()))
                .hasCause(new IllegalArgumentException("잘못된 접근입니다. password을 확인해주세요."));
    }

    @Test
    public void 회원가입_닉네임_오류() throws Exception {
        // given
        String userData = mapper.writeValueAsString(getUserRequest(username, password, "@@"));

        // when, then
        assertThatThrownBy(() -> mockMvc.perform(post(SIGN_UP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userData))
                .andDo(print())
                .andExpect(status().isOk()))
                .hasCause(new IllegalArgumentException("잘못된 접근입니다. nickname을 확인해주세요."));
    }

    @WithMockUser(username = "dnd2dnd2", roles = "USER", password = "dnddnd123@")
    @Test
    public void 사용자용접근() throws Exception {
        // given
        String userData = mapper.writeValueAsString(getUserRequest(username, password, nickname));
        signUp(userData);

        // when, then
        mockMvc.perform(get("/api/user"))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.nickname").value(nickname))
                .andDo(print())
                .andReturn();
    }

    @WithMockUser(username = "dnd2dnd2", roles = "ADMIN", password = "dnddnd123@")
    @Test
    public void 관리자용접근() throws Exception {
        // given
        User user = User.builder()
                .username("admin")
                .password(passwordEncoder.encode(password))
                .nickname("admin")
                .authorities(Collections.singleton(Authority.builder().authorityName("ROLE_ADMIN").build()))
                .activated(true)
                .build();
        userRepository.save(user);
        String userData = mapper.writeValueAsString(getUserRequest(username, password, nickname));
        signUp(userData);


        // when, then
        mockMvc.perform(get("/api/user/"+username))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.nickname").value(nickname))
                .andDo(print())
                .andReturn();
    }
}