package com.kopo.hanagreenworld.point.controller;

import com.kopo.hanagreenworld.point.domain.PointCategory;
import com.kopo.hanagreenworld.point.dto.EcoSeedConvertRequest;
import com.kopo.hanagreenworld.point.dto.EcoSeedEarnRequest;
import com.kopo.hanagreenworld.point.dto.EcoSeedResponse;
import com.kopo.hanagreenworld.point.dto.EcoSeedTransactionResponse;
import com.kopo.hanagreenworld.point.service.EcoSeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/eco-seeds")
@RequiredArgsConstructor
@Tag(name = "원큐씨앗 API", description = "원큐씨앗 적립, 사용, 전환 API")
public class EcoSeedController {

    private final EcoSeedService ecoSeedService;

    @GetMapping
    @Operation(summary = "원큐씨앗 정보 조회", description = "현재 사용자의 원큐씨앗 잔액 및 정보를 조회합니다.")
    public ResponseEntity<EcoSeedResponse> getEcoSeedInfo() {
        log.info("원큐씨앗 정보 조회 요청");
        EcoSeedResponse response = ecoSeedService.getEcoSeedInfo();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    @Operation(summary = "회원 프로필 정보 조회", description = "현재 사용자의 member_profile 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getMemberProfile() {
        log.info("회원 프로필 정보 조회 요청");
        Map<String, Object> profile = ecoSeedService.getMemberProfile();
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/earn")
    @Operation(summary = "원큐씨앗 적립", description = "원큐씨앗을 적립합니다.")
    public ResponseEntity<EcoSeedResponse> earnEcoSeeds(@Valid @RequestBody EcoSeedEarnRequest request) {
        log.info("원큐씨앗 적립 요청: {} - {}개", request.getCategory(), request.getPointsAmount());
        EcoSeedResponse response = ecoSeedService.earnEcoSeeds(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/convert")
    @Operation(summary = "하나머니 전환", description = "원큐씨앗을 하나머니로 전환합니다.")
    public ResponseEntity<EcoSeedResponse> convertToHanaMoney(@Valid @RequestBody EcoSeedConvertRequest request) {
        log.info("하나머니 전환 요청: {}개", request.getPointsAmount());
        EcoSeedResponse response = ecoSeedService.convertToHanaMoney(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions")
    @Operation(summary = "거래 내역 조회", description = "원큐씨앗 거래 내역을 조회합니다.")
    public ResponseEntity<Map<String, Object>> getTransactionHistory(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("거래 내역 조회 요청");
        Page<EcoSeedTransactionResponse> page = ecoSeedService.getTransactionHistory(pageable);
        
        // Page 객체를 안전한 DTO로 변환
        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("currentPage", page.getNumber());
        response.put("size", page.getSize());
        response.put("first", page.isFirst());
        response.put("last", page.isLast());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/category/{category}")
    @Operation(summary = "카테고리별 거래 내역 조회", description = "특정 카테고리의 원큐씨앗 거래 내역을 조회합니다.")
    public ResponseEntity<List<EcoSeedTransactionResponse>> getTransactionHistoryByCategory(
            @PathVariable PointCategory category) {
        log.info("카테고리별 거래 내역 조회 요청: {}", category);
        List<EcoSeedTransactionResponse> response = ecoSeedService.getTransactionHistoryByCategory(category);
        return ResponseEntity.ok(response);
    }

    // 편의를 위한 API들
    @PostMapping("/earn/walking")
    @Operation(summary = "걷기로 원큐씨앗 적립", description = "걷기 활동으로 원큐씨앗을 적립합니다.")
    public ResponseEntity<EcoSeedResponse> earnFromWalking(@RequestParam Integer steps) {
        log.info("걷기로 원큐씨앗 적립 요청: {}걸음", steps);
        
        // 걸음 수에 따른 원큐씨앗 계산 (1000걸음 = 1원큐씨앗)
        int points = steps / 1000;
        if (points == 0 && steps > 0) points = 1; // 최소 1개
        
        EcoSeedEarnRequest request = new EcoSeedEarnRequest();
        request.setCategory(PointCategory.WALKING);
        request.setPointsAmount(points);
        request.setDescription(steps + "걸음으로 원큐씨앗 적립");
        
        EcoSeedResponse response = ecoSeedService.earnEcoSeeds(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/earn/quiz")
    @Operation(summary = "퀴즈로 원큐씨앗 적립", description = "퀴즈 완료로 원큐씨앗을 적립합니다.")
    public ResponseEntity<EcoSeedResponse> earnFromQuiz(@RequestParam String quizType) {
        log.info("퀴즈로 원큐씨앗 적립 요청: {}", quizType);
        
        EcoSeedEarnRequest request = new EcoSeedEarnRequest();
        request.setCategory(PointCategory.DAILY_QUIZ);
        request.setPointsAmount(5); // 퀴즈당 5개
        request.setDescription(quizType + " 퀴즈 완료로 원큐씨앗 적립");
        
        EcoSeedResponse response = ecoSeedService.earnEcoSeeds(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/earn/challenge")
    @Operation(summary = "챌린지로 원큐씨앗 적립", description = "챌린지 완료로 원큐씨앗을 적립합니다.")
    public ResponseEntity<EcoSeedResponse> earnFromChallenge(@RequestParam String challengeName) {
        log.info("챌린지로 원큐씨앗 적립 요청: {}", challengeName);
        
        EcoSeedEarnRequest request = new EcoSeedEarnRequest();
        request.setCategory(PointCategory.ECO_CHALLENGE);
        request.setPointsAmount(10); // 챌린지당 10개
        request.setDescription(challengeName + " 챌린지 완료로 원큐씨앗 적립");
        
        EcoSeedResponse response = ecoSeedService.earnEcoSeeds(request);
        return ResponseEntity.ok(response);
    }
}
