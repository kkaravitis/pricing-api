package com.wordpress.kkaravitis.pricing.domain;

import com.wordpress.kkaravitis.pricing.adapter.outbound.EventPublisher;
import com.wordpress.kkaravitis.pricing.adapter.outbound.PriceResultRepository;
import com.wordpress.kkaravitis.pricing.adapter.outbound.PricingWebSocketPublisher;
import com.wordpress.kkaravitis.pricing.adapter.outbound.ProductPriceRepository;
import com.wordpress.kkaravitis.pricing.domain.events.BusinessRuleEvent;
import com.wordpress.kkaravitis.pricing.domain.events.ClickEvent;
import com.wordpress.kkaravitis.pricing.domain.events.InventoryLevelEvent;
import com.wordpress.kkaravitis.pricing.domain.events.OrderEvent;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PriceAdvisorService {

    private final PriceResultRepository repository;
    private final ProductPriceRepository productPriceRepository;
    private final PricingWebSocketPublisher webSocketPublisher;
    private final EventPublisher eventPublisher;


    @Transactional
    public void handlePricingResult(PricingResult result) {
        repository.save(result);

        ProductPriceResultWithChange enriched =
              productPriceRepository.fetchLatestPriceResultWithChangeForProduct(
                    result.getProductId()
              );

        webSocketPublisher.publish(enriched);
    }

    @Transactional
    public List<ProductPriceResultWithChange> getLatestProductPriceResults() {
        return productPriceRepository.fetchLatestPriceResultsPerProduct();
    }

    public void sendClickEvent(ClickEvent clickEvent) {
        eventPublisher.send(new ClickEvent(clickEvent.productId(),
              clickEvent.productName(),
              Instant.now().toEpochMilli()));
    }

    public void sendOrderEvent(OrderEvent orderEvent) {
        eventPublisher.send(new OrderEvent(orderEvent.orderId(),
              orderEvent.productId(),
              orderEvent.productName(),
              orderEvent.quantity(),
              Instant.now().toEpochMilli()));
    }

    public void sendInventoryLevelEvent(InventoryLevelEvent inventoryLevelEvent) {
        eventPublisher.send(inventoryLevelEvent);
    }

    public void sendBusinessRuleEvent(BusinessRuleEvent businessRuleEvent) {
        eventPublisher.send(businessRuleEvent);
    }


}
