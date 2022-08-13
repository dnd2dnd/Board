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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.transaction.Transactional;

import java.awt.print.Pageable;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private static String uuidEx = "09e7bfd8-71ab-4eaa-b7ca-d7151f1552f3";

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
        // given
        BoardRequest boardRequest = BoardRequest.builder()
                .title(title)
                .contents(contents)
                .build();

        // when, then
        mockMvc.perform(post(Board_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(boardRequest)))
                .andDo(print())
                .andReturn();
    }

    @WithMockUser(username = "username", roles = "USER", password = "password")
    @Test
    public void 게시판_검색() throws Exception {
        // given
        // 많은 데이터라 db에 저장된 것 사용
        MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
        requestParam.add("page", String.valueOf(0));
        requestParam.add("size", String.valueOf(10));
        requestParam.add("searchOption", "title");
        requestParam.add("keyword", "abc");

        // when
        ResultActions resultActions = mockMvc.perform(get(Board_URL)
                        .params(requestParam));

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void 게시판_상세검색() throws Exception {
        //given
        // 저장되어있는 데이터 uuidEx 사용

        // when
        ResultActions resultActions = mockMvc.perform(get(Board_URL+"/{board_id}", uuidEx));
        Board board = boardRepository.findById(UUID.fromString(uuidEx)).orElseThrow(IllegalArgumentException::new);

        // then
        resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("title").value(board.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("contents").value(board.getContents()))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void 게시판_삭제() throws Exception {
        //given
        // 저장되어있는 데이터 uuidEx 사용

        // when
        ResultActions resultActions = mockMvc.perform(delete(Board_URL+"/{board_id}", uuidEx));

        // then
        resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("게시글이 삭제되었습니다."))
                .andExpect(status().isOk())
                .andDo(print());

    }

    @WithMockUser(username = "username", roles = "USER", password = "password")
    @Test
    public void 게시판_수정() throws Exception {
        // given
        // 저장되어있는 데이터 uuidEx 사용
        BoardRequest boardRequest = BoardRequest.builder()
                .title(title+"2")
                .contents(contents)
                .writer(username)
                .build();
        String changeBoard = mapper.writeValueAsString(boardRequest);

        // when
        ResultActions resultActions = mockMvc.perform(patch(Board_URL+"/update/{board_id}", uuidEx)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(changeBoard));

        // then
        resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("게시글이 수정되었습니다."))
                .andExpect(status().isOk())
                .andDo(print());
    }
}