package com.kopo.hanagreenworld.member.service;

import com.kopo.hanagreenworld.common.util.JwtUtil;
import com.kopo.hanagreenworld.member.domain.Member;
import com.kopo.hanagreenworld.member.dto.AuthResponse;
import com.kopo.hanagreenworld.member.dto.LoginRequest;
import com.kopo.hanagreenworld.member.dto.SignupRequest;
import com.kopo.hanagreenworld.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse signup(SignupRequest request) {
        // 중복 검사
        if (memberRepository.existsByMemberId(request.getMemberId())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 회원 생성
        Member member = Member.builder()
                .memberId(request.getMemberId())
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .role(Member.MemberRole.USER)
                .status(Member.MemberStatus.ACTIVE)
                .build();

        // 비밀번호 암호화
        member.encodePassword(passwordEncoder);

        // 저장
        Member savedMember = memberRepository.save(member);

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(savedMember.getMemberId(), savedMember.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(savedMember.getMemberId(), savedMember.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .memberId(savedMember.getMemberId())
                .email(savedMember.getEmail())
                .name(savedMember.getName())
                .message("회원가입이 완료되었습니다.")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // 회원 조회
        Member member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다."));

        // 비밀번호 검증
        if (!member.checkPassword(request.getPassword(), passwordEncoder)) {
            throw new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 계정 상태 확인
        if (member.getStatus() != Member.MemberStatus.ACTIVE) {
            throw new RuntimeException("비활성화된 계정입니다.");
        }

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(member.getMemberId(), member.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(member.getMemberId(), member.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .name(member.getName())
                .message("로그인이 완료되었습니다.")
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        // 토큰 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new RuntimeException("만료된 토큰입니다.");
        }

        // 회원 정보 조회
        String memberId = jwtUtil.getMemberIdFromToken(refreshToken);
        String email = jwtUtil.getEmailFromToken(refreshToken);

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));

        // 새로운 토큰 생성
        String newAccessToken = jwtUtil.generateAccessToken(member.getMemberId(), member.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(member.getMemberId(), member.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .name(member.getName())
                .message("토큰이 갱신되었습니다.")
                .build();
    }
}
