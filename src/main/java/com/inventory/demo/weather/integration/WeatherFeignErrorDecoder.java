package com.inventory.demo.weather.integration;

import com.inventory.demo.weather.exception.CityNotFoundException;
import com.inventory.demo.weather.exception.WeatherProviderException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Feign error decoder that maps HTTP error responses from OpenWeatherMap
 * into typed domain exceptions.
 *
 * <ul>
 *   <li>404 → {@link CityNotFoundException}</li>
 *   <li>401, 5xx → {@link WeatherProviderException}</li>
 * </ul>
 */
public class WeatherFeignErrorDecoder implements ErrorDecoder {

    private static final Logger log = LoggerFactory.getLogger(WeatherFeignErrorDecoder.class);

    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_UNAUTHORIZED = 401;

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String requestUrl = response.request().url();

        if (status == HTTP_NOT_FOUND) {
            log.warn("City not found - OpenWeatherMap returned 404 for method={}", methodKey);
            String cityName = extractCityFromUrl(requestUrl);
            return new CityNotFoundException(cityName);
        }

        if (status == HTTP_UNAUTHORIZED) {
            log.error("OpenWeatherMap API key is invalid or missing - method={} status={}", methodKey, status);
            return new WeatherProviderException("Weather provider authentication failed");
        }

        log.error("OpenWeatherMap returned unexpected status - method={} status={}", methodKey, status);
        return new WeatherProviderException(
                String.format("Weather provider returned unexpected status: %d", status));
    }

    private String extractCityFromUrl(String url) {
        if (url == null) {
            return "unknown";
        }
        int qIndex = url.indexOf('q');
        if (qIndex < 0) {
            return "unknown";
        }
        String afterQ = url.substring(qIndex + 2);
        int ampersand = afterQ.indexOf('&');
        return ampersand >= 0 ? afterQ.substring(0, ampersand) : afterQ;
    }
}
