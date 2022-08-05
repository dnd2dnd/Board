package com.dnd.board.service;

import com.dnd.board.SecurityUtil;
import com.dnd.board.entity.Authority;
import com.dnd.board.entity.User;
import com.dnd.board.http.request.UserRequest;
import com.dnd.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void signup(UserRequest userDto) {
        if (userRepository.findOneWithAuthoritiesByUsername(userDto.getUsername()).orElse(null) != null) {
            throw new RuntimeException("이미 가입되어 있는 유저입니다.");
        }
        if (checkNullUserRequest(userDto)){
            throw new IllegalArgumentException("잘못된 접근입니다. username, password, username을 확인해주세요.");
        }

        userNameCheck(userDto);
        nickNameCheck(userDto);
        passWordCheck(userDto);

        //빌더 패턴의 장점
        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();

        User user = User.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .nickname(userDto.getNickname())
                .authorities(Collections.singleton(authority))
                .activated(true)
                .build();

        userRepository.save(user);
    }

    private boolean checkNullUserRequest(UserRequest userRequest){
        return userRequest.getUsername() == null || userRequest.getPassword() == null || userRequest.getNickname() == null;
    }

    private void userNameCheck(UserRequest userRequest) {
        if (!Pattern.matches("^[A-Za-z0-9]{4,12}$", userRequest.getUsername())) {
            throw new IllegalArgumentException("잘못된 접근입니다. username을 확인해주세요.");
        }
    }

    private void nickNameCheck(UserRequest userRequest) {
        if (!Pattern.matches("^[A-Za-z0-9가-힣]{2,10}$", userRequest.getNickname())) {
            throw new IllegalArgumentException("잘못된 접근입니다. nickname을 확인해주세요.");
        }
    }

    private void passWordCheck(UserRequest userRequest) {
        if (!Pattern.matches("^.*(?=^.{8,15}$)(?=.*\\d)(?=.*[a-zA-Z])(?=.*[!@#$%^&+=]).*$", userRequest.getPassword())) {
            throw new IllegalArgumentException("잘못된 접근입니다. password을 확인해주세요.");
        }
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities(String username) {
        return userRepository.findOneWithAuthoritiesByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> getMyUserWithAuthorities() {
        return SecurityUtil.getCurrentUsername().flatMap(userRepository::findOneWithAuthoritiesByUsername);
    }
}