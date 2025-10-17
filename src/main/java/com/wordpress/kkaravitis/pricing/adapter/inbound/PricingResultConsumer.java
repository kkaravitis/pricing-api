/*
 * Copyright (c) 2025 Konstantinos Karavitis
 *
 * Licensed under the Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 * You may not use this file for commercial purposes.
 * See the LICENSE file in the project root or visit:
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

package com.wordpress.kkaravitis.pricing.adapter.inbound;

import com.wordpress.kkaravitis.pricing.adapter.outbound.TopicsData;
import com.wordpress.kkaravitis.pricing.domain.PriceAdvisorService;
import com.wordpress.kkaravitis.pricing.domain.PricingResult;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PricingResultConsumer {
    private final PriceAdvisorService priceAdvisorService;

    @KafkaListener(id = "pricing-results-consumer",
          topics = "${app.kafka.topics.price-result-topic}",
          containerFactory = "concurrentKafkaListenerContainerFactory"
    )
    public void consume(@Payload  PricingResultEvent incoming) {
        PricingResult enriched = PricingResult.builder()
              .productId(incoming.getProductId())
              .productName(incoming.getProductName())
              .price(incoming.getNewPrice().getAmount())
              .currency(incoming.getNewPrice().getCurrency())
              .timestamp(incoming.getTimestamp() != null ? LocalDateTime
                    .ofInstant(Instant.ofEpochMilli(incoming.getTimestamp()),
                          ZoneOffset.UTC) : LocalDateTime.now())
              .inventoryLevel(incoming.getInventoryLevel())
              .demandMetric(incoming.getCurrentDemand())
              .build();

        log.info("Storing pricing result for product [{}]", enriched.getProductId());

        priceAdvisorService.handlePricingResult(enriched);
    }


}
