package com.dnd.board.controller;

import com.dnd.board.config.TokenProvider;
import com.dnd.board.entity.Authority;
import com.dnd.board.entity.Board;
import com.dnd.board.entity.User;
import com.dnd.board.http.request.BoardRequest;
import com.dnd.board.http.request.UserRequest;
import com.dnd.board.repository.BoardRepository;
import com.dnd.board.repository.UserRepository;
import com.dnd.board.service.BoardService;
import com.dnd.board.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.transaction.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class BoardControllerTest {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    BoardService boardService;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private WebApplicationContext ctx;

    private static String Board_URL = "/api/board";
    private static String username = "username";
    private static String password = "password";
    private static String nickname = "nickname";
    private static String title = "title";
    private static String contents = "contents";


    @BeforeEach
    void setup() throws Exception {
        // mockMvc 설정
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();

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
    }

    @WithMockUser(username = "test", roles = "USER", password = "test")
    @Test
    public void 사용자_확인() throws Exception {
        mockMvc.perform(get("/api/user"))
                .andDo(print());
    }

    @WithMockUser(username = "username", roles = "USER", password = "password")
    private String getAccessToken() throws Exception {
        UserRequest userRequest = new UserRequest(username, password, nickname);
        String userData = mapper.writeValueAsString(userRequest);
        MvcResult result = mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userData))
                .andReturn();
        return result.getResponse().getHeader("Authorization");
    }

    @WithMockUser(username = "username", roles = "USER", password = "password")
    @Test
    public void 게시판_생성() throws Exception {
//        User user = userRepository.findOneWithAuthoritiesByUsername(username).orElseThrow();

//        // given
        String[] jwt = getAccessToken().split(" ");
//        System.out.println(jwt);
//        Authentication authentication = tokenProvider.getAuthentication(jwt[1]);
//        User a = userRepository.findById(UUID.fromString(authentication.getName())).orElseThrow();

//        System.out.println(user.getUserId());
//        System.out.println(authentication.getName());
//        System.out.println(a.getUsername());

        BoardRequest boardRequest = BoardRequest.builder()
                .title(title)
                .contents(contents)
                .build();

        MvcResult result = mockMvc.perform(post(Board_URL)
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(boardRequest)))
                .andDo(print())
                .andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }
}