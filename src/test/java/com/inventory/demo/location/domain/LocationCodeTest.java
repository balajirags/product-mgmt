package com.inventory.demo.location.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocationCodeTest {

    @Nested
    class FromCodeTests {

        @Test
        void shouldReturnWHMUM_whenCodeIsWHMUM() {
            // when
            LocationCode result = LocationCode.fromCode("WH-MUM");

            // then
            assertThat(result).isEqualTo(LocationCode.WH_MUM);
            assertThat(result.getCode()).isEqualTo("WH-MUM");
        }

        @Test
        void shouldReturnWHCHN_whenCodeIsWHCHN() {
            // when
            LocationCode result = LocationCode.fromCode("WH-CHN");

            // then
            assertThat(result).isEqualTo(LocationCode.WH_CHN);
            assertThat(result.getCode()).isEqualTo("WH-CHN");
        }

        @Test
        void shouldReturnWHBEN_whenCodeIsWHBEN() {
            // when
            LocationCode result = LocationCode.fromCode("WH-BEN");

            // then
            assertThat(result).isEqualTo(LocationCode.WH_BEN);
            assertThat(result.getCode()).isEqualTo("WH-BEN");
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "WH-DEL", "", "wh-mum"})
        void shouldThrowIllegalArgumentException_whenCodeIsInvalid(String invalidCode) {
            // when / then
            assertThatThrownBy(() -> LocationCode.fromCode(invalidCode))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown location code: " + invalidCode);
        }
    }

    @Nested
    class GetCodeTests {

        @Test
        void shouldReturnAllCodes() {
            // then
            assertThat(LocationCode.values()).hasSize(3);
            assertThat(LocationCode.WH_MUM.getCode()).isEqualTo("WH-MUM");
            assertThat(LocationCode.WH_CHN.getCode()).isEqualTo("WH-CHN");
            assertThat(LocationCode.WH_BEN.getCode()).isEqualTo("WH-BEN");
        }
    }
}
