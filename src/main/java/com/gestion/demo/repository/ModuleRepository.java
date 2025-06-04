package com.gestion.demo.repository;

import com.gestion.demo.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM formation_module WHERE module_id = :moduleId", nativeQuery = true)
    void deleteModuleAssociations(@Param("moduleId") Long moduleId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM modules WHERE id = :moduleId", nativeQuery = true)
    void deleteModuleById(@Param("moduleId") Long moduleId);
} 