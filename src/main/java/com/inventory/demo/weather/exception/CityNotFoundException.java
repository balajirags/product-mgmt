package com.inventory.demo.weather.exception;

import com.inventory.demo.exception.DomainException;

/**
 * Thrown when the requested city is not found by the weather provider.
 * Maps to HTTP 404 Not Found.
 */
public class CityNotFoundException extends DomainException {

    private static final String ERROR_CODE = "CITY_NOT_FOUND";

    public CityNotFoundException(String cityName) {
        super(ERROR_CODE, String.format("City not found: %s", cityName));
    }
}
