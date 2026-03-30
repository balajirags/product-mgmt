package com.inventory.demo.weather.api;

import com.inventory.demo.exception.GlobalExceptionHandler;
import com.inventory.demo.weather.domain.WeatherUnit;
import com.inventory.demo.weather.exception.CityNotFoundException;
import com.inventory.demo.weather.exception.WeatherProviderException;
import com.inventory.demo.weather.service.WeatherService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {WeatherController.class, GlobalExceptionHandler.class})
@ContextConfiguration(classes = {WeatherController.class, GlobalExceptionHandler.class})
class WeatherControllerTest {

    private static final String WEATHER_URL = "/api/v1/weather";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherService weatherService;

    private static WeatherResponse sampleWeatherResponse() {
        return new WeatherResponse("London", 18.5, 65, "clear sky", WeatherUnit.CELSIUS);
    }

    @Nested
    class SuccessCases {

        @Test
        void shouldReturn200_withWeatherData_whenCityFound() throws Exception {
            when(weatherService.getWeatherByCity("London", null)).thenReturn(sampleWeatherResponse());

            mockMvc.perform(get(WEATHER_URL).param("city", "London"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.city").value("London"))
                    .andExpect(jsonPath("$.temperature").value(18.5))
                    .andExpect(jsonPath("$.humidity").value(65))
                    .andExpect(jsonPath("$.description").value("clear sky"))
                    .andExpect(jsonPath("$.unit").value("CELSIUS"));
        }

        @Test
        void shouldReturn200_whenUnitsParamProvided() throws Exception {
            when(weatherService.getWeatherByCity("Paris", "metric")).thenReturn(
                    new WeatherResponse("Paris", 22.0, 50, "sunny", WeatherUnit.CELSIUS));

            mockMvc.perform(get(WEATHER_URL).param("city", "Paris").param("units", "metric"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.city").value("Paris"));
        }
    }

    @Nested
    class ErrorCases {

        @Test
        void shouldReturn404_whenCityNotFound() throws Exception {
            when(weatherService.getWeatherByCity("Atlantis", null))
                    .thenThrow(new CityNotFoundException("Atlantis"));

            mockMvc.perform(get(WEATHER_URL).param("city", "Atlantis"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("City Not Found"));
        }

        @Test
        void shouldReturn503_whenProviderUnavailable() throws Exception {
            when(weatherService.getWeatherByCity("London", null))
                    .thenThrow(new WeatherProviderException("upstream down"));

            mockMvc.perform(get(WEATHER_URL).param("city", "London"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.title").value("Weather Provider Unavailable"));
        }

        @Test
        void shouldReturn400_whenCityParamMissing() throws Exception {
            mockMvc.perform(get(WEATHER_URL))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400_whenCityIsBlank() throws Exception {
            mockMvc.perform(get(WEATHER_URL).param("city", ""))
                    .andExpect(status().isBadRequest());
        }
    }
}
