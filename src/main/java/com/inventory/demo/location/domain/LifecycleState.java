package com.inventory.demo.location.domain;

/**
 * Represents the lifecycle state of a location.
 * Locations are always created in DRAFT state.
 */
public enum LifecycleState {
    DRAFT,
    ACTIVE,
    INACTIVE
}
