package com.dnd.board.service;

import com.dnd.board.entity.User;
import com.dnd.board.http.request.UserRequest;
import com.dnd.board.repository.UserRepository;
import org.assertj.core.api.Assert;
import org.assertj.core.internal.bytebuddy.matcher.ElementMatcher;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.transaction.Transactional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private String PASSWORD = "d2d";

    private UserRequest makeUserRequest(){
        return new UserRequest("d2d", PASSWORD, "dnd2dnd2");
    }

    @Test
    public void 회원가입_성공() throws Exception {
        // given
        UserRequest userRequest = makeUserRequest();

        // when
        userService.signup(userRequest);

        // then
        User user = userRepository.findOneWithAuthoritiesByUsername(userRequest.getUsername()).orElseThrow();
        assertThat(user.getUserId()).isNotNull();
        assertEquals(user.getUsername(), userRequest.getUsername());
        assertEquals(user.getNickname(), userRequest.getNickname());
    }

    @Test
    public void 회원가입_실패_아이디중복() throws Exception {
        // given
        UserRequest userRequest = makeUserRequest();
        userService.signup(userRequest);

        // when, then
        assertEquals(assertThrows(RuntimeException.class, () -> userService.signup(userRequest)).getMessage(), "이미 가입되어 있는 유저입니다.");
    }

    @Test
    public void 회원가입_실패_잘못된입력() throws Exception {
        // given
        UserRequest userRequest1 = new UserRequest(null, "1", "nickname1");
        UserRequest userRequest2 = new UserRequest("username2", null, "nickname2");
        UserRequest userRequest3 = new UserRequest("username3", "3", null);

        // when, given
        assertEquals(assertThrows(RuntimeException.class, () -> userService.signup(userRequest1)).getMessage(), "잘못된 접근입니다. email, password, username을 확인해주세요.");
        assertEquals(assertThrows(RuntimeException.class, () -> userService.signup(userRequest2)).getMessage(), "잘못된 접근입니다. email, password, username을 확인해주세요.");
        assertEquals(assertThrows(RuntimeException.class, () -> userService.signup(userRequest3)).getMessage(), "잘못된 접근입니다. email, password, username을 확인해주세요.");
    }
}