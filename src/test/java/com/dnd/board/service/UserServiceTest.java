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
import org.springframework.security.test.context.support.WithMockUser;

import javax.transaction.Transactional;


import java.util.Optional;

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

    private static String username = "dnd2dnd2";
    private static String password = "dnddnd123@";
    private static String nickname = "웅이";

    private UserRequest makeUserRequest(){
        return new UserRequest(username, password, nickname);
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
    public void 회원가입_실패_빈칸입력() throws Exception {
        // given
        UserRequest userRequest1 = new UserRequest(null, password, nickname);
        UserRequest userRequest2 = new UserRequest(username, null, nickname);
        UserRequest userRequest3 = new UserRequest(username, password, null);

        // when, given
        assertEquals(assertThrows(RuntimeException.class, () -> userService.signup(userRequest1)).getMessage(), "잘못된 접근입니다. username, password, username을 확인해주세요.");
        assertEquals(assertThrows(RuntimeException.class, () -> userService.signup(userRequest2)).getMessage(), "잘못된 접근입니다. username, password, username을 확인해주세요.");
        assertEquals(assertThrows(RuntimeException.class, () -> userService.signup(userRequest3)).getMessage(), "잘못된 접근입니다. username, password, username을 확인해주세요.");
    }

    @Test
    public void 회원가입_잘못된입력() throws Exception {
        // given
        UserRequest userRequest1 = new UserRequest("가나다", password, nickname);
        UserRequest userRequest2 = new UserRequest(username, "웅", nickname);
        UserRequest userRequest3 = new UserRequest(username, password, "웅");

        // when, given
        assertEquals(assertThrows(RuntimeException.class, () -> userService.signup(userRequest1)).getMessage(), "잘못된 접근입니다. username을 확인해주세요.");
        assertEquals(assertThrows(RuntimeException.class, () -> userService.signup(userRequest2)).getMessage(), "잘못된 접근입니다. password을 확인해주세요.");
        assertEquals(assertThrows(RuntimeException.class, () -> userService.signup(userRequest3)).getMessage(), "잘못된 접근입니다. nickname을 확인해주세요.");
    }

    @WithMockUser(username = "dnd2dnd2", roles = "USER", password = "dnddnd123@")
    @Test
    public void 회원확인() throws Exception {
        // given
        UserRequest userRequest = makeUserRequest();
        userService.signup(userRequest);

        // when
        Optional<User> user = userService.getMyUserWithAuthorities();

        // then
        assertEquals(user.get().getUsername(), userRequest.getUsername());
    }
}