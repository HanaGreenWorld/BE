package com.kopo.hanagreenworld.member.domain;

import jakarta.persistence.*;

import com.kopo.hanagreenworld.common.domain.DateTimeEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "eco_levels")
@Getter
@NoArgsConstructor
public class EcoLevel extends DateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "level_id")
    private Long id;

    @Column(name = "level_code", length = 20, nullable = false, unique = true)
    private String levelCode; // BEGINNER, INTERMEDIATE, EXPERT

    @Column(name = "name", length = 100, nullable = false)
    private String name; // 친환경 새내기, 친환경 실천가, 친환경 달인

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "required_points", nullable = false)
    private Long requiredPoints;

    // 이미지 URL로 변경
    @Column(name = "image_url", length = 500)
    private String imageUrl; // /assets/beginner.png, /assets/intermediate.png, /assets/expert.png

    @Column(name = "icon", length = 10)
    private String icon; // ��, 🌿, �� (보조용)

    @Column(name = "color_code", length = 20)
    private String colorCode; // #10B981, #059669, #4CAF50

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder
    public EcoLevel(String levelCode, String name, String description,
                   Long requiredPoints, String imageUrl, String icon, 
                   String colorCode, Boolean isActive) {
        this.levelCode = levelCode;
        this.name = name;
        this.description = description;
        this.requiredPoints = requiredPoints;
        this.imageUrl = imageUrl;
        this.icon = icon;
        this.colorCode = colorCode;
        this.isActive = isActive == null ? true : isActive;
    }

    public void deactivate() { this.isActive = false; }
}