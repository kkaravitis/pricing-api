package com.wordpress.kkaravitis.pricing.domain.competitor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CompetitorPriceDto(
      String productId,
      String productName,
      BigDecimal competitorPrice,
      OffsetDateTime updatedAt
) {}
