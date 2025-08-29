package com.kopo.hanagreenworld.notification.domain;

import jakarta.persistence.*;

import com.kopo.hanagreenworld.common.domain.DateTimeEntity;
import com.kopo.hanagreenworld.member.domain.Member;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor
public class Notification extends DateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", length = 50, nullable = false)
    private NotificationType type;

    public enum NotificationType {
        CHALLENGE_COMPLETED("챌린지 완료"),
        QUIZ_AVAILABLE("퀴즈 알림"),
        WALKING_GOAL("걷기 목표 달성"),
        TEAM_INVITE("팀 초대"),
        BENEFIT_CHANGE("혜택 변경"),
        INSURANCE_EXPIRY("보험 만료"),
        ECO_REPORT("친환경 리포트");

        private final String displayName;

        NotificationType(String displayName) {
            this.displayName = displayName;
        }
    }

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType; // CHALLENGE, QUIZ, WALKING, etc.

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "image_url", length = 500)
    private String imageUrl; // 알림에 표시될 이미지

    @Builder
    public Notification(Member member, String title, String message, 
                       NotificationType type, String relatedEntityType, 
                       Long relatedEntityId, String imageUrl) {
        this.member = member;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.imageUrl = imageUrl;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}