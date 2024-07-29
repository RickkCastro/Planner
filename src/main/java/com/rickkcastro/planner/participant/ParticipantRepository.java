package com.rickkcastro.planner.participant;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, UUID> {

    public List<Participant> findByTripId(UUID tripId);
}
