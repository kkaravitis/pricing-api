package com.wordpress.kkaravitis.pricing.domain.dto;


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wordpress.kkaravitis.pricing.domain.PricingResult;
import com.wordpress.kkaravitis.pricing.domain.ProductPriceResultWithChange;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


@Slf4j
class PricingResultTest {
private final static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void init(){
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void testPricingResult() throws JsonProcessingException {
        PricingResult original = PricingResult.builder()
              .productId("iphone-15-pro")
              .productName("iPhone 15 Pro")
              .price(new BigDecimal("1099.99"))
              .currency("EUR")
              .timestamp(LocalDateTime.of(2024, 6, 1, 12, 0))
              .demandMetric(0.85)
              .competitorPrice(1050.00)
              .inventoryLevel(23.0)
              .modelPrediction(1100.00)
              .build();

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(original);
        log.info(json);

        PricingResult pricingResult = objectMapper.readValue(json, PricingResult.class);

        assertEquals("iphone-15-pro", pricingResult.getProductId());

    }

    @Test
    void testProductPriceResult() throws JsonProcessingException {
        ProductPriceResultWithChange original = new ProductPriceResultWithChange();
        original.setProductId("demo");

        String json = objectMapper.writeValueAsString(original);
        log.info(json);

        ProductPriceResultWithChange productPriceResultWithChange = objectMapper.readValue(json, ProductPriceResultWithChange.class);

        assertEquals("demo", productPriceResultWithChange.getProductId());
    }

}