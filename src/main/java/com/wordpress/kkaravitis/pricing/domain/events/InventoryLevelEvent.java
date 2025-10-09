package com.wordpress.kkaravitis.pricing.domain.events;

public record InventoryLevelEvent(String productId, String productName, Integer quantity) {
}
