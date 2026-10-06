package com.rumi.buildingmanagement.infrastructure.persistence;

import com.rumi.buildingmanagement.domain.model.Building;
import com.rumi.buildingmanagement.domain.repository.BuildingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaBuildingRepository implements BuildingRepository {

    private final SpringDataBuildingRepository springDataRepository;

    public JpaBuildingRepository(SpringDataBuildingRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<Building> findById(UUID id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Building save(Building building) {
        BuildingJpaEntity savedEntity = springDataRepository.save(toEntity(building));
        return toDomain(savedEntity);
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataRepository.existsById(id);
    }

    private BuildingJpaEntity toEntity(Building building) {
        return new BuildingJpaEntity(
                building.getId(),
                building.getName(),
                building.getAddress()
        );
    }

    private Building toDomain(BuildingJpaEntity entity) {
        return new Building(
                entity.getId(),
                entity.getName(),
                entity.getAddress()
        );
    }
}
