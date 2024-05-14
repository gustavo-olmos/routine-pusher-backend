package com.routine.pusher.repository;

import com.routine.pusher.model.dto.LembreteDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<LembreteDTO, Long> { }
