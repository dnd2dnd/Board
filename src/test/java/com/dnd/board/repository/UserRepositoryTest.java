package com.dnd.board.repository;

import com.dnd.board.entity.Authority;
import com.dnd.board.entity.User;
import com.dnd.board.http.request.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;

    private String PASSWORD = "d2d";

    private UserRequest makeUserRequest(){
        return new UserRequest("userdnd", PASSWORD, "dnd2dnd2");
    }

    private User getUser(UserRequest userRequest){
        return User.builder()
                .username(userRequest.getUsername())
                .password(userRequest.getPassword())
                .nickname(userRequest.getNickname())
                .authorities(Collections.singleton(Authority.builder()
                                        .authorityName("ROLE_USER")
                                        .build()))
                .activated(true)
                .build();
    }

    @Test
    public void User_저장() {
        // given
        UserRequest userRequest = makeUserRequest();

        // when
        User user = userRepository.save(getUser(userRequest));

        // then
        assertEquals(userRequest.getUsername(), user.getUsername());
        assertEquals(userRequest.getNickname(), user.getNickname());

    }

    @Test
    public void 아이디로_user_가져오기() {
        // given
        UserRequest userRequest = makeUserRequest();
        System.out.println(userRequest.getUsername());
        userRepository.save(getUser(userRequest));

        // when
        User user = userRepository.findOneWithAuthoritiesByUsername(userRequest.getUsername()).orElseThrow();
        System.out.println(user.getNickname());
        // then
        assertEquals(user.getUsername(), userRequest.getUsername());
    }
}