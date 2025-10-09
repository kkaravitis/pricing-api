/*
 * Copyright (c) 2025 Konstantinos Karavitis
 *
 * Licensed under the Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 * You may not use this file for commercial purposes.
 * See the LICENSE file in the project root or visit:
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

package com.wordpress.kkaravitis.pricing.domain.competitor;

import com.wordpress.kkaravitis.pricing.domain.Money;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompetitorPriceService {

    private final CompetitorPriceRepository repository;
    private String currency;

    public void save(CompetitorPriceDto dto) {
        repository.saveOrUpdate(dto);
    }

    public List<CompetitorPriceDto> list() {
        return repository.findAll();
    }

    public CompetitorPrice findCompetitorPrice(String productId) {
        CompetitorPriceDto dto = repository.findById(productId);
        return dto == null ? null : new CompetitorPrice(dto.productId(), new Money(dto.competitorPrice(), currency));
    }

    @Autowired
    protected void setCurrency(@Value("${app.currency}") String currency) {
        this.currency = currency;
    }

}
