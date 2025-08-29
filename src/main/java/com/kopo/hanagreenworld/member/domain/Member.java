package com.kopo.hanagreenworld.member.domain;

import com.kopo.hanagreenworld.common.domain.DateTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Member extends DateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String login_id;

    @Column(nullable = false)
    private String password_hashed;

    @Column(nullable = false)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime last_login_at;

    @Column(nullable = false)
    private boolean is_active;

    @Builder
    public Member(String login_id, String password_hashed, String username) {
        this.login_id = login_id;
        this.password_hashed = password_hashed;
        this.username = username;
        this.is_active = true;
    }

    public void updatePassword(String password_hashed) {
        this.password_hashed = password_hashed;
    }

    public void joinTeam(Team team) {
        this.team = team;
    }

    public void leaveTeam() {
        this.team = null;
    }
}
