package com.kopo.hanagreenworld.member.controller;

import com.kopo.hanagreenworld.common.util.SecurityUtil;
import com.kopo.hanagreenworld.member.domain.Member;
import com.kopo.hanagreenworld.member.dto.AuthResponse;
import com.kopo.hanagreenworld.member.dto.LoginRequest;
import com.kopo.hanagreenworld.member.dto.SignupRequest;
import com.kopo.hanagreenworld.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "회원가입, 로그인, 토큰 갱신 API")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("회원가입 요청: {}", request.getLoginId());
        AuthResponse response = memberService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "회원 로그인을 처리합니다.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("로그인 요청: {}", request.getLoginId());
        AuthResponse response = memberService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String authorization) {
        String refreshToken = authorization.replace("Bearer ", "");
        log.info("토큰 갱신 요청");
        AuthResponse response = memberService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    @Operation(summary = "인증 테스트", description = "토큰이 유효한지 테스트합니다.")
    public ResponseEntity<String> testAuth() {
        return ResponseEntity.ok("인증이 성공했습니다!");
    }

    @GetMapping("/me")
    @Operation(summary = "현재 사용자 정보", description = "현재 로그인된 사용자의 정보를 반환합니다.")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        try {
            Member member = SecurityUtil.getCurrentMember();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("loginId", member.getLoginId());
            userInfo.put("email", member.getEmail());
            userInfo.put("name", member.getName());
            userInfo.put("role", member.getRole());
            userInfo.put("status", member.getStatus());
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
