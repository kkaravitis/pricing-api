package com.wordpress.kkaravitis.pricing.adapter.outbound;

import com.wordpress.kkaravitis.pricing.domain.events.BusinessRuleEvent;
import com.wordpress.kkaravitis.pricing.domain.events.ClickEvent;
import com.wordpress.kkaravitis.pricing.domain.events.InventoryLevelEvent;
import com.wordpress.kkaravitis.pricing.domain.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafka;

    private final TopicsData topicsData;


    public void send(ClickEvent event) {
        kafka.send(topicsData.getClickEventTopic(), event);
    }

    public void send(OrderEvent event) {
        kafka.send(topicsData.getOrderEventTopic(), event);
    }

    public void send(InventoryLevelEvent event) {
        kafka.send(topicsData.getInventoryEventTopic(), event);
    }

    public void send(BusinessRuleEvent event) {
        kafka.send(topicsData.getRuleEventTopic(), event);
    }
}
