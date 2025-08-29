package com.kopo.hanagreenworld.common.util;

import com.kopo.hanagreenworld.member.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityUtil {

    public static Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Member) {
            return (Member) principal;
        } else {
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
        }
    }

    public static String getCurrentMemberId() {
        return getCurrentMember().getMemberId();
    }

    public static String getCurrentMemberEmail() {
        return getCurrentMember().getEmail();
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
