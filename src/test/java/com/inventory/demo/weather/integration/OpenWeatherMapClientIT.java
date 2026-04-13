package com.inventory.demo.weather.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.inventory.demo.weather.exception.CityNotFoundException;
import com.inventory.demo.weather.exception.WeatherProviderException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for {@link OpenWeatherMapClient} using WireMock.
 * Verifies HTTP serialization, query parameter mapping, and error decoding.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "weather.base-url=http://localhost:${wiremock.server.port}",
        "weather.api-key=test-api-key"
})
class OpenWeatherMapClientIT {

    @Autowired
    private OpenWeatherMapClient openWeatherMapClient;

    private static final String WEATHER_PATH = "/data/2.5/weather";
    private static final String API_KEY = "test-api-key";

    @Test
    void shouldFetchWeather_whenCityIsValid() {
        stubFor(get(urlPathEqualTo(WEATHER_PATH))
                .withQueryParam("q", equalTo("London"))
                .withQueryParam("units", equalTo("metric"))
                .withQueryParam("appid", equalTo(API_KEY))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "name": "London",
                                  "main": { "temp": 18.5, "humidity": 65 },
                                  "weather": [{ "description": "clear sky" }]
                                }
                                """)));

        OpenWeatherMapResponse response = openWeatherMapClient.getCurrentWeather("London", "metric", API_KEY);

        assertThat(response.name()).isEqualTo("London");
        assertThat(response.main().temp()).isEqualTo(18.5);
        assertThat(response.main().humidity()).isEqualTo(65);
        assertThat(response.weather()).hasSize(1);
        assertThat(response.weather().get(0).description()).isEqualTo("clear sky");
    }

    @Test
    void shouldThrowCityNotFoundException_whenCityNotFound() {
        stubFor(get(urlPathEqualTo(WEATHER_PATH))
                .withQueryParam("q", equalTo("Atlantis"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\": \"city not found\"}")));

        assertThatThrownBy(() -> openWeatherMapClient.getCurrentWeather("Atlantis", "metric", API_KEY))
                .isInstanceOf(CityNotFoundException.class);
    }

    @Test
    void shouldThrowWeatherProviderException_whenApiKeyInvalid() {
        stubFor(get(urlPathEqualTo(WEATHER_PATH))
                .withQueryParam("appid", equalTo("invalid-key"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\": \"Invalid API key\"}")));

        assertThatThrownBy(() -> openWeatherMapClient.getCurrentWeather("London", "metric", "invalid-key"))
                .isInstanceOf(WeatherProviderException.class);
    }

    @Test
    void shouldThrowWeatherProviderException_whenServerError() {
        WireMock.stubFor(get(urlPathEqualTo(WEATHER_PATH))
                .withQueryParam("q", equalTo("ErrorCity"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\": \"Internal server error\"}")));

        assertThatThrownBy(() -> openWeatherMapClient.getCurrentWeather("ErrorCity", "metric", API_KEY))
                .isInstanceOf(WeatherProviderException.class);
    }
}
