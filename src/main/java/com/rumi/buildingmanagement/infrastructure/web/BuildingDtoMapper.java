package com.rumi.buildingmanagement.infrastructure.web;

import com.rumi.buildingmanagement.domain.model.Building;
import com.rumi.buildingmanagement.infrastructure.web.dto.BuildingRequestDto;
import com.rumi.buildingmanagement.infrastructure.web.dto.BuildingResponseDto;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BuildingDtoMapper {

    public Building toDomain(UUID buildingId, BuildingRequestDto request) {
        return new Building(
                buildingId,
                request.name(),
                request.address()
        );
    }

    public BuildingResponseDto toResponse(Building building) {
        return new BuildingResponseDto(
                building.getId(),
                building.getName(),
                building.getAddress()
        );
    }
}
