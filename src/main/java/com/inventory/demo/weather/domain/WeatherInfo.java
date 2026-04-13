package com.inventory.demo.weather.domain;

/**
 * Immutable domain model representing weather information for a city.
 * This is the internal representation used by the service layer.
 */
public final class WeatherInfo {

    private final String city;
    private final double temperature;
    private final int humidity;
    private final String description;
    private final WeatherUnit unit;

    public WeatherInfo(String city, double temperature, int humidity, String description, WeatherUnit unit) {
        this.city = city;
        this.temperature = temperature;
        this.humidity = humidity;
        this.description = description;
        this.unit = unit;
    }

    public String getCity() {
        return city;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public String getDescription() {
        return description;
    }

    public WeatherUnit getUnit() {
        return unit;
    }
}
