package com.routine.pusher.data.repository;

import com.routine.pusher.data.model.entities.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Long> { }
