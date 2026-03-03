package com.inventory.demo.location.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.demo.exception.BusinessRuleException;
import com.inventory.demo.exception.GlobalExceptionHandler;
import com.inventory.demo.location.service.LocationService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {LocationController.class, GlobalExceptionHandler.class})
@ContextConfiguration(classes = {LocationController.class, GlobalExceptionHandler.class})
class LocationControllerTest {

    private static final String LOCATIONS_URL = "/api/v1/locations";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LocationService locationService;

    private static LocationResponse sampleResponse() {
        return new LocationResponse(
                UUID.randomUUID(),
                "WH-MUM",
                "Mumbai Central Warehouse",
                "WAREHOUSE",
                "DRAFT",
                new LocationResponse.AddressResponse(
                        "123 Industrial Area",
                        "Mumbai",
                        "Maharashtra",
                        "400001",
                        "India"
                ),
                "John Doe",
                Instant.now(),
                Instant.now()
        );
    }

    private static String validRequestJson() {
        return """
                {
                    "location_code": "WH-MUM",
                    "location_name": "Mumbai Central Warehouse",
                    "location_type": "WAREHOUSE",
                    "address": {
                        "street": "123 Industrial Area",
                        "city": "Mumbai",
                        "state": "Maharashtra",
                        "postal_code": "400001",
                        "country": "India"
                    },
                    "contact_person": "John Doe"
                }
                """;
    }

    @Nested
    class SuccessCases {

        @Test
        void shouldReturnCreated_whenValidRequestIsSubmitted() throws Exception {
            // given
            when(locationService.registerLocation(any(CreateLocationRequest.class)))
                    .thenReturn(sampleResponse());

            // when / then
            mockMvc.perform(post(LOCATIONS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validRequestJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.location_code").value("WH-MUM"))
                    .andExpect(jsonPath("$.location_name").value("Mumbai Central Warehouse"))
                    .andExpect(jsonPath("$.location_type").value("WAREHOUSE"))
                    .andExpect(jsonPath("$.lifecycle_state").value("DRAFT"))
                    .andExpect(jsonPath("$.contact_person").value("John Doe"))
                    .andExpect(jsonPath("$.address.street").value("123 Industrial Area"))
                    .andExpect(jsonPath("$.address.city").value("Mumbai"));
        }
    }

    @Nested
    class ValidationCases {

        @Test
        void shouldReturnBadRequest_whenLocationCodeIsMissing() throws Exception {
            // given
            String request = """
                    {
                        "location_name": "Mumbai Central Warehouse",
                        "location_type": "WAREHOUSE",
                        "address": {
                            "street": "123 Industrial Area",
                            "city": "Mumbai",
                            "state": "Maharashtra",
                            "postal_code": "400001",
                            "country": "India"
                        },
                        "contact_person": "John Doe"
                    }
                    """;

            // when / then
            mockMvc.perform(post(LOCATIONS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.field_errors.locationCode").value("location_code is required"));

            verify(locationService, never()).registerLocation(any());
        }

        @Test
        void shouldReturnBadRequest_whenLocationNameIsMissing() throws Exception {
            // given
            String request = """
                    {
                        "location_code": "WH-MUM",
                        "location_type": "WAREHOUSE",
                        "address": {
                            "street": "123 Industrial Area",
                            "city": "Mumbai",
                            "state": "Maharashtra",
                            "postal_code": "400001",
                            "country": "India"
                        },
                        "contact_person": "John Doe"
                    }
                    """;

            // when / then
            mockMvc.perform(post(LOCATIONS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.field_errors.locationName").value("location_name is required"));

            verify(locationService, never()).registerLocation(any());
        }

        @Test
        void shouldReturnBadRequest_whenContactPersonIsMissing() throws Exception {
            // given
            String request = """
                    {
                        "location_code": "WH-MUM",
                        "location_name": "Mumbai Central Warehouse",
                        "location_type": "WAREHOUSE",
                        "address": {
                            "street": "123 Industrial Area",
                            "city": "Mumbai",
                            "state": "Maharashtra",
                            "postal_code": "400001",
                            "country": "India"
                        }
                    }
                    """;

            // when / then
            mockMvc.perform(post(LOCATIONS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.field_errors.contactPerson").value("contact_person is required"));

            verify(locationService, never()).registerLocation(any());
        }

        @Test
        void shouldReturnBadRequest_whenAddressIsMissing() throws Exception {
            // given
            String request = """
                    {
                        "location_code": "WH-MUM",
                        "location_name": "Mumbai Central Warehouse",
                        "location_type": "WAREHOUSE",
                        "contact_person": "John Doe"
                    }
                    """;

            // when / then
            mockMvc.perform(post(LOCATIONS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.field_errors.address").value("address is required"));

            verify(locationService, never()).registerLocation(any());
        }

        @Test
        void shouldReturnBadRequest_whenLocationTypeIsMissing() throws Exception {
            // given
            String request = """
                    {
                        "location_code": "WH-MUM",
                        "location_name": "Mumbai Central Warehouse",
                        "address": {
                            "street": "123 Industrial Area",
                            "city": "Mumbai",
                            "state": "Maharashtra",
                            "postal_code": "400001",
                            "country": "India"
                        },
                        "contact_person": "John Doe"
                    }
                    """;

            // when / then
            mockMvc.perform(post(LOCATIONS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.field_errors.locationType").value("location_type is required"));

            verify(locationService, never()).registerLocation(any());
        }

        @Test
        void shouldReturnBadRequest_whenAddressFieldsAreBlank() throws Exception {
            // given
            String request = """
                    {
                        "location_code": "WH-MUM",
                        "location_name": "Mumbai Central Warehouse",
                        "location_type": "WAREHOUSE",
                        "address": {
                            "street": "",
                            "city": "",
                            "state": "",
                            "postal_code": "",
                            "country": ""
                        },
                        "contact_person": "John Doe"
                    }
                    """;

            // when / then
            mockMvc.perform(post(LOCATIONS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"));

            verify(locationService, never()).registerLocation(any());
        }
    }

    @Nested
    class BusinessRuleCases {

        @Test
        void shouldReturnConflict_whenDuplicateLocation() throws Exception {
            // given
            when(locationService.registerLocation(any(CreateLocationRequest.class)))
                    .thenThrow(new BusinessRuleException("DUPLICATE_LOCATION",
                            "Location with code WH-MUM and name Mumbai Central Warehouse already exists"));

            // when / then
            mockMvc.perform(post(LOCATIONS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validRequestJson()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("DUPLICATE_LOCATION"))
                    .andExpect(jsonPath("$.detail").value("Location with code WH-MUM and name Mumbai Central Warehouse already exists"));
        }

        @Test
        void shouldReturnConflict_whenInvalidLocationCode() throws Exception {
            // given
            when(locationService.registerLocation(any(CreateLocationRequest.class)))
                    .thenThrow(new BusinessRuleException("INVALID_LOCATION_CODE",
                            "Invalid location code: INVALID"));

            String request = """
                    {
                        "location_code": "INVALID",
                        "location_name": "Test",
                        "location_type": "WAREHOUSE",
                        "address": {
                            "street": "Street",
                            "city": "City",
                            "state": "State",
                            "postal_code": "12345",
                            "country": "Country"
                        },
                        "contact_person": "Contact"
                    }
                    """;

            // when / then
            mockMvc.perform(post(LOCATIONS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("INVALID_LOCATION_CODE"));
        }
    }
}
