package com.inventory.demo.location.service;

import com.inventory.demo.exception.BusinessRuleException;
import com.inventory.demo.location.api.CreateLocationRequest;
import com.inventory.demo.location.api.LocationResponse;
import com.inventory.demo.location.domain.LifecycleState;
import com.inventory.demo.location.domain.Location;
import com.inventory.demo.location.domain.LocationType;
import com.inventory.demo.location.repository.LocationRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationService locationService;

    private static CreateLocationRequest validRequest() {
        return new CreateLocationRequest(
                "WH-MUM",
                "Mumbai Central Warehouse",
                "WAREHOUSE",
                new CreateLocationRequest.AddressRequest(
                        "123 Industrial Area",
                        "Mumbai",
                        "Maharashtra",
                        "400001",
                        "India"
                ),
                "John Doe"
        );
    }

    @Nested
    class SuccessCases {

        @Test
        void shouldRegisterLocation_whenAllFieldsAreValid() {
            // given
            CreateLocationRequest request = validRequest();
            when(locationRepository.existsByLocationCodeAndLocationName("WH-MUM", "Mumbai Central Warehouse"))
                    .thenReturn(false);
            when(locationRepository.save(any(Location.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            LocationResponse response = locationService.registerLocation(request);

            // then
            assertThat(response.locationCode()).isEqualTo("WH-MUM");
            assertThat(response.locationName()).isEqualTo("Mumbai Central Warehouse");
            assertThat(response.locationType()).isEqualTo("WAREHOUSE");
            assertThat(response.lifecycleState()).isEqualTo("DRAFT");
            assertThat(response.contactPerson()).isEqualTo("John Doe");
            assertThat(response.address().street()).isEqualTo("123 Industrial Area");
            assertThat(response.address().city()).isEqualTo("Mumbai");
            assertThat(response.address().state()).isEqualTo("Maharashtra");
            assertThat(response.address().postalCode()).isEqualTo("400001");
            assertThat(response.address().country()).isEqualTo("India");

            ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
            verify(locationRepository).save(captor.capture());
            Location savedLocation = captor.getValue();
            assertThat(savedLocation.getLifecycleState()).isEqualTo(LifecycleState.DRAFT);
        }

        @Test
        void shouldSetLifecycleStateToDraft_whenLocationIsCreated() {
            // given
            CreateLocationRequest request = validRequest();
            when(locationRepository.existsByLocationCodeAndLocationName("WH-MUM", "Mumbai Central Warehouse"))
                    .thenReturn(false);
            when(locationRepository.save(any(Location.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            LocationResponse response = locationService.registerLocation(request);

            // then
            assertThat(response.lifecycleState()).isEqualTo("DRAFT");
        }

        @Test
        void shouldAcceptLocationCodeWHCHN_whenValid() {
            // given
            CreateLocationRequest request = new CreateLocationRequest(
                    "WH-CHN",
                    "Chennai Warehouse",
                    "STORE",
                    new CreateLocationRequest.AddressRequest("Street", "Chennai", "Tamil Nadu", "600001", "India"),
                    "Jane Doe"
            );
            when(locationRepository.existsByLocationCodeAndLocationName("WH-CHN", "Chennai Warehouse"))
                    .thenReturn(false);
            when(locationRepository.save(any(Location.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            LocationResponse response = locationService.registerLocation(request);

            // then
            assertThat(response.locationCode()).isEqualTo("WH-CHN");
            assertThat(response.locationType()).isEqualTo("STORE");
        }

        @Test
        void shouldAcceptLocationCodeWHBEN_whenValid() {
            // given
            CreateLocationRequest request = new CreateLocationRequest(
                    "WH-BEN",
                    "Bengaluru Hub",
                    "DARKSTORE",
                    new CreateLocationRequest.AddressRequest("Street", "Bengaluru", "Karnataka", "560001", "India"),
                    "Bob Smith"
            );
            when(locationRepository.existsByLocationCodeAndLocationName("WH-BEN", "Bengaluru Hub"))
                    .thenReturn(false);
            when(locationRepository.save(any(Location.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            LocationResponse response = locationService.registerLocation(request);

            // then
            assertThat(response.locationCode()).isEqualTo("WH-BEN");
            assertThat(response.locationType()).isEqualTo("DARKSTORE");
        }

        @Test
        void shouldAcceptThreePLNodeLocationType_whenValid() {
            // given
            CreateLocationRequest request = new CreateLocationRequest(
                    "WH-MUM",
                    "Mumbai 3PL Node",
                    "THREE_PL_NODE",
                    new CreateLocationRequest.AddressRequest("Street", "Mumbai", "Maharashtra", "400001", "India"),
                    "Alice"
            );
            when(locationRepository.existsByLocationCodeAndLocationName("WH-MUM", "Mumbai 3PL Node"))
                    .thenReturn(false);
            when(locationRepository.save(any(Location.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            LocationResponse response = locationService.registerLocation(request);

            // then
            assertThat(response.locationType()).isEqualTo("THREE_PL_NODE");
        }
    }

    @Nested
    class FailureCases {

        @Test
        void shouldThrowBusinessRuleException_whenLocationCodeIsInvalid() {
            // given
            CreateLocationRequest request = new CreateLocationRequest(
                    "INVALID-CODE",
                    "Test Warehouse",
                    "WAREHOUSE",
                    new CreateLocationRequest.AddressRequest("Street", "City", "State", "12345", "Country"),
                    "Contact"
            );

            // when / then
            assertThatThrownBy(() -> locationService.registerLocation(request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Invalid location code: INVALID-CODE")
                    .hasMessageContaining("WH-MUM, WH-CHN, WH-BEN");

            verify(locationRepository, never()).save(any());
        }

        @Test
        void shouldThrowBusinessRuleException_whenLocationTypeIsInvalid() {
            // given
            CreateLocationRequest request = new CreateLocationRequest(
                    "WH-MUM",
                    "Test Warehouse",
                    "INVALID_TYPE",
                    new CreateLocationRequest.AddressRequest("Street", "City", "State", "12345", "Country"),
                    "Contact"
            );

            // when / then
            assertThatThrownBy(() -> locationService.registerLocation(request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Invalid location type: INVALID_TYPE");

            verify(locationRepository, never()).save(any());
        }

        @Test
        void shouldThrowBusinessRuleException_whenDuplicateLocationExists() {
            // given
            CreateLocationRequest request = validRequest();
            when(locationRepository.existsByLocationCodeAndLocationName("WH-MUM", "Mumbai Central Warehouse"))
                    .thenReturn(true);

            // when / then
            assertThatThrownBy(() -> locationService.registerLocation(request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("already exists");

            verify(locationRepository, never()).save(any());
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void shouldPreserveOriginalExceptionCause_whenLocationCodeIsInvalid() {
            // given
            CreateLocationRequest request = new CreateLocationRequest(
                    "BAD-CODE",
                    "Test",
                    "WAREHOUSE",
                    new CreateLocationRequest.AddressRequest("Street", "City", "State", "12345", "Country"),
                    "Contact"
            );

            // when / then
            assertThatThrownBy(() -> locationService.registerLocation(request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasCauseInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldPreserveOriginalExceptionCause_whenLocationTypeIsInvalid() {
            // given
            CreateLocationRequest request = new CreateLocationRequest(
                    "WH-MUM",
                    "Test",
                    "UNKNOWN",
                    new CreateLocationRequest.AddressRequest("Street", "City", "State", "12345", "Country"),
                    "Contact"
            );

            // when / then
            assertThatThrownBy(() -> locationService.registerLocation(request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasCauseInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldSaveLocationWithAllAddressFields_whenValid() {
            // given
            CreateLocationRequest request = validRequest();
            when(locationRepository.existsByLocationCodeAndLocationName("WH-MUM", "Mumbai Central Warehouse"))
                    .thenReturn(false);
            when(locationRepository.save(any(Location.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            locationService.registerLocation(request);

            // then
            ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
            verify(locationRepository).save(captor.capture());
            Location saved = captor.getValue();
            assertThat(saved.getStreet()).isEqualTo("123 Industrial Area");
            assertThat(saved.getCity()).isEqualTo("Mumbai");
            assertThat(saved.getState()).isEqualTo("Maharashtra");
            assertThat(saved.getPostalCode()).isEqualTo("400001");
            assertThat(saved.getCountry()).isEqualTo("India");
            assertThat(saved.getContactPerson()).isEqualTo("John Doe");
            assertThat(saved.getLocationType()).isEqualTo(LocationType.WAREHOUSE);
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }
    }
}
