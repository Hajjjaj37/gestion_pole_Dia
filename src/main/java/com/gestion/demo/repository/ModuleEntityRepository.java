package com.gestion.demo.repository;

import com.gestion.demo.model.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleEntityRepository extends JpaRepository<ModuleEntity, Long> {
} 