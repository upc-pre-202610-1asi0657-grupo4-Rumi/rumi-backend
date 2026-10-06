package com.rumi.buildingmanagement.application;

import com.rumi.buildingmanagement.domain.model.Building;
import com.rumi.buildingmanagement.domain.repository.BuildingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BuildingApplicationService {

    private final BuildingRepository buildingRepository;

    public BuildingApplicationService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    public Optional<Building> findBuildingById(UUID buildingId) {
        return buildingRepository.findById(buildingId);
    }

    @Transactional
    public Building registerBuilding(Building building) {
        return buildingRepository.save(building);
    }
}
