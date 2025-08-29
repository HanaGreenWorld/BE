package com.kopo.hanagreenworld.point.domain;

public enum PointCategory {
    // 적립
    DAILY_QUIZ("일일 퀴즈", "/assets/hana3dIcon/hanaIcon3d_3_103.png"),
    WALKING("걷기", "/assets/hana3dIcon/hanaIcon3d_123.png"),
    ELECTRONIC_RECEIPT("전자확인증", "/assets/hana3dIcon/hanaIcon3d_4_13.png"),
    ECO_CHALLENGE("에코 챌린지", "/assets/hana3dIcon/hanaIcon3d_103.png"),
    ECO_MERCHANT("친환경 가맹점", "/assets/hana3dIcon/hanaIcon3d_85.png"),
    TEAM_CHALLENGE("팀 챌린지", "/assets/green_team.png"),

    // 사용
    HANA_MONEY_CONVERSION("하나머니 전환", "/assets/hanamoney_logo.png"),
    ENVIRONMENT_DONATION("환경 기부", "/assets/sprout.png");

    private final String displayName;
    private final String imageUrl;
    
    PointCategory(String displayName, String imageUrl) {
        this.displayName = displayName;
        this.imageUrl = imageUrl;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    public String getImageUrl() {
        return imageUrl;
    }
}