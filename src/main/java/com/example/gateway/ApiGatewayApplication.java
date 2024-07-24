package com.example.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

    @Value("${regions.current.url}")
    private String currentRegionUrl;

    @Value("${regions.eu.url}")
    private String euRegionUrl;

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("ProfileEu", r -> r.path("/profile/**")
                        .and()
                        .predicate(new CustomHeaderRoutePredicateFactory().apply(config -> {
                            config.setHeaderName("Authorization");
                            config.setRegionName("EU");
                        }))
                        .filters(f -> f.rewritePath("/profile(?<segment>/?.*)", "/scim2${segment}"))
                        .uri(euRegionUrl))
                .route("ProfileCurrent", r -> r.path("/profile/**")
                        .and()
                        .predicate(new CustomHeaderRoutePredicateFactory().apply(config -> {
                            config.setHeaderName("Authorization");
                            config.setRegionName(null);
                        }))
                        .filters(f -> f.rewritePath("/profile(?<segment>/?.*)", "/scim2${segment}"))
                        .uri(currentRegionUrl))
                .build();
    }
}
