package com.wordpress.kkaravitis.pricing.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@Value
public class PricingResult {
      String productId;
      String productName;
      BigDecimal price;
      String currency;
      LocalDateTime timestamp;

      // Optional enrichments
      Double demandMetric;
      Double competitorPrice;
      Double inventoryLevel;
      Double modelPrediction;
}
