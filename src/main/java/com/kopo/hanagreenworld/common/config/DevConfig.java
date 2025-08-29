package com.kopo.hanagreenworld.common.config;

import com.kopo.hanagreenworld.member.domain.Member;
import com.kopo.hanagreenworld.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("dev") // 개발 환경에서만 실행
public class DevConfig {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDevData() {
        return args -> {
            // 개발용 테스트 계정이 없으면 생성
            if (!memberRepository.existsByMemberId("testuser")) {
                Member testMember = Member.builder()
                        .memberId("testuser")
                        .email("test@hana.com")
                        .password("test1234!")
                        .name("테스트 사용자")
                        .phoneNumber("010-1234-5678")
                        .role(Member.MemberRole.USER)
                        .status(Member.MemberStatus.ACTIVE)
                        .build();

                testMember.encodePassword(passwordEncoder);
                memberRepository.save(testMember);
                log.info("개발용 테스트 계정이 생성되었습니다: testuser / test1234!");
            }

            // 관리자 계정도 생성
            if (!memberRepository.existsByMemberId("admin")) {
                Member adminMember = Member.builder()
                        .memberId("admin")
                        .email("admin@hana.com")
                        .password("admin1234!")
                        .name("관리자")
                        .phoneNumber("010-9999-9999")
                        .role(Member.MemberRole.ADMIN)
                        .status(Member.MemberStatus.ACTIVE)
                        .build();

                adminMember.encodePassword(passwordEncoder);
                memberRepository.save(adminMember);
                log.info("개발용 관리자 계정이 생성되었습니다: admin / admin1234!");
            }
        };
    }
}
