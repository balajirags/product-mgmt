package com.inventory.demo.location.service;

import com.inventory.demo.exception.BusinessRuleException;
import com.inventory.demo.location.api.CreateLocationRequest;
import com.inventory.demo.location.api.LocationResponse;
import com.inventory.demo.location.domain.Location;
import com.inventory.demo.location.domain.LocationCode;
import com.inventory.demo.location.domain.LocationType;
import com.inventory.demo.location.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing location registration and related business operations.
 */
@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private static final String DUPLICATE_LOCATION_ERROR = "DUPLICATE_LOCATION";
    private static final String INVALID_LOCATION_TYPE_ERROR = "INVALID_LOCATION_TYPE";
    private static final String INVALID_LOCATION_CODE_ERROR = "INVALID_LOCATION_CODE";

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    /**
     * Registers a new location in DRAFT state.
     *
     * @param request the location registration request
     * @return the created location as a response DTO
     * @throws BusinessRuleException if the location code or type is invalid,
     *                               or if a duplicate location exists
     */
    @Transactional
    public LocationResponse registerLocation(CreateLocationRequest request) {
        log.info("Registering new location: code={}, name={}", request.locationCode(), request.locationName());

        LocationCode locationCode = validateLocationCode(request.locationCode());
        LocationType locationType = validateLocationType(request.locationType());
        checkForDuplicate(request.locationCode(), request.locationName());

        Location location = Location.create(
                locationCode.getCode(),
                request.locationName(),
                locationType,
                request.address().street(),
                request.address().city(),
                request.address().state(),
                request.address().postalCode(),
                request.address().country(),
                request.contactPerson()
        );

        Location savedLocation = locationRepository.save(location);
        log.info("Location registered successfully: id={}, code={}, name={}, state={}",
                savedLocation.getId(), savedLocation.getLocationCode(),
                savedLocation.getLocationName(), savedLocation.getLifecycleState());

        return LocationResponse.fromEntity(savedLocation);
    }

    private LocationCode validateLocationCode(String code) {
        try {
            return LocationCode.fromCode(code);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid location code attempted: {}", code);
            throw new BusinessRuleException(INVALID_LOCATION_CODE_ERROR,
                    "Invalid location code: " + code + ". Valid codes are: WH-MUM, WH-CHN, WH-BEN", ex);
        }
    }

    private LocationType validateLocationType(String type) {
        try {
            return LocationType.valueOf(type.toUpperCase(java.util.Locale.ROOT).replace(" ", "_").replace("-", "_"));
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid location type attempted: {}", type);
            throw new BusinessRuleException(INVALID_LOCATION_TYPE_ERROR,
                    "Invalid location type: " + type + ". Valid types are: WAREHOUSE, STORE, DARKSTORE, THREE_PL_NODE", ex);
        }
    }

    private void checkForDuplicate(String locationCode, String locationName) {
        if (locationRepository.existsByLocationCodeAndLocationName(locationCode, locationName)) {
            log.warn("Duplicate location registration attempted: code={}, name={}", locationCode, locationName);
            throw new BusinessRuleException(DUPLICATE_LOCATION_ERROR,
                    "Location with code " + locationCode + " and name " + locationName + " already exists");
        }
    }
}
