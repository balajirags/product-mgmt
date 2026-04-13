package com.inventory.demo.weather.integration;

import com.inventory.demo.weather.exception.CityNotFoundException;
import com.inventory.demo.weather.exception.WeatherProviderException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherFeignErrorDecoderTest {

    private final WeatherFeignErrorDecoder decoder = new WeatherFeignErrorDecoder();

    private Response buildResponse(int status, String url) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                url,
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                null
        );
        return Response.builder()
                .status(status)
                .reason("test")
                .request(request)
                .headers(Collections.emptyMap())
                .build();
    }

    @Test
    void shouldThrowCityNotFoundException_whenStatusIs404() {
        Response response = buildResponse(404, "https://api.openweathermap.org/data/2.5/weather?q=London&units=metric");

        Exception result = decoder.decode("getCurrentWeather", response);

        assertThat(result).isInstanceOf(CityNotFoundException.class);
        assertThat(result.getMessage()).contains("London");
    }

    @Test
    void shouldThrowWeatherProviderException_whenStatusIs401() {
        Response response = buildResponse(401, "https://api.openweathermap.org/data/2.5/weather?q=Paris");

        Exception result = decoder.decode("getCurrentWeather", response);

        assertThat(result).isInstanceOf(WeatherProviderException.class);
        assertThat(result.getMessage()).contains("authentication failed");
    }

    @Test
    void shouldThrowWeatherProviderException_whenStatusIs500() {
        Response response = buildResponse(500, "https://api.openweathermap.org/data/2.5/weather?q=Berlin");

        Exception result = decoder.decode("getCurrentWeather", response);

        assertThat(result).isInstanceOf(WeatherProviderException.class);
        assertThat(result.getMessage()).contains("500");
    }

    @Test
    void shouldReturnCityNotFound_withUnknown_whenUrlHasNoQParam() {
        Response response = buildResponse(404, "https://api.openweathermap.org/data/2.5/weather");

        Exception result = decoder.decode("getCurrentWeather", response);

        assertThat(result).isInstanceOf(CityNotFoundException.class);
        assertThat(result.getMessage()).contains("unknown");
    }

    @Test
    void shouldReturnCityNotFound_withUnknown_whenUrlIsNull() {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "https://api.openweathermap.org/placeholder",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                null
        );
        Response response = Response.builder()
                .status(404)
                .reason("test")
                .request(request)
                .headers(Collections.emptyMap())
                .build();

        Exception result = decoder.decode("getCurrentWeather", response);

        assertThat(result).isInstanceOf(CityNotFoundException.class);
    }
}
