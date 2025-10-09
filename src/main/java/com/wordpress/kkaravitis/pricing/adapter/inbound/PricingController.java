/*
 * Copyright (c) 2025 Konstantinos Karavitis
 *
 * Licensed under the Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 * You may not use this file for commercial purposes.
 * See the LICENSE file in the project root or visit:
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

package com.wordpress.kkaravitis.pricing.adapter.inbound;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.wordpress.kkaravitis.pricing.domain.Money;
import com.wordpress.kkaravitis.pricing.domain.PriceAdvisorService;
import com.wordpress.kkaravitis.pricing.domain.ProductPriceResultWithChange;
import com.wordpress.kkaravitis.pricing.domain.events.BusinessRuleEvent;
import com.wordpress.kkaravitis.pricing.domain.events.ClickEvent;
import com.wordpress.kkaravitis.pricing.domain.events.InventoryLevelEvent;
import com.wordpress.kkaravitis.pricing.domain.events.OrderEvent;
import com.wordpress.kkaravitis.pricing.domain.events.PriceRule;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/pricing-api", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PricingController {

    private final PriceAdvisorService priceAdvisorService;
    private String currency;

    @GetMapping(path="/prices/latest", produces = APPLICATION_JSON_VALUE)
    public List<ProductPriceResultWithChange> getProductPriceResults() {
        return priceAdvisorService.getLatestProductPriceResults();
    }

    @PostMapping(path="/events/click", consumes = APPLICATION_JSON_VALUE)
    public void sendClickEvent(@RequestBody ClickEvent clickEvent) {
        priceAdvisorService.sendClickEvent(clickEvent);
    }

    @PostMapping(value = "/events/order", consumes = APPLICATION_JSON_VALUE)
    public void postOrder(@RequestBody OrderEvent orderEvent) {
        priceAdvisorService.sendOrderEvent(orderEvent);
    }

    @PostMapping(value = "/events/inventory", consumes = APPLICATION_JSON_VALUE)
    public void postInventory(@RequestBody InventoryLevelEvent inventoryLevelEvent) {
        priceAdvisorService.sendInventoryLevelEvent(inventoryLevelEvent);

    }

    @PostMapping(value = "/events/rule", consumes = APPLICATION_JSON_VALUE)
    public void postRule(@RequestBody BusinessRuleEventRequest request) {
        BusinessRuleEvent ruleEvent = new BusinessRuleEvent(request.productId,
              request.getProductName(),
              new PriceRule(new Money(request.min, currency),
                    new Money(request.max, currency)));
        priceAdvisorService.sendBusinessRuleEvent(ruleEvent);
    }

    @Autowired
    protected void setCurrency(@Value("${app.currency}") String currency) {
        this.currency = currency;
    }


    @Data
    static class BusinessRuleEventRequest {
        String productId;
        String productName;
        BigDecimal min;
        BigDecimal max;
    }

}
