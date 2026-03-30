package com.inventory.demo.weather.exception;

import com.inventory.demo.exception.DomainException;

/**
 * Thrown when the weather provider returns an unexpected error (5xx, 401, timeout).
 * Maps to HTTP 503 Service Unavailable.
 */
public class WeatherProviderException extends DomainException {

    private static final String ERROR_CODE = "WEATHER_PROVIDER_UNAVAILABLE";

    public WeatherProviderException(String message) {
        super(ERROR_CODE, message);
    }

    public WeatherProviderException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
