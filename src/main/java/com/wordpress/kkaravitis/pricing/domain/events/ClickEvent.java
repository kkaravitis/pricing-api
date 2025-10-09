package com.wordpress.kkaravitis.pricing.domain.events;

public record ClickEvent(String productId, String productName, Long timestamp) {
}
