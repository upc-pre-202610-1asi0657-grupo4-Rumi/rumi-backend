package com.rumi.buildingmanagement.infrastructure.web;

import com.rumi.buildingmanagement.domain.model.Building;
import com.rumi.buildingmanagement.infrastructure.web.dto.BuildingRequestDto;
import com.rumi.buildingmanagement.infrastructure.web.dto.BuildingResponseDto;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BuildingDtoMapperTest {

    @Test
    void mapsBetweenApiDtosAndTheBuildingDomainModel() {
        BuildingDtoMapper mapper = new BuildingDtoMapper();
        UUID buildingId = UUID.randomUUID();
        BuildingRequestDto request = new BuildingRequestDto("Rumi Tower", "Av. Arequipa 1234");

        Building building = mapper.toDomain(buildingId, request);
        BuildingResponseDto response = mapper.toResponse(building);

        assertThat(building.getId()).isEqualTo(buildingId);
        assertThat(building.getName()).isEqualTo(request.name());
        assertThat(building.getAddress()).isEqualTo(request.address());
        assertThat(response).isEqualTo(new BuildingResponseDto(
                buildingId,
                "Rumi Tower",
                "Av. Arequipa 1234"
        ));
    }
}
