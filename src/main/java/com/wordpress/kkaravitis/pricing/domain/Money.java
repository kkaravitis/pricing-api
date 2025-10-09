package com.wordpress.kkaravitis.pricing.domain;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class Money {
    private final BigDecimal amount;
    private final String currency;
}
