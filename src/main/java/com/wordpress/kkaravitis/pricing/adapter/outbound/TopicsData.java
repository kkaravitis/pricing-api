package com.wordpress.kkaravitis.pricing.adapter.outbound;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.kafka.topics")
public class TopicsData {

    private String clickEventTopic;

    private String orderEventTopic;

    private String inventoryEventTopic;

    private String ruleEventTopic;

    private String priceResultTopic;
}
