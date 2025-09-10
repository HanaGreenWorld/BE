package com.kopo.hanagreenworld.member.controller;

import com.kopo.hanagreenworld.member.dto.*;
import com.kopo.hanagreenworld.member.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "팀 API", description = "팀 관련 API (팀 정보, 랭킹, 가입/탈퇴)")
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/my-team")
    @Operation(summary = "내 팀 정보 조회", description = "현재 사용자가 속한 팀의 상세 정보를 조회합니다.")
    public ResponseEntity<TeamResponse> getMyTeam() {
        log.info("내 팀 정보 조회 요청");
        try {
            TeamResponse response = teamService.getMyTeam();
            log.info("내 팀 정보 조회 성공: 팀 ID = {}", response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("내 팀 정보 조회 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/ranking")
    @Operation(summary = "팀 랭킹 조회", description = "전체 팀 랭킹과 내 팀의 순위를 조회합니다.")
    public ResponseEntity<TeamRankingResponse> getTeamRanking() {
        log.info("팀 랭킹 조회 요청");
        try {
            TeamRankingResponse response = teamService.getTeamRanking();
            log.info("팀 랭킹 조회 성공: 내 팀 순위 = {}위", response.getMyTeamRank());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("팀 랭킹 조회 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/join")
    @Operation(summary = "팀 참여", description = "초대코드를 사용하여 팀에 참여합니다.")
    public ResponseEntity<TeamResponse> joinTeam(@Valid @RequestBody TeamJoinRequest request) {
        log.info("팀 참여 요청: 초대코드 = {}", request.getInviteCode());
        try {
            TeamResponse response = teamService.joinTeamByInviteCode(request.getInviteCode());
            log.info("팀 참여 성공: 팀 ID = {}, 팀명 = {}", response.getId(), response.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("팀 참여 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{teamId}/stats")
    @Operation(summary = "팀 통계 조회", description = "특정 팀의 상세 통계 정보를 조회합니다.")
    public ResponseEntity<TeamResponse.TeamStatsResponse> getTeamStats(@PathVariable Long teamId) {
        log.info("팀 통계 조회 요청: 팀 ID = {}", teamId);
        try {
            TeamResponse.TeamStatsResponse response = teamService.getTeamStats(teamId);
            log.info("팀 통계 조회 성공: 팀 ID = {}", teamId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("팀 통계 조회 실패: 팀 ID = {}, 에러 = {}", teamId, e.getMessage(), e);
            throw e;
        }
    }


    @DeleteMapping("/{teamId}/leave")
    @Operation(summary = "팀 탈퇴", description = "현재 팀에서 탈퇴합니다.")
    public ResponseEntity<Void> leaveTeam(@PathVariable Long teamId) {
        log.info("팀 탈퇴 요청: 팀 ID = {}", teamId);
        try {
            teamService.leaveTeam(teamId);
            log.info("팀 탈퇴 성공: 팀 ID = {}", teamId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("팀 탈퇴 실패: 팀 ID = {}, 에러 = {}", teamId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/{teamId}/invite-code")
    @Operation(summary = "팀 초대 코드 생성", description = "새로운 팀 초대 코드를 생성합니다.")
    public ResponseEntity<TeamInviteCodeResponse> generateInviteCode(@PathVariable Long teamId) {
        log.info("팀 초대 코드 생성 요청: 팀 ID = {}", teamId);
        try {
            // TODO: 실제 초대 코드 생성 로직 구현
            String inviteCode = "GG-" + teamId.toString().substring(0, 4).toUpperCase();
            TeamInviteCodeResponse response = new TeamInviteCodeResponse(inviteCode);
            log.info("팀 초대 코드 생성 성공: 팀 ID = {}, 초대 코드 = {}", teamId, inviteCode);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("팀 초대 코드 생성 실패: 팀 ID = {}, 에러 = {}", teamId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{teamId}/members")
    @Operation(summary = "팀 멤버 목록 조회", description = "특정 팀의 멤버 목록을 조회합니다.")
    public ResponseEntity<TeamMembersResponse> getTeamMembers(@PathVariable Long teamId) {
        log.info("팀 멤버 목록 조회 요청: 팀 ID = {}", teamId);
        try {
            // TODO: 실제 팀 멤버 조회 로직 구현
            TeamMembersResponse response = TeamMembersResponse.builder()
                    .teamId(teamId)
                    .members(java.util.Collections.emptyList())
                    .totalCount(0)
                    .build();
            log.info("팀 멤버 목록 조회 성공: 팀 ID = {}", teamId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("팀 멤버 목록 조회 실패: 팀 ID = {}, 에러 = {}", teamId, e.getMessage(), e);
            throw e;
        }
    }
}

