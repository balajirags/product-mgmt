package com.inventory.demo.weather.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for the OpenWeatherMap current weather API.
 *
 * <p>Base URL is configured via {@code weather.base-url} property.
 * The API key is passed per-request as a query parameter and must never appear in logs.
 *
 * <p>Error handling is delegated to {@link WeatherFeignErrorDecoder}.
 */
@FeignClient(
        name = "open-weather-map-client",
        url = "${weather.base-url}",
        configuration = WeatherFeignClientConfig.class
)
public interface OpenWeatherMapClient {

    /**
     * Retrieves current weather data for a given city.
     *
     * @param city    the city name to query
     * @param units   temperature units (e.g., {@code metric} for Celsius)
     * @param apiKey  the OpenWeatherMap API key
     * @return the raw weather response from OpenWeatherMap
     */
    @GetMapping("/data/2.5/weather")
    OpenWeatherMapResponse getCurrentWeather(
            @RequestParam("q") String city,
            @RequestParam("units") String units,
            @RequestParam("appid") String apiKey
    );
}
