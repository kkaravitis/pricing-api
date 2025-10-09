/*
 * Copyright (c) 2025 Konstantinos Karavitis
 *
 * Licensed under the Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 * You may not use this file for commercial purposes.
 * See the LICENSE file in the project root or visit:
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

package com.wordpress.kkaravitis.pricing.adapter.inbound;

import com.wordpress.kkaravitis.pricing.domain.competitor.CompetitorPrice;
import com.wordpress.kkaravitis.pricing.domain.competitor.CompetitorPriceDto;
import com.wordpress.kkaravitis.pricing.domain.competitor.CompetitorPriceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/competitor-api/competitor-prices")
@RequiredArgsConstructor
public class CompetitorPriceController {

    private final CompetitorPriceService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CompetitorPriceDto> listAll() {
        return service.list();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveOrUpdate(@RequestBody CompetitorPriceDto dto) {
        service.save(dto);
    }

    @GetMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompetitorPrice> getCompetitorPrice(@PathVariable String productId) {
        CompetitorPrice payload = service.findCompetitorPrice(productId);
        return payload == null ? ResponseEntity.notFound().build() : ResponseEntity.ok().body(payload);
    }


}
