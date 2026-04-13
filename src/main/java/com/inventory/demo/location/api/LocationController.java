package com.inventory.demo.location.api;

import com.inventory.demo.location.service.LocationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for location registration endpoints.
 */
@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    private static final Logger log = LoggerFactory.getLogger(LocationController.class);

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Registers a new location in DRAFT state.
     *
     * @param request the location registration request with all mandatory fields
     * @return the created location with HTTP 201
     */
    @PostMapping
    public ResponseEntity<LocationResponse> registerLocation(@Valid @RequestBody CreateLocationRequest request) {
        log.info("POST /api/v1/locations - Registering location: code={}", request.locationCode());
        LocationResponse response = locationService.registerLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
