package com.inventory.demo.weather.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the Weather module.
 * Enables {@link WeatherClientProperties} binding and validation at startup.
 */
@Configuration
@EnableConfigurationProperties(WeatherClientProperties.class)
public class WeatherConfig {
}
