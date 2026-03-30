package com.inventory.demo.weather.api;

import com.inventory.demo.weather.service.WeatherService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the weather API.
 * Exposes {@code GET /api/v1/weather} to retrieve current weather by city name.
 */
@Validated
@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {

    private static final Logger log = LoggerFactory.getLogger(WeatherController.class);

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * Returns current weather for the specified city.
     *
     * @param city  the city name (required, max 100 characters)
     * @param units optional temperature units; defaults to {@code metric} (Celsius)
     * @return the weather response with temperature, humidity, description, and unit
     */
    @GetMapping
    public ResponseEntity<WeatherResponse> getWeather(
            @RequestParam("city")
            @NotBlank(message = "city must not be blank")
            @Size(max = 100, message = "city must not exceed 100 characters")
            String city,

            @RequestParam(name = "units", required = false)
            String units) {

        log.info("GET /api/v1/weather - city={} units={}", city, units);
        WeatherResponse response = weatherService.getWeatherByCity(city, units);
        return ResponseEntity.ok(response);
    }
}
