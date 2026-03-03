package com.inventory.demo.location.api;

import com.inventory.demo.location.domain.LifecycleState;
import com.inventory.demo.location.domain.Location;
import com.inventory.demo.location.domain.LocationType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationResponseTest {

    @Test
    void shouldMapAllFieldsFromEntity() {
        // given
        Location location = Location.create(
                "WH-MUM", "Mumbai Warehouse", LocationType.WAREHOUSE,
                "123 Industrial Area", "Mumbai", "Maharashtra",
                "400001", "India", "John Doe"
        );

        // when
        LocationResponse response = LocationResponse.fromEntity(location);

        // then
        assertThat(response.locationCode()).isEqualTo("WH-MUM");
        assertThat(response.locationName()).isEqualTo("Mumbai Warehouse");
        assertThat(response.locationType()).isEqualTo("WAREHOUSE");
        assertThat(response.lifecycleState()).isEqualTo("DRAFT");
        assertThat(response.contactPerson()).isEqualTo("John Doe");
        assertThat(response.address().street()).isEqualTo("123 Industrial Area");
        assertThat(response.address().city()).isEqualTo("Mumbai");
        assertThat(response.address().state()).isEqualTo("Maharashtra");
        assertThat(response.address().postalCode()).isEqualTo("400001");
        assertThat(response.address().country()).isEqualTo("India");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    void shouldMapDarkstoreLocationType() {
        // given
        Location location = Location.create(
                "WH-BEN", "Bengaluru Darkstore", LocationType.DARKSTORE,
                "456 Street", "Bengaluru", "Karnataka",
                "560001", "India", "Alice"
        );

        // when
        LocationResponse response = LocationResponse.fromEntity(location);

        // then
        assertThat(response.locationType()).isEqualTo("DARKSTORE");
        assertThat(response.lifecycleState()).isEqualTo(LifecycleState.DRAFT.name());
    }
}
