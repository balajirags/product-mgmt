package com.inventory.demo.weather.service;

import com.inventory.demo.weather.api.WeatherResponse;
import com.inventory.demo.weather.config.WeatherClientProperties;
import com.inventory.demo.weather.domain.WeatherUnit;
import com.inventory.demo.weather.exception.WeatherProviderException;
import com.inventory.demo.weather.integration.OpenWeatherMapClient;
import com.inventory.demo.weather.integration.OpenWeatherMapResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private OpenWeatherMapClient openWeatherMapClient;

    @Mock
    private WeatherClientProperties properties;

    @InjectMocks
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        when(properties.getApiKey()).thenReturn("test-api-key");
    }

    private OpenWeatherMapResponse validResponse(String city, double temp, int humidity, String desc) {
        OpenWeatherMapResponse.Main main = new OpenWeatherMapResponse.Main(temp, humidity);
        OpenWeatherMapResponse.Weather weather = new OpenWeatherMapResponse.Weather(desc);
        return new OpenWeatherMapResponse(city, main, List.of(weather));
    }

    @Nested
    class SuccessCases {

        @Test
        void shouldReturnWeatherResponse_whenCityFound() {
            OpenWeatherMapResponse raw = validResponse("London", 18.5, 65, "clear sky");
            when(openWeatherMapClient.getCurrentWeather("London", "metric", "test-api-key"))
                    .thenReturn(raw);

            WeatherResponse result = weatherService.getWeatherByCity("London", "metric");

            assertThat(result.city()).isEqualTo("London");
            assertThat(result.temperature()).isEqualTo(18.5);
            assertThat(result.humidity()).isEqualTo(65);
            assertThat(result.description()).isEqualTo("clear sky");
            assertThat(result.unit()).isEqualTo(WeatherUnit.CELSIUS);
        }

        @Test
        void shouldDefaultToMetricUnits_whenUnitsIsNull() {
            OpenWeatherMapResponse raw = validResponse("Paris", 22.0, 50, "sunny");
            when(openWeatherMapClient.getCurrentWeather("Paris", "metric", "test-api-key"))
                    .thenReturn(raw);

            WeatherResponse result = weatherService.getWeatherByCity("Paris", null);

            assertThat(result.city()).isEqualTo("Paris");
        }

        @Test
        void shouldDefaultToMetricUnits_whenUnitsIsBlank() {
            OpenWeatherMapResponse raw = validResponse("Berlin", 10.0, 80, "cloudy");
            when(openWeatherMapClient.getCurrentWeather("Berlin", "metric", "test-api-key"))
                    .thenReturn(raw);

            WeatherResponse result = weatherService.getWeatherByCity("Berlin", "  ");

            assertThat(result.city()).isEqualTo("Berlin");
        }

        @Test
        void shouldUseFallbackDescription_whenWeatherListIsEmpty() {
            OpenWeatherMapResponse.Main main = new OpenWeatherMapResponse.Main(15.0, 60);
            OpenWeatherMapResponse raw = new OpenWeatherMapResponse("Tokyo", main, Collections.emptyList());
            when(openWeatherMapClient.getCurrentWeather("Tokyo", "metric", "test-api-key"))
                    .thenReturn(raw);

            WeatherResponse result = weatherService.getWeatherByCity("Tokyo", "metric");

            assertThat(result.description()).isEqualTo("No description available");
        }

        @Test
        void shouldUseFallbackDescription_whenWeatherListIsNull() {
            OpenWeatherMapResponse.Main main = new OpenWeatherMapResponse.Main(15.0, 60);
            OpenWeatherMapResponse raw = new OpenWeatherMapResponse("Oslo", main, null);
            when(openWeatherMapClient.getCurrentWeather("Oslo", "metric", "test-api-key"))
                    .thenReturn(raw);

            WeatherResponse result = weatherService.getWeatherByCity("Oslo", "metric");

            assertThat(result.description()).isEqualTo("No description available");
        }

        @Test
        void shouldUseFallbackDescription_whenDescriptionIsNull() {
            OpenWeatherMapResponse.Main main = new OpenWeatherMapResponse.Main(15.0, 60);
            OpenWeatherMapResponse.Weather weather = new OpenWeatherMapResponse.Weather(null);
            OpenWeatherMapResponse raw = new OpenWeatherMapResponse("Seoul", main, List.of(weather));
            when(openWeatherMapClient.getCurrentWeather("Seoul", "metric", "test-api-key"))
                    .thenReturn(raw);

            WeatherResponse result = weatherService.getWeatherByCity("Seoul", "metric");

            assertThat(result.description()).isEqualTo("No description available");
        }
    }

    @Nested
    class FailureCases {

        @Test
        void shouldThrowWeatherProviderException_whenMainFieldIsNull() {
            OpenWeatherMapResponse raw = new OpenWeatherMapResponse("Madrid", null, Collections.emptyList());
            when(openWeatherMapClient.getCurrentWeather("Madrid", "metric", "test-api-key"))
                    .thenReturn(raw);

            assertThatThrownBy(() -> weatherService.getWeatherByCity("Madrid", "metric"))
                    .isInstanceOf(WeatherProviderException.class)
                    .hasMessageContaining("missing temperature data");
        }
    }
}
