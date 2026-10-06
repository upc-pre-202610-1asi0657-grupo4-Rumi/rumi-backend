package com.rumi.buildingmanagement.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataBuildingRepository extends JpaRepository<BuildingJpaEntity, UUID> {
}
