package com.kopo.hanagreenworld.point.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.kopo.hanagreenworld.common.domain.DateTimeEntity;
import com.kopo.hanagreenworld.member.domain.Member;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_transactions")
@Getter
@NoArgsConstructor
public class PointTransaction extends DateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private PointCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "points_amount", nullable = false)
    private Integer pointsAmount;

    @Column(name = "balance_after")
    private Long balanceAfter;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    public enum TransactionType {
        EARN, USE
    }

    @Builder
    public PointTransaction(Member member, TransactionType transactionType, PointCategory category,
                            String description, Integer pointsAmount, Long balanceAfter,
                            LocalDateTime occurredAt) {
        this.member = member;
        this.transactionType = transactionType;
        this.category = category;
        this.description = description;
        this.pointsAmount = pointsAmount;
        this.balanceAfter = balanceAfter;
        this.occurredAt = occurredAt == null ? LocalDateTime.now() : occurredAt;
    }

    public void setBalanceAfter(Long balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}