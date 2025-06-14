package com.gestion.demo.repository;

import com.gestion.demo.model.Pub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PubRepository extends JpaRepository<Pub, Long> {
    List<Pub> findByClassesId(Long classeId);
} 