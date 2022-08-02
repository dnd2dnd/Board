package com.dnd.board.service;

import com.dnd.board.config.TokenProvider;
import com.dnd.board.entity.Authority;
import com.dnd.board.entity.Board;
import com.dnd.board.entity.User;
import com.dnd.board.http.request.UserRequest;
import com.dnd.board.repository.BoardRepository;
import com.dnd.board.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Transactional
@SpringBootTest
class BoardServiceTest {
    @Autowired
    BoardRepository boardRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenProvider tokenProvider;



    private static String username = "username";
    private static String password = "password";
    private static String nickname = "nickname";
    private static String title = "title";
    private static String contents = "contents";

    private static User user;
//    private String getAccessToken() throws Exception {
//        UserRequest userRequest = new UserRequest(username, password, nickname);
//        String userData = mapper.writeValueAsString(userRequest);
//        MvcResult result = mockMvc.perform(post("/api/authenticate")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(userData))
//                .andReturn();
//        return result.getResponse().getHeader("Authorization");
//    }

    @BeforeEach
    public void signupUser(){
        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();
        user = userRepository.save(User.builder()
                        .username(username)
                        .password(password)
                        .nickname(nickname)
                        .authorities(Collections.singleton(authority))
                        .activated(true).build());
    }

    @AfterEach
    public void cleanup() {
        userRepository.deleteAll();
    }


    public UUID getUUID(){
        User user = userRepository.findOneWithAuthoritiesByUsername(username).orElseThrow();
        return user.getUserId();
    }


    @Test
    public void 게시판_저장_메소드() {
        System.out.println(getUUID());
        Board board = Board.builder()
                .userId(new User(getUUID()))
                .title("title")
                .contents("contents")
                .build();
        Board data = boardRepository.save(board);
        Board date2 = boardRepository.findById(data.getIdx()).orElseThrow();
        System.out.println(date2.getTitle());
        System.out.println(date2.getContents());
        System.out.println(date2.getUserId());

    }
}