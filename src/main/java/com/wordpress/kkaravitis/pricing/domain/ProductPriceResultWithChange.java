package com.wordpress.kkaravitis.pricing.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProductPriceResultWithChange {
    private Long id;
    private String productId;
    private String productName;
    private BigDecimal price;
    private String currency;
    private LocalDateTime timestamp;
    private Double demandMetric;
    private Double competitorPrice;
    private Double inventoryLevel;
    private Double modelPrediction;

    private BigDecimal previousPrice;
    private BigDecimal priceChangePercent;
    private String priceChangeLabel;
}
