package com.kopo.hanagreenworld.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamJoinRequest {
    private String inviteCode;
}