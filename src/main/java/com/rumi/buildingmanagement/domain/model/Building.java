package com.rumi.buildingmanagement.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class Building {

    private final UUID id;
    private final String name;
    private final String address;

    public Building(UUID id, String name, String address) {
        this.id = Objects.requireNonNull(id, "Building id is required");
        this.name = requireText(name, "Building name is required");
        this.address = requireText(address, "Building address is required");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
