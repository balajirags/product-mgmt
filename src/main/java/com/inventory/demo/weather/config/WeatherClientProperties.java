package com.inventory.demo.weather.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the OpenWeatherMap API client.
 * The API key is loaded from the {@code WEATHER_API_KEY} environment variable.
 * Application fails to start if the key is missing or blank.
 */
@Validated
@ConfigurationProperties(prefix = "weather")
public class WeatherClientProperties {

    private static final String DEFAULT_BASE_URL = "https://api.openweathermap.org";
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 5000;

    /**
     * OpenWeatherMap API key. Must be set via {@code WEATHER_API_KEY} environment variable.
     * Never logged or exposed in error responses.
     */
    @NotBlank(message = "weather.api.key must not be blank. Set the WEATHER_API_KEY environment variable.")
    private String apiKey;

    /**
     * Base URL for the OpenWeatherMap API.
     */
    private String baseUrl = DEFAULT_BASE_URL;

    /**
     * Connection timeout in milliseconds.
     */
    private int connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;

    /**
     * Read timeout in milliseconds.
     */
    private int readTimeoutMs = DEFAULT_READ_TIMEOUT_MS;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }
}
