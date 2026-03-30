package com.inventory.demo.weather.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inventory.demo.weather.domain.WeatherUnit;

/**
 * Response DTO for the weather API endpoint.
 * Uses snake_case JSON properties per API conventions.
 */
public record WeatherResponse(
        @JsonProperty("city") String city,
        @JsonProperty("temperature") double temperature,
        @JsonProperty("humidity") int humidity,
        @JsonProperty("description") String description,
        @JsonProperty("unit") WeatherUnit unit
) {
}
