/*
 * Copyright (c) 2025 Konstantinos Karavitis
 *
 * Licensed under the Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 * You may not use this file for commercial purposes.
 * See the LICENSE file in the project root or visit:
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

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

      Double demandMetric;
      Double competitorPrice;
      Double inventoryLevel;
      Double modelPrediction;
}
