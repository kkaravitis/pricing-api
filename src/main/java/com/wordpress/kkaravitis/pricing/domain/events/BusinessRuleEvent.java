package com.wordpress.kkaravitis.pricing.domain.events;

public record BusinessRuleEvent(String productId, String productName, PriceRule priceRule) {
}
