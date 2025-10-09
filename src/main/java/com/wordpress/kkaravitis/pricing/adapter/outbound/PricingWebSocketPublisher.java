/*
 * Copyright (c) 2025 Konstantinos Karavitis
 *
 * Licensed under the Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 * You may not use this file for commercial purposes.
 * See the LICENSE file in the project root or visit:
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

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
