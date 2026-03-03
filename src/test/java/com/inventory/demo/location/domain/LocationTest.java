package com.inventory.demo.location.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class LocationTest {

    @Test
    void shouldCreateLocationInDraftState() {
        // when
        Location location = Location.create(
                "WH-MUM", "Mumbai Warehouse", LocationType.WAREHOUSE,
                "Street 1", "Mumbai", "Maharashtra", "400001", "India", "John Doe"
        );

        // then
        assertThat(location.getLocationCode()).isEqualTo("WH-MUM");
        assertThat(location.getLocationName()).isEqualTo("Mumbai Warehouse");
        assertThat(location.getLocationType()).isEqualTo(LocationType.WAREHOUSE);
        assertThat(location.getLifecycleState()).isEqualTo(LifecycleState.DRAFT);
        assertThat(location.getStreet()).isEqualTo("Street 1");
        assertThat(location.getCity()).isEqualTo("Mumbai");
        assertThat(location.getState()).isEqualTo("Maharashtra");
        assertThat(location.getPostalCode()).isEqualTo("400001");
        assertThat(location.getCountry()).isEqualTo("India");
        assertThat(location.getContactPerson()).isEqualTo("John Doe");
        assertThat(location.getCreatedAt()).isNotNull();
        assertThat(location.getUpdatedAt()).isNotNull();
        assertThat(location.getId()).isNull(); // not persisted yet
    }

    @Test
    void shouldSetTimestampsOnCreation() {
        // when
        Location location = Location.create(
                "WH-CHN", "Chennai Hub", LocationType.STORE,
                "Road 1", "Chennai", "Tamil Nadu", "600001", "India", "Jane"
        );

        // then
        assertThat(location.getCreatedAt()).isNotNull();
        assertThat(location.getUpdatedAt()).isNotNull();
        assertThat(location.getCreatedAt()).isCloseTo(location.getUpdatedAt(), within(1, java.time.temporal.ChronoUnit.SECONDS));
    }
}
