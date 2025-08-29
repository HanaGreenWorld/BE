package com.kopo.hanagreenworld.member.domain;

import jakarta.persistence.*;

import com.kopo.hanagreenworld.common.domain.DateTimeEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_profiles")
@Getter
@NoArgsConstructor
public class MemberProfile extends DateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_level_id")
    private EcoLevel currentLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_level_id")
    private EcoLevel nextLevel;

    @Column(name = "total_points")
    private Long totalPoints = 0L;

    @Column(name = "current_points")
    private Long currentPoints = 0L;

    @Column(name = "total_carbon_saved")
    private Double totalCarbonSaved = 0.0;

    @Column(name = "total_activities_count")
    private Integer totalActivitiesCount = 0;

    @Column(name = "current_month_points")
    private Long currentMonthPoints = 0L;
    
    @Column(name = "current_month_carbon_saved")
    private Double currentMonthCarbonSaved = 0.0;
    
    @Column(name = "current_month_activities_count")
    private Integer currentMonthActivitiesCount = 0;

    @Column(name = "progress_to_next_level")
    private Double progressToNextLevel = 0.0;

    @Column(name = "points_to_next_level")
    private Long pointsToNextLevel = 0L;

    @Builder
    public MemberProfile(Member member, String nickname, EcoLevel currentLevel, EcoLevel nextLevel) {
        this.member = member;
        this.nickname = nickname;
        this.currentLevel = currentLevel;
        this.nextLevel = nextLevel;
    }

    public void updateEcoLevel(EcoLevel currentLevel, EcoLevel nextLevel) {
        this.currentLevel = currentLevel;
        this.nextLevel = nextLevel;
    }

    public void updatePoints(Long points) {
        this.currentPoints += points;
        this.totalPoints += points;
        this.currentMonthPoints += points;
    }

    public void updateCarbonSaved(Double carbonSaved) {
        this.totalCarbonSaved += carbonSaved;
        this.currentMonthCarbonSaved += carbonSaved;
    }

    public void incrementActivityCount() {
        this.totalActivitiesCount++;
        this.currentMonthActivitiesCount++;
    }

    public void updateProgressToNextLevel(Double progress, Long pointsToNext) {
        this.progressToNextLevel = progress;
        this.pointsToNextLevel = pointsToNext;
    }

    public void resetCurrentMonthData() {
        this.currentMonthPoints = 0L;
        this.currentMonthCarbonSaved = 0.0;
        this.currentMonthActivitiesCount = 0;
    }
}