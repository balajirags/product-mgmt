package com.inventory.demo.weather.integration;

import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign client configuration for the OpenWeatherMap client.
 * Sets log level to BASIC (no request/response bodies — avoids leaking the API key),
 * disables Feign's internal retries (resilience handled at application level),
 * and registers the custom error decoder.
 */
@Configuration
public class WeatherFeignClientConfig {

    /**
     * Use BASIC logging to record method, URL, status, and elapsed time only.
     * FULL logging is intentionally avoided to prevent API key exposure in logs.
     */
    @Bean
    public Logger.Level weatherFeignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Disable Feign internal retries — retries should be managed by Resilience4j if needed.
     */
    @Bean
    public Retryer weatherFeignRetryer() {
        return Retryer.NEVER_RETRY;
    }

    /**
     * Register the custom error decoder that maps HTTP errors to domain exceptions.
     */
    @Bean
    public ErrorDecoder weatherFeignErrorDecoder() {
        return new WeatherFeignErrorDecoder();
    }
}
