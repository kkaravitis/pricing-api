package com.wordpress.kkaravitis.pricing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class PricingApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(PricingApiApplication.class);
    }
}
