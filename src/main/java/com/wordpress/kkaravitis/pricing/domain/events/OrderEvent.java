package com.wordpress.kkaravitis.pricing.domain.events;

public record OrderEvent(String orderId, String productId, String productName, Integer quantity, Long timestamp) {
}
