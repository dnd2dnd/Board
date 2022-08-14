package com.dnd.board.service;

import com.dnd.board.SecurityUtil;
import com.dnd.board.config.TokenProvider;
import com.dnd.board.entity.Authority;
import com.dnd.board.entity.Board;
import com.dnd.board.entity.SearchOption;
import com.dnd.board.entity.User;
import com.dnd.board.http.request.BoardRequest;
import com.dnd.board.http.request.UserRequest;
import com.dnd.board.http.response.Board.BoardListResponse;
import com.dnd.board.http.response.Board.BoardResponse;
import com.dnd.board.repository.BoardRepository;
import com.dnd.board.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.transaction.Transactional;
import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
    BoardService boardService;

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    PasswordEncoder passwordEncoder;

    private static String username = "dnd2dnd3";
    private static String password = "dnddnd123@";
    private static String nickname = "웅이";

    private static String title = "title";
    private static String contents = "contents";

    private static User user;

    @AfterEach //  Test가 하나 끝날때 마다 repository를 비워줌
    public void afterEach() {
        boardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    public void signupUser(){
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


    public UUID getUUID(){
        User user = userRepository.findOneWithAuthoritiesByUsername(username).orElseThrow();
        return user.getUserId();
    }


    @WithMockUser(username = "dnd2dnd3", roles = "USER", password = "dnddnd123@")
    @Test
    public Board 게시판_저장_메소드() {
        // given
        BoardRequest board = BoardRequest.builder()
                .title(title)
                .contents(contents)
                .build();

        // when
        Board data = boardService.setBoard(board);

        // then
        assertEquals(title, data.getTitle());
        assertEquals(contents, data.getContents());
        assertEquals(getUUID(), data.getUserId().getUserId());

        return data;
    }

    @WithMockUser(username = "dnd2dnd3", roles = "USER", password = "dnddnd123@")
    @Test
    public void 게시판_가져오기_메소드() {
        // given
        Board data = 게시판_저장_메소드();

        // when
        BoardResponse boardResponse = boardService.getBoardResponse(data.getIdx());

        // then
        assertEquals(data.getTitle(), boardResponse.getTitle());
        assertEquals(data.getContents(), boardResponse.getContents());

    }

    @WithMockUser(username = "dnd2dnd3", roles = "USER", password = "dnddnd123@")
    @Test
    public void 게시판_삭제_메소드() {
        // given
        Board data = 게시판_저장_메소드();

        // when
        boardService.deleteBoard(data.getIdx());

        // then
        assertThrows(IllegalArgumentException.class, () -> boardService.getBoardResponse(data.getIdx()));
    }

    @WithMockUser(username = "dnd2dnd3", roles = "USER", password = "dnddnd123@")
    @Test
    public void 게시판_수정_메소드() {
        // given
        Board data = 게시판_저장_메소드();
        BoardRequest boardRequest = BoardRequest.builder()
                                                .title(title+"1")
                                                .contents(contents+"1")
                                                .build();
        // when
        boardService.updateBoard(data.getIdx(), boardRequest);
        BoardResponse boardResponse = boardService.getBoardResponse(data.getIdx());

        // then
        assertEquals(title+"1", boardResponse.getTitle());
        assertEquals(contents+"1", boardResponse.getContents());
    }

}