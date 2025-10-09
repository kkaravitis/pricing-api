package com.wordpress.kkaravitis.pricing.adapter.outbound;

import com.wordpress.kkaravitis.pricing.domain.ProductPriceResultWithChange;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PricingWebSocketPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public void publish(ProductPriceResultWithChange result) {
        String destination = "/stream/prices/";
        messagingTemplate.convertAndSend(destination, result);
    }

}
