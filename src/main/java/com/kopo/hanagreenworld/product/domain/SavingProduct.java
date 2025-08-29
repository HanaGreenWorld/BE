package com.kopo.hanagreenworld.product.domain;

import java.math.BigDecimal;
import jakarta.persistence.*;

import com.kopo.hanagreenworld.common.domain.DateTimeEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "savings_products")
@Getter
@NoArgsConstructor
public class SavingProduct extends DateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "savings_product_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 금리 정보
    @Column(name = "base_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal baseRate;

    @Column(name = "max_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal maxRate;

    @Column(name = "preferential_rate", precision = 5, scale = 2)
    private BigDecimal preferentialRate;

    // 가입 조건
    @Column(name = "min_amount", nullable = false)
    private Long minAmount;

    @Column(name = "max_amount", nullable = false)
    private Long maxAmount;

    @Column(name = "period_months", nullable = false)
    private Integer periodMonths;

    @Column(name = "deposit_type", length = 50, nullable = false)
    private String depositType; // 자유적립식, 정기적립식

    @Column(name = "interest_payment_type", length = 50, nullable = false)
    private String interestPaymentType; // 만기일시지급식, 월복리식

    // 환경 관련
    @Column(name = "donation_description", columnDefinition = "TEXT")
    private String donationDescription;

    @Column(name = "donation_organizations", columnDefinition = "TEXT")
    private String donationOrganizations; // JSON 형태로 저장

    @Column(name = "donation_options", columnDefinition = "TEXT")
    private String donationOptions; // JSON 형태로 저장

    @Builder
    public SavingProduct(Product product, BigDecimal baseRate, BigDecimal maxRate,
                         BigDecimal preferentialRate, Long minAmount, Long maxAmount,
                         Integer periodMonths, String depositType, String interestPaymentType,
                         String donationDescription, String donationOrganizations, String donationOptions) {
        this.product = product;
        this.baseRate = baseRate;
        this.maxRate = maxRate;
        this.preferentialRate = preferentialRate;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.periodMonths = periodMonths;
        this.depositType = depositType;
        this.interestPaymentType = interestPaymentType;
        this.donationDescription = donationDescription;
        this.donationOrganizations = donationOrganizations;
        this.donationOptions = donationOptions;
    }
}