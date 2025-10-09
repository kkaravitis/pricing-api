package com.wordpress.kkaravitis.pricing.adapter.inbound;

import com.wordpress.kkaravitis.pricing.adapter.outbound.TopicsData;
import com.wordpress.kkaravitis.pricing.domain.PriceAdvisorService;
import com.wordpress.kkaravitis.pricing.domain.PricingResult;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PricingResultConsumer {
    private final PriceAdvisorService priceAdvisorService;

    @KafkaListener(topics = "${app.kafka.topics.price-result-topic}", groupId = "pricing-api-group")
    public void consume(ConsumerRecord<String, PricingResult> pricingResultRecord) {
        PricingResult incoming = pricingResultRecord.value();

        PricingResult enriched = PricingResult.builder()
              .productId(incoming.getProductId())
              .productName(incoming.getProductName())
              .price(incoming.getPrice())
              .currency(incoming.getCurrency())
              .timestamp(incoming.getTimestamp() != null ? incoming.getTimestamp() : LocalDateTime.now())
              .demandMetric(incoming.getDemandMetric())
              .competitorPrice(incoming.getCompetitorPrice())
              .inventoryLevel(incoming.getInventoryLevel())
              .modelPrediction(incoming.getModelPrediction())
              .build();

        log.info("Storing pricing result for product [{}]", enriched.getProductId());

        priceAdvisorService.handlePricingResult(enriched);
    }
}
