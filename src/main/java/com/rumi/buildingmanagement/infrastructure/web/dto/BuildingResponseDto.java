package com.rumi.buildingmanagement.infrastructure.web.dto;

import java.util.UUID;

public record BuildingResponseDto(
        UUID id,
        String name,
        String address
) {
}
