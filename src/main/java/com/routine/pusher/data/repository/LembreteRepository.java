package com.routine.pusher.data.repository;

import com.routine.pusher.data.model.entities.LembreteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LembreteRepository extends JpaRepository<LembreteEntity, Long>{ }
