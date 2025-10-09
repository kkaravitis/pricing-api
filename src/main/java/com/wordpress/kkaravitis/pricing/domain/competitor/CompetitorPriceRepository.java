/*
 * Copyright (c) 2025 Konstantinos Karavitis
 *
 * Licensed under the Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 * You may not use this file for commercial purposes.
 * See the LICENSE file in the project root or visit:
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

package com.wordpress.kkaravitis.pricing.domain.competitor;

import static com.wordpress.kkaravitis.pricing.jooq.Tables.COMPETITOR_PRICE;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.val;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CompetitorPriceRepository {

    private final DSLContext dsl;

    public List<CompetitorPriceDto> findAll() {
        return dsl.select(COMPETITOR_PRICE.PRODUCT_ID,
                    COMPETITOR_PRICE.PRODUCT_NAME,
                    COMPETITOR_PRICE.COMPETITOR_PRICE_,
                    COMPETITOR_PRICE.UPDATED_AT)
              .from(COMPETITOR_PRICE)
              .orderBy(COMPETITOR_PRICE.PRODUCT_NAME.asc().nullsLast(), COMPETITOR_PRICE.PRODUCT_ID.asc())
              .fetch(CompetitorPriceRepository::toDto);
    }

    public void saveOrUpdate(CompetitorPriceDto dto) {
        Objects.requireNonNull(dto.productId(), "productId is required");
        Objects.requireNonNull(dto.competitorPrice(), "competitorPrice is required");

        OffsetDateTime now = OffsetDateTime.now();

        dsl.insertInto(COMPETITOR_PRICE)
              .set(COMPETITOR_PRICE.PRODUCT_ID, dto.productId())
              .set(COMPETITOR_PRICE.PRODUCT_NAME, dto.productName()) // nullable on insert
              .set(COMPETITOR_PRICE.COMPETITOR_PRICE_, dto.competitorPrice())
              .set(COMPETITOR_PRICE.UPDATED_AT, now)
              .onConflict(COMPETITOR_PRICE.PRODUCT_ID)
              .doUpdate()
              .set(COMPETITOR_PRICE.COMPETITOR_PRICE_, dto.competitorPrice())
              .set(COMPETITOR_PRICE.PRODUCT_NAME, coalesce(val(dto.productName()), COMPETITOR_PRICE.PRODUCT_NAME))
              .set(COMPETITOR_PRICE.UPDATED_AT, now)
              .execute();
    }

    public CompetitorPriceDto findById(String productId) {
        var competitorPriceRecord = dsl.select(COMPETITOR_PRICE.PRODUCT_ID,
                    COMPETITOR_PRICE.PRODUCT_NAME,
                    COMPETITOR_PRICE.COMPETITOR_PRICE_,
                    COMPETITOR_PRICE.UPDATED_AT)
              .from(COMPETITOR_PRICE)
              .where(COMPETITOR_PRICE.PRODUCT_ID.eq(productId))
              .fetchOne();

        return competitorPriceRecord == null ? null : toDto(competitorPriceRecord);
    }

    private static CompetitorPriceDto toDto(Record4<String, String, BigDecimal, OffsetDateTime> record) {
        return new CompetitorPriceDto(
              record.get(COMPETITOR_PRICE.PRODUCT_ID),
              record.get(COMPETITOR_PRICE.PRODUCT_NAME),
              record.get(COMPETITOR_PRICE.COMPETITOR_PRICE_),
              record.get(COMPETITOR_PRICE.UPDATED_AT)
        );
    }

}
