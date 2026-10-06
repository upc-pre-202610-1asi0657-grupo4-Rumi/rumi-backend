package com.rumi.buildingmanagement.infrastructure.web;

import com.rumi.buildingmanagement.application.BuildingApplicationService;
import com.rumi.buildingmanagement.domain.model.Building;
import com.rumi.buildingmanagement.infrastructure.web.dto.BuildingRequestDto;
import com.rumi.buildingmanagement.infrastructure.web.dto.BuildingResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/buildings")
public class BuildingController {

    private final BuildingApplicationService buildingService;
    private final BuildingDtoMapper mapper;

    public BuildingController(BuildingApplicationService buildingService, BuildingDtoMapper mapper) {
        this.buildingService = buildingService;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BuildingResponseDto registerBuilding(@RequestBody BuildingRequestDto request) {
        Building building = mapper.toDomain(UUID.randomUUID(), request);
        Building savedBuilding = buildingService.registerBuilding(building);
        return mapper.toResponse(savedBuilding);
    }
}
