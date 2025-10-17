/*
 * Copyright (c) 2025 Konstantinos Karavitis
 *
 * Licensed under the Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 * You may not use this file for commercial purposes.
 * See the LICENSE file in the project root or visit:
 * https://creativecommons.org/licenses/by-nc/4.0/
 */
package com.wordpress.kkaravitis.pricing.adapter.inbound;

import com.wordpress.kkaravitis.pricing.domain.Money;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PricingResultEvent implements Serializable {
    private String productId;
    private String productName;
    private Money newPrice;
    private Long timestamp;
    private Double inventoryLevel;
    private Double currentDemand;

}
