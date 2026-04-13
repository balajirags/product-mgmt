package com.inventory.demo.location.repository;

import com.inventory.demo.location.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for Location aggregate.
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    /**
     * Checks whether a location with the given code and name already exists.
     *
     * @param locationCode the location code
     * @param locationName the location name
     * @return true if a duplicate exists
     */
    boolean existsByLocationCodeAndLocationName(String locationCode, String locationName);
}
