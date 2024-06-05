package com.routine.pusher.repository;

import com.routine.pusher.model.entities.SubtarefaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubTarefaRepository extends JpaRepository<SubtarefaEntity, Long> { }
