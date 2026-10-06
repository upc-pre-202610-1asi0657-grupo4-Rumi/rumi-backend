package com.rumi.buildingmanagement.application;

import com.rumi.buildingmanagement.domain.model.Building;
import com.rumi.buildingmanagement.domain.repository.BuildingRepository;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BuildingApplicationServiceTest {

    @Test
    void registersAndFindsABuildingThroughTheDomainRepository() {
        BuildingRepository repository = new InMemoryBuildingRepository();
        BuildingApplicationService service = new BuildingApplicationService(repository);
        Building building = new Building(UUID.randomUUID(), "Rumi Tower", "Av. Arequipa 1234");

        service.registerBuilding(building);

        assertThat(service.findBuildingById(building.getId())).containsSame(building);
    }

    private static final class InMemoryBuildingRepository implements BuildingRepository {

        private final Map<UUID, Building> buildings = new HashMap<>();

        @Override
        public Optional<Building> findById(UUID id) {
            return Optional.ofNullable(buildings.get(id));
        }

        @Override
        public Building save(Building building) {
            buildings.put(building.getId(), building);
            return building;
        }

        @Override
        public boolean existsById(UUID id) {
            return buildings.containsKey(id);
        }
    }
}
