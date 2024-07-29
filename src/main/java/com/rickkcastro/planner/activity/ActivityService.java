package com.rickkcastro.planner.activity;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rickkcastro.planner.trip.Trip;

@Service
public class ActivityService {

    @Autowired
    ActivityRepository repository;

    public ActivityResponse saveActivity(ActivityRequestPayload payload, Trip trip) {
        Activity newActivity = new Activity(payload.title(), payload.occurs_at(), trip);

        this.repository.save(newActivity);

        return new ActivityResponse(newActivity.getId());
    }

    public List<ActivityData> getAllActivitiesFromTripId(UUID tripId) {
        return this.repository.findByTripId(tripId).stream()
                .map(activity -> new ActivityData(activity.getId(), activity.getTitle(), activity.getOccursAt()))
                .toList();
    }
}
