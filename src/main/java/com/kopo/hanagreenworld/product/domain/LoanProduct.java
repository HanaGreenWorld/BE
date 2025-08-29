package com.kopo.hanagreenworld.product.domain;

import java.math.BigDecimal;
import jakarta.persistence.*;

import com.kopo.hanagreenworld.common.domain.DateTimeEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan_products")
@Getter
@NoArgsConstructor
public class LoanProduct extends DateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_product_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 대출 정보
    @Column(name = "loan_type", length = 50, nullable = false)
    private String loanType; // 전기차대출, 그린모빌리티대출 등

    @Column(name = "base_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal baseRate;

    @Column(name = "preferential_rate", precision = 5, scale = 2)
    private BigDecimal preferentialRate;

    @Column(name = "min_amount", nullable = false)
    private Long minAmount;

    @Column(name = "max_amount", nullable = false)
    private Long maxAmount;

    @Column(name = "max_period_months", nullable = false)
    private Integer maxPeriodMonths;

    @Column(name = "vehicle_type", length = 100)
    private String vehicleType; // 전기차 종류

    @Column(name = "repayment_type", length = 50)
    private String repaymentType; // 원리금균등상환, 원금균등상환 등

    @Builder
    public LoanProduct(Product product, String loanType, BigDecimal baseRate,
                      BigDecimal preferentialRate, Long minAmount, Long maxAmount,
                      Integer maxPeriodMonths, String vehicleType, String repaymentType) {
        this.product = product;
        this.loanType = loanType;
        this.baseRate = baseRate;
        this.preferentialRate = preferentialRate;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.maxPeriodMonths = maxPeriodMonths;
        this.vehicleType = vehicleType;
        this.repaymentType = repaymentType;
    }
}