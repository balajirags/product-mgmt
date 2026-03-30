package com.inventory.demo.weather.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Collections;
import java.util.List;

/**
 * Raw response DTO from the OpenWeatherMap API.
 * Intentionally kept in the integration layer — never exposed to the service or API layer.
 * The {@code weather} list is defensively copied to prevent exposure of mutable state.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class OpenWeatherMapResponse {

    private final String cityName;
    private final Main mainData;
    private final List<Weather> weatherConditions;

    public OpenWeatherMapResponse(
            @JsonProperty("name") String name,
            @JsonProperty("main") Main main,
            @JsonProperty("weather") List<Weather> weather) {
        this.cityName = name;
        this.mainData = main;
        this.weatherConditions = weather == null ? Collections.emptyList() : Collections.unmodifiableList(weather);
    }

    public String name() {
        return cityName;
    }

    public Main main() {
        return mainData;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "List is already unmodifiable — safe to return")
    public List<Weather> weather() {
        return weatherConditions;
    }

    /**
     * Main weather metrics returned by OpenWeatherMap.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Main(
            @JsonProperty("temp") double temp,
            @JsonProperty("humidity") int humidity
    ) {
    }

    /**
     * Weather condition description returned by OpenWeatherMap.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Weather(
            @JsonProperty("description") String description
    ) {
    }
}
