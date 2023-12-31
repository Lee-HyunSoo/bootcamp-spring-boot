package com.springboot.security.service.impl;

import com.springboot.security.config.security.JwtTokenProvider;
import com.springboot.security.config.security.common.CommonResponse;
import com.springboot.security.domain.dto.SignInResultDto;
import com.springboot.security.domain.dto.SignUpResultDto;
import com.springboot.security.domain.entity.User;
import com.springboot.security.domain.repository.UserRepository;
import com.springboot.security.service.SignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Slf4j
@Service
public class SignServiceImpl implements SignService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SignUpResultDto signUp(String id, String password, String name, String role) {
        log.info("[getSignUpResult] 회원가입 정보 전달");
        User user;
        if (role.equalsIgnoreCase("admin")) {
            user = User.builder()
                    .uid(id)
                    .name(name)
                    .password(passwordEncoder.encode(password))
                    .roles(Collections.singletonList("ROLE_ADMIN"))
                    .build();
        } else {
            user = User.builder()
                    .uid(id)
                    .name(name)
                    .password(passwordEncoder.encode(password))
                    .roles(Collections.singletonList("ROLE_USER"))
                    .build();
        }

        User savedUser = userRepository.save(user);
        SignUpResultDto signUpResultDto = new SignInResultDto();

        log.info("[getSignUpResult] userEntity 값이 들어왔는지 확인 후 결과 값 주입");
        if (!savedUser.getName().isEmpty()) {
            log.info("[getSignUpResult] 정상 처리 완료");
            setSuccessResult(signUpResultDto);
        } else {
            log.info("[getSignUpResult] 실패 처리 완료");
            setFailResult(signUpResultDto);
        }

        return signUpResultDto;
    }

    @Override
    public SignInResultDto signIn(String id, String password) throws RuntimeException {
        log.info("[getSignInResult] signDataHandler 로 회원정보 요청");
        User user = userRepository.getByUid(id);
        log.info("[getSignInResult] Id : {}", id);

        log.info("[getSignInResult] 패스워드 비교 수행");
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException();
        }
        log.info("[getSignInResult] 패스워드 일치");

        log.info("[getSignInResult] SignInResultDto 객체 생성");
        SignInResultDto signInResultDto = SignInResultDto.builder()
                .token(
                        jwtTokenProvider.createToken(String.valueOf(user.getUid()), user.getRoles())
                )
                .build();
        log.info("[getSignInResult] SignInResultDto 객체에 값 주입");
        setSuccessResult(signInResultDto);

        return signInResultDto;
    }

    private void setSuccessResult(SignUpResultDto signUpResultDto) {
        signUpResultDto.setSuccess(true);
        signUpResultDto.setCode(CommonResponse.SUCCESS.getCode());
        signUpResultDto.setMsg(CommonResponse.SUCCESS.getMsg());
    }

    private void setFailResult(SignUpResultDto signUpResultDto) {
        signUpResultDto.setSuccess(false);
        signUpResultDto.setCode(CommonResponse.FAIL.getCode());
        signUpResultDto.setMsg(CommonResponse.FAIL.getMsg());
    }
}
