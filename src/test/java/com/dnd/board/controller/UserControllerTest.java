package com.dnd.board.controller;

import com.dnd.board.entity.Authority;
import com.dnd.board.entity.User;
import com.dnd.board.http.request.UserRequest;
import com.dnd.board.repository.UserRepository;
import com.dnd.board.service.UserService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    private static String username = "d2d";
    private static String password = "d2d";
    private static String nickname = "웅둘웅둘";

    @Autowired
    private WebApplicationContext ctx;

    @BeforeEach
    void setup() throws Exception {
        // mockMvc 설정
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    private MvcResult signUp(String userRequest) throws Exception {
        return mockMvc.perform(post(SIGN_UP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isOk())
                .andReturn();
    }

    @AfterEach //  Test가 하나 끝날때 마다 repository를 비워줌
    public void afterEach() {
        userRepository.deleteAll();
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
        UserRequest userRequest = getUserRequest(username+"1", password, nickname);
        String userData = mapper.writeValueAsString(userRequest);

        MvcResult result = signUp(userData);

        String response = result.getResponse().getContentAsString();
        assertThat("사용자 등록에 성공하였습니다.").isEqualTo(JsonPath.parse(response).read("$.message"));
    }

    @Test
    public void 회원가입_중복() throws Exception {
        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .authorities(Collections.singleton(authority))
                .activated(true)
                .build();

        userRepository.save(user);

        UserRequest userRequest = getUserRequest(username, password, nickname);

        Assertions.assertThrows(RuntimeException.class, () -> {userService.signup(userRequest);});

    }

}