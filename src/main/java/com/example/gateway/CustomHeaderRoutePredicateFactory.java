package com.example.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.util.MultiValueMap;

import java.util.Base64;
import java.util.function.Predicate;

public class CustomHeaderRoutePredicateFactory extends AbstractRoutePredicateFactory<CustomHeaderRoutePredicateFactory.Config> {

    public CustomHeaderRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            MultiValueMap<String, String> headers = exchange.getRequest().getHeaders();
            String headerValue = headers.getFirst(config.getHeaderName());
            if (headerValue == null) {
                return false;
            }
            String[] headerValues = headerValue.split(" ");
            if (headerValues.length != 2) {
                return false;
            }
            String bearerToken = headerValues[1];
            String[] chunks = bearerToken.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();

            String payload = new String(decoder.decode(chunks[1]));

            // Jackson main object
            ObjectMapper mapper = new ObjectMapper();

            // read the json strings and convert it into JsonNode
            try {
                JsonNode node = mapper.readTree(payload);
                if (node.get("resident_region") == null) {
                    return false;
                }
                String region = node.get("resident_region").asText();
                return region != null && region.equals(config.getRegionName());
            } catch (JsonProcessingException e) {
                return false;
            }
        };
    }

    public static class Config {
        private String headerName;
        private String regionName;

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }

        public String getRegionName() {
            return regionName;
        }

        public void setRegionName(String regionName) {
            this.regionName = regionName;
        }
    }
}
