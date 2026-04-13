package com.inventory.demo.location.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a physical fulfillment location (warehouse, store, darkstore, 3PL node).
 * Locations are always created in DRAFT lifecycle state.
 */
@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "location_code", nullable = false)
    private String locationCode;

    @Column(name = "location_name", nullable = false)
    private String locationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false)
    private LocationType locationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_state", nullable = false)
    private LifecycleState lifecycleState;

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "contact_person", nullable = false)
    private String contactPerson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Location() {
        // JPA requires a no-arg constructor
    }

    private Location(String locationCode,
                     String locationName,
                     LocationType locationType,
                     String street,
                     String city,
                     String state,
                     String postalCode,
                     String country,
                     String contactPerson) {
        this.locationCode = locationCode;
        this.locationName = locationName;
        this.locationType = locationType;
        this.lifecycleState = LifecycleState.DRAFT;
        this.street = street;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.contactPerson = contactPerson;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Factory method to create a new Location in DRAFT state.
     *
     * @param locationCode  the predefined location code (e.g., "WH-MUM")
     * @param locationName  the human-readable name
     * @param locationType  the type of location
     * @param street        the street address
     * @param city          the city
     * @param state         the state or province
     * @param postalCode    the postal code
     * @param country       the country
     * @param contactPerson the name of the operational contact
     * @return a new Location in DRAFT state
     */
    public static Location create(String locationCode,
                                  String locationName,
                                  LocationType locationType,
                                  String street,
                                  String city,
                                  String state,
                                  String postalCode,
                                  String country,
                                  String contactPerson) {
        return new Location(locationCode, locationName, locationType,
                street, city, state, postalCode, country, contactPerson);
    }

    public UUID getId() {
        return id;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public String getLocationName() {
        return locationName;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public LifecycleState getLifecycleState() {
        return lifecycleState;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
