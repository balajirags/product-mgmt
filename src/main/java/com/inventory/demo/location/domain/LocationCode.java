package com.inventory.demo.location.domain;

/**
 * Predefined location codes representing physical fulfillment sites.
 */
public enum LocationCode {
    WH_MUM("WH-MUM"),
    WH_CHN("WH-CHN"),
    WH_BEN("WH-BEN");

    private final String code;

    LocationCode(String code) {
        this.code = code;
    }

    /**
     * Returns the string representation of the location code.
     *
     * @return the location code string (e.g., "WH-MUM")
     */
    public String getCode() {
        return code;
    }

    /**
     * Resolves a LocationCode from its string representation.
     *
     * @param code the location code string
     * @return the corresponding LocationCode enum value
     * @throws IllegalArgumentException if the code is not recognized
     */
    public static LocationCode fromCode(String code) {
        for (LocationCode locationCode : values()) {
            if (locationCode.code.equals(code)) {
                return locationCode;
            }
        }
        throw new IllegalArgumentException("Unknown location code: " + code);
    }
}
