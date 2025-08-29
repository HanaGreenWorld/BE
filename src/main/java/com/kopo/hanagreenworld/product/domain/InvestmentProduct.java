package com.kopo.hanagreenworld.product.domain;

import java.math.BigDecimal;
import jakarta.persistence.*;

import com.kopo.hanagreenworld.common.domain.DateTimeEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "investment_products")
@Getter
@NoArgsConstructor
public class InvestmentProduct extends DateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "investment_product_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 투자 정보
    @Column(name = "investment_type", length = 50, nullable = false)
    private String investmentType; // 펀드, ETF, 채권 등

    @Column(name = "risk_level", length = 20)
    private String riskLevel; // 낮음, 보통, 높음

    @Column(name = "expected_return_rate", precision = 5, scale = 2)
    private BigDecimal expectedReturnRate;

    @Column(name = "min_investment_amount", nullable = false)
    private Long minInvestmentAmount;

    @Column(name = "management_fee", precision = 5, scale = 2)
    private BigDecimal managementFee;

    @Column(name = "eco_investment_focus", length = 200)
    private String ecoInvestmentFocus; // ESG, 친환경 에너지 등

    @Builder
    public InvestmentProduct(Product product, String investmentType, String riskLevel,
                            BigDecimal expectedReturnRate, Long minInvestmentAmount,
                            BigDecimal managementFee, String ecoInvestmentFocus) {
        this.product = product;
        this.investmentType = investmentType;
        this.riskLevel = riskLevel;
        this.expectedReturnRate = expectedReturnRate;
        this.minInvestmentAmount = minInvestmentAmount;
        this.managementFee = managementFee;
        this.ecoInvestmentFocus = ecoInvestmentFocus;
    }
}