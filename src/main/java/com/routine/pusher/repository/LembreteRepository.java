package com.routine.pusher.repository;

import com.routine.pusher.model.entities.LembreteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LembreteRepository extends JpaRepository<LembreteEntity, Long>{ }
