package com.wordpress.kkaravitis.pricing.domain.events;

import com.wordpress.kkaravitis.pricing.domain.Money;

public record PriceRule(Money minPrice, Money maxPrice) {
}
