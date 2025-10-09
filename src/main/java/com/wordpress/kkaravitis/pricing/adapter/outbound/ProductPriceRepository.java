package com.wordpress.kkaravitis.pricing.adapter.outbound;

import static com.wordpress.kkaravitis.pricing.jooq.Tables.PRODUCT_PRICE_RESULTS;

import com.wordpress.kkaravitis.pricing.domain.ProductPriceResultWithChange;
import java.time.LocalDateTime;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class ProductPriceRepository {

    private final DSLContext dsl;

    public ProductPriceRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<ProductPriceResultWithChange> fetchLatestPriceResultsPerProduct() {
        return buildLatestPriceChangeQuery(null)
              .fetchInto(ProductPriceResultWithChange.class);
    }


    public ProductPriceResultWithChange fetchLatestPriceResultWithChangeForProduct(String productId) {
        return buildLatestPriceChangeQuery(productId)
              .fetchOneInto(ProductPriceResultWithChange.class);
    }


    private ResultQuery<? extends Record> buildLatestPriceChangeQuery(String productId) {

        Condition cond = (productId == null)
              ? DSL.noCondition()
              : PRODUCT_PRICE_RESULTS.PRODUCT_ID.eq(productId);

        Table<?> t = dsl
              .select(
                    PRODUCT_PRICE_RESULTS.PRODUCT_ID.as("product_id"),
                    PRODUCT_PRICE_RESULTS.ID.as("id"),
                    PRODUCT_PRICE_RESULTS.TIMESTAMP.as("timestamp"),
                    PRODUCT_PRICE_RESULTS.PRICE.as("price"),
                    DSL.lag(PRODUCT_PRICE_RESULTS.PRICE)
                          .over()
                          .partitionBy(PRODUCT_PRICE_RESULTS.PRODUCT_ID)
                          .orderBy(PRODUCT_PRICE_RESULTS.TIMESTAMP.desc(),
                                PRODUCT_PRICE_RESULTS.ID.desc())
                          .as("prev_price"),
                    DSL.rowNumber()
                          .over()
                          .partitionBy(PRODUCT_PRICE_RESULTS.PRODUCT_ID)
                          .orderBy(PRODUCT_PRICE_RESULTS.TIMESTAMP.desc(),
                                PRODUCT_PRICE_RESULTS.ID.desc())
                          .as("rn")
              )
              .from(PRODUCT_PRICE_RESULTS)
              .where(cond)
              .asTable("t");

        Field<String>          productIdF = t.field("product_id", String.class);
        Field<Long>            idF        = t.field("id", Long.class);
        Field<LocalDateTime>   tsF        = t.field("timestamp", LocalDateTime.class);
        Field<BigDecimal>      priceF     = t.field("price", BigDecimal.class);
        Field<BigDecimal>      prevF      = t.field("prev_price", BigDecimal.class);
        Field<Integer>         rnF        = t.field("rn", Integer.class);

        Field<BigDecimal> previousPrice =
              prevF.as("previousPrice");

        Field<BigDecimal> priceChangePercent =
              DSL.when(prevF.isNotNull().and(prevF.ne(BigDecimal.ZERO)),
                          DSL.round(priceF.minus(prevF).div(prevF).mul(BigDecimal.valueOf(100)), 2))
                    .otherwise((BigDecimal) null)
                    .as("priceChangePercent");

        Field<String> priceChangeLabel =
              DSL.when(prevF.isNull(), DSL.val((String) null))
                    .when(priceF.gt(prevF), DSL.inline("increase"))
                    .when(priceF.lt(prevF), DSL.inline("decrease"))
                    .otherwise(DSL.inline("same"))
                    .as("priceChangeLabel");

        return dsl
              .select(productIdF.as("product_id"),
                    idF.as("id"),
                    tsF.as("timestamp"),
                    priceF.as("price"),
                    previousPrice,
                    priceChangePercent,
                    priceChangeLabel)
              .from(t)
              .where(rnF.eq(1))
              .orderBy(productIdF.asc());
    }

}


