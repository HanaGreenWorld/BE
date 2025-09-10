package com.kopo.hanagreenworld.member.service;

import com.kopo.hanagreenworld.common.exception.BusinessException;
import com.kopo.hanagreenworld.common.exception.ErrorCode;
import com.kopo.hanagreenworld.common.util.SecurityUtil;
import com.kopo.hanagreenworld.member.domain.Member;
import com.kopo.hanagreenworld.member.domain.MemberTeam;
import com.kopo.hanagreenworld.member.domain.Team;
import com.kopo.hanagreenworld.member.dto.*;
import com.kopo.hanagreenworld.member.repository.MemberRepository;
import com.kopo.hanagreenworld.member.repository.MemberTeamRepository;
import com.kopo.hanagreenworld.member.repository.TeamRepository;
import com.kopo.hanagreenworld.point.domain.TeamPointTransaction;
import com.kopo.hanagreenworld.point.repository.PointTransactionRepository;
import com.kopo.hanagreenworld.activity.domain.Challenge;
import com.kopo.hanagreenworld.activity.repository.ChallengeRepository;
import com.kopo.hanagreenworld.activity.repository.ChallengeRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final MemberRepository memberRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeRecordRepository challengeRecordRepository;

    /**
     * 현재 사용자의 팀 정보 조회
     */
    public TeamResponse getMyTeam() {
        Member currentMember = SecurityUtil.getCurrentMember();
        if (currentMember == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        MemberTeam memberTeam = memberTeamRepository.findByMember_MemberIdAndIsActiveTrue(currentMember.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        Team team = memberTeam.getTeam();
        TeamResponse.TeamStatsResponse stats = getTeamStats(team.getId());
        List<TeamResponse.EmblemResponse> emblems = getTeamEmblems(team.getId());
        
        // 팀장 정보 조회
        Member leader = memberRepository.findById(team.getLeaderId())
                .orElse(null);
        
        // 현재 진행 중인 챌린지 조회 (가장 최근 활성 챌린지)
        Challenge currentChallenge = challengeRepository.findByIsActiveTrue().stream()
                .findFirst()
                .orElse(null);
        
        // 완료된 챌린지 수 계산
        Integer completedChallenges = challengeRecordRepository.countByMember_MemberIdAndVerificationStatus(
                currentMember.getMemberId(), "VERIFIED");

        return TeamResponse.from(team, stats, emblems, leader, currentChallenge, completedChallenges);
    }

    /**
     * 초대코드로 팀 참여
     */
    @Transactional
    public TeamResponse joinTeamByInviteCode(String inviteCode) {
        Member currentMember = SecurityUtil.getCurrentMember();
        if (currentMember == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 이미 팀에 속해있는지 확인
        Optional<MemberTeam> existingTeam = memberTeamRepository.findByMember_MemberIdAndIsActiveTrue(currentMember.getMemberId());
        if (existingTeam.isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_IN_TEAM);
        }

        // 초대코드로 팀 조회 (GG-0001 형식)
        if (!inviteCode.startsWith("GG-")) {
            throw new BusinessException(ErrorCode.INVALID_INVITE_CODE);
        }
        
        String teamIdStr = inviteCode.substring(3);
        Long teamId;
        try {
            teamId = Long.parseLong(teamIdStr);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_INVITE_CODE);
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        if (!team.getIsActive()) {
            throw new BusinessException(ErrorCode.TEAM_NOT_ACTIVE);
        }

        // 팀원 수 확인
        long currentMemberCount = memberTeamRepository.countByTeam_IdAndIsActiveTrue(teamId);
        if (team.getMaxMembers() != null && currentMemberCount >= team.getMaxMembers()) {
            throw new BusinessException(ErrorCode.TEAM_FULL);
        }

        // 팀 참여
        MemberTeam memberTeam = MemberTeam.builder()
                .member(currentMember)
                .team(team)
                .role(MemberTeam.TeamRole.MEMBER)
                .build();

        memberTeamRepository.save(memberTeam);

        // 팀 정보 반환
        TeamResponse.TeamStatsResponse stats = getTeamStats(team.getId());
        List<TeamResponse.EmblemResponse> emblems = getTeamEmblems(team.getId());
        Member leader = memberRepository.findById(team.getLeaderId()).orElse(null);
        Challenge currentChallenge = challengeRepository.findByIsActiveTrue().stream()
                .findFirst()
                .orElse(null);
        Integer completedChallenges = challengeRecordRepository.countByMember_MemberIdAndVerificationStatus(
                currentMember.getMemberId(), "VERIFIED");

        return TeamResponse.from(team, stats, emblems, leader, currentChallenge, completedChallenges);
    }

    /**
     * 팀 랭킹 조회
     */
    public TeamRankingResponse getTeamRanking() {
        Member currentMember = SecurityUtil.getCurrentMember();
        if (currentMember == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        // 상위 10개 팀 조회
        List<Team> topTeams = teamRepository.findTeamsByMonthlyRanking(currentMonth)
                .stream()
                .limit(10)
                .collect(Collectors.toList());

        List<TeamRankingResponse.TopTeamResponse> topTeamResponses = topTeams.stream()
                .map(this::convertToTopTeamResponse)
                .collect(Collectors.toList());

        // 내 팀 정보 조회
        MemberTeam myMemberTeam = memberTeamRepository.findByMember_MemberIdAndIsActiveTrue(currentMember.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        TeamRankingResponse.TeamRankingInfo myTeamInfo = getMyTeamRankingInfo(myMemberTeam.getTeam(), currentMonth);

        // 전체 팀 수 조회
        Integer totalTeams = teamRepository.findByIsActiveTrue().size();

        return TeamRankingResponse.create(topTeamResponses, myTeamInfo, totalTeams);
    }

    /**
     * 팀 가입 (초대 코드로)
     */
    @Transactional
    public TeamResponse joinTeam(TeamJoinRequest request) {
        Member currentMember = SecurityUtil.getCurrentMember();
        if (currentMember == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 이미 팀에 속해있는지 확인
        if (memberTeamRepository.findByMember_MemberIdAndIsActiveTrue(currentMember.getMemberId()).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_IN_TEAM);
        }

        // 초대 코드로 팀 조회 (실제로는 초대 코드 테이블이 있어야 함)
        Long teamId = parseInviteCode(request.getInviteCode());
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        // 팀 가입
        MemberTeam memberTeam = MemberTeam.builder()
                .member(currentMember)
                .team(team)
                .role(MemberTeam.TeamRole.MEMBER)
                .build();

        memberTeamRepository.save(memberTeam);

        // 팀 정보 반환
        TeamResponse.TeamStatsResponse stats = getTeamStats(team.getId());
        List<TeamResponse.EmblemResponse> emblems = getTeamEmblems(team.getId());

        // 팀장 정보 조회
        Member leader = memberRepository.findById(team.getLeaderId()).orElse(null);
        
        // 현재 진행 중인 챌린지 조회
        Challenge currentChallenge = challengeRepository.findByIsActiveTrue().stream()
                .findFirst()
                .orElse(null);
        
        // 완료된 챌린지 수 계산
        Integer completedChallenges = challengeRecordRepository.countByMember_MemberIdAndVerificationStatus(
                currentMember.getMemberId(), "VERIFIED");

        return TeamResponse.from(team, stats, emblems, leader, currentChallenge, completedChallenges);
    }

    /**
     * 팀 탈퇴
     */
    @Transactional
    public void leaveTeam(Long teamId) {
        Member currentMember = SecurityUtil.getCurrentMember();
        if (currentMember == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        MemberTeam memberTeam = memberTeamRepository.findByMember_MemberIdAndIsActiveTrue(currentMember.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        if (!memberTeam.getTeam().getId().equals(teamId)) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }

        // 팀장은 탈퇴할 수 없음
        if (memberTeam.isLeader()) {
            throw new BusinessException(ErrorCode.LEADER_CANNOT_LEAVE);
        }

        memberTeam.deactivate();
        memberTeamRepository.save(memberTeam);
    }

    /**
     * 팀 통계 조회
     */
    public TeamResponse.TeamStatsResponse getTeamStats(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        // 월간 점수 조회
        Long monthlyPoints = pointTransactionRepository.findMonthlyTeamPoints(teamId, currentMonth);
        
        // 총 점수 조회
        Long totalPoints = pointTransactionRepository.findTotalTeamPoints(teamId);
        
        // 월간 랭킹 조회
        Integer monthlyRank = teamRepository.findTeamRankByMonth(teamId, currentMonth);
        
        // 활성 멤버 수 조회
        Integer activeMembers = memberTeamRepository.countActiveMembersByTeamId(teamId);
        
        // 이번 달 완료된 챌린지 수 (TODO: 실제 챌린지 완료 수 조회)
        Integer completedChallengesThisMonth = 0;
        
        // 탄소 절감량 (TODO: 실제 탄소 절감량 계산)
        Long carbonSavedKg = totalPoints / 100L; // 임시 계산

        return TeamResponse.TeamStatsResponse.builder()
                .monthlyPoints(monthlyPoints != null ? monthlyPoints : 0L)
                .totalPoints(totalPoints != null ? totalPoints : 0L)
                .monthlyRank(monthlyRank != null ? monthlyRank : 999)
                .totalRank(monthlyRank) // TODO: 전체 랭킹 계산
                .carbonSavedKg(carbonSavedKg)
                .activeMembers(activeMembers)
                .completedChallengesThisMonth(completedChallengesThisMonth)
                .build();
    }

    /**
     * 팀 엠블럼 조회
     */
    private List<TeamResponse.EmblemResponse> getTeamEmblems(Long teamId) {
        // TODO: 실제 엠블럼 시스템 구현
        List<TeamResponse.EmblemResponse> emblems = new ArrayList<>();
        
        // 임시 엠블럼 데이터
        emblems.add(TeamResponse.EmblemResponse.builder()
                .id("emblem_1")
                .name("그린 스타터")
                .description("첫 번째 챌린지 완료")
                .iconUrl("/assets/emblems/green_starter.png")
                .isEarned(true)
                .earnedAt(java.time.LocalDateTime.now().minusDays(30))
                .build());
                
        emblems.add(TeamResponse.EmblemResponse.builder()
                .id("emblem_2")
                .name("걷기 마스터")
                .description("걷기 챌린지 10회 완료")
                .iconUrl("/assets/emblems/walking_master.png")
                .isEarned(true)
                .earnedAt(java.time.LocalDateTime.now().minusDays(15))
                .build());

        return emblems;
    }

    /**
     * 상위 팀 응답 변환
     */
    private TeamRankingResponse.TopTeamResponse convertToTopTeamResponse(Team team) {
        TeamResponse.TeamStatsResponse stats = getTeamStats(team.getId());
        
        return TeamRankingResponse.TopTeamResponse.builder()
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .slogan(team.getDescription())
                .rank(stats.getMonthlyRank())
                .totalPoints(stats.getTotalPoints())
                .members(stats.getActiveMembers())
                .leaderName("그린리더") // TODO: 실제 팀장 이름 조회
                .emblemUrl("/assets/emblems/default.png")
                .build();
    }

    /**
     * 내 팀 랭킹 정보 조회
     */
    private TeamRankingResponse.TeamRankingInfo getMyTeamRankingInfo(Team team, String currentMonth) {
        TeamResponse.TeamStatsResponse stats = getTeamStats(team.getId());
        
        // 이전 달 랭킹 조회 (TODO: 실제 이전 달 랭킹 조회)
        Integer previousRank = stats.getMonthlyRank() + 1;
        String trend = "same";
        Integer rankChange = 0;
        
        if (previousRank < stats.getMonthlyRank()) {
            trend = "up";
            rankChange = previousRank - stats.getMonthlyRank();
        } else if (previousRank > stats.getMonthlyRank()) {
            trend = "down";
            rankChange = previousRank - stats.getMonthlyRank();
        }

        return TeamRankingResponse.TeamRankingInfo.builder()
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .currentRank(stats.getMonthlyRank())
                .previousRank(previousRank)
                .monthlyPoints(stats.getMonthlyPoints())
                .totalPoints(stats.getTotalPoints())
                .members(stats.getActiveMembers())
                .trend(trend)
                .rankChange(rankChange)
                .build();
    }

    /**
     * 초대 코드 파싱 (임시 구현)
     */
    private Long parseInviteCode(String inviteCode) {
        // 실제로는 초대 코드 테이블에서 조회해야 함
        // 임시로 코드에서 팀 ID 추출
        try {
            String teamIdStr = inviteCode.substring(3); // "GG-" 제거
            return Long.parseLong(teamIdStr);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_INVITE_CODE);
        }
    }
}
