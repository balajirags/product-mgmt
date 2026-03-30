package com.inventory.demo.weather.service;

import com.inventory.demo.weather.api.WeatherResponse;
import com.inventory.demo.weather.config.WeatherClientProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import com.inventory.demo.weather.domain.WeatherInfo;
import com.inventory.demo.weather.domain.WeatherUnit;
import com.inventory.demo.weather.exception.WeatherProviderException;
import com.inventory.demo.weather.integration.OpenWeatherMapClient;
import com.inventory.demo.weather.integration.OpenWeatherMapResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for retrieving weather information by city name.
 * Delegates to the OpenWeatherMap Feign client and maps the response
 * to the internal domain model.
 */
@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    private static final String METRIC_UNITS = "metric";
    private static final String UNKNOWN_DESCRIPTION = "No description available";

    private final OpenWeatherMapClient openWeatherMapClient;
    private final WeatherClientProperties properties;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed singleton — safe to store reference")
    public WeatherService(OpenWeatherMapClient openWeatherMapClient, WeatherClientProperties properties) {
        this.openWeatherMapClient = openWeatherMapClient;
        this.properties = properties;
    }

    /**
     * Retrieves current weather for the given city.
     *
     * @param city  the city name (required, non-blank)
     * @param units the units parameter; only {@code metric} is supported
     * @return the weather response DTO
     * @throws com.inventory.demo.weather.exception.CityNotFoundException if the city is not found
     * @throws WeatherProviderException if the upstream provider is unavailable
     */
    public WeatherResponse getWeatherByCity(String city, String units) {
        String resolvedUnits = resolveUnits(units);
        log.info("Fetching weather for city={} units={}", city, resolvedUnits);

        OpenWeatherMapResponse rawResponse = openWeatherMapClient.getCurrentWeather(
                city, resolvedUnits, properties.getApiKey());

        WeatherInfo weatherInfo = mapToWeatherInfo(rawResponse);
        log.info("Weather retrieved successfully for city={} temperature={} humidity={}",
                weatherInfo.getCity(), weatherInfo.getTemperature(), weatherInfo.getHumidity());

        return toResponse(weatherInfo);
    }

    private String resolveUnits(String units) {
        if (units == null || units.isBlank()) {
            return METRIC_UNITS;
        }
        return units;
    }

    private WeatherInfo mapToWeatherInfo(OpenWeatherMapResponse raw) {
        if (raw.main() == null) {
            log.error("OpenWeatherMap response missing 'main' field for city={}", raw.name());
            throw new WeatherProviderException("Invalid response from weather provider: missing temperature data");
        }
        String description = extractDescription(raw.weather());
        return new WeatherInfo(
                raw.name(),
                raw.main().temp(),
                raw.main().humidity(),
                description,
                WeatherUnit.CELSIUS
        );
    }

    private String extractDescription(List<OpenWeatherMapResponse.Weather> weatherList) {
        if (weatherList == null || weatherList.isEmpty()) {
            return UNKNOWN_DESCRIPTION;
        }
        OpenWeatherMapResponse.Weather first = weatherList.get(0);
        return first.description() != null ? first.description() : UNKNOWN_DESCRIPTION;
    }

    private WeatherResponse toResponse(WeatherInfo info) {
        return new WeatherResponse(
                info.getCity(),
                info.getTemperature(),
                info.getHumidity(),
                info.getDescription(),
                info.getUnit()
        );
    }
}
