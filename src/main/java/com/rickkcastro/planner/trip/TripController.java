package com.rickkcastro.planner.trip;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rickkcastro.planner.activity.ActivityData;
import com.rickkcastro.planner.activity.ActivityRequestPayload;
import com.rickkcastro.planner.activity.ActivityResponse;
import com.rickkcastro.planner.activity.ActivityService;
import com.rickkcastro.planner.link.LinkData;
import com.rickkcastro.planner.link.LinkRequestPayload;
import com.rickkcastro.planner.link.LinkResponse;
import com.rickkcastro.planner.link.LinkService;
import com.rickkcastro.planner.participant.ParticipantCreateResponse;
import com.rickkcastro.planner.participant.ParticipantData;
import com.rickkcastro.planner.participant.ParticipantRequestPayload;
import com.rickkcastro.planner.participant.ParticipantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private LinkService linkService;

    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload) {
        Trip newTrip = new Trip(payload);

        this.tripRepository.save(newTrip);

        this.participantService.registerParticipantsToTrip(payload.emails_to_invite(), newTrip);

        return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payload) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();
            rawTrip.setDestination(payload.destination());
            rawTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));

            this.tripRepository.save(rawTrip);

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/confirm")
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();
            rawTrip.setIsConfirmed(true);

            this.tripRepository.save(rawTrip);
            this.participantService.triggerConfirmationEmailToParticipants(id);

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    // Participants

    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID id,
            @RequestBody ParticipantRequestPayload payload) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            ParticipantCreateResponse participantCreateResponse = this.participantService
                    .registerParticipantToEvent(payload.email(), rawTrip);

            if (rawTrip.getIsConfirmed()) {
                this.participantService.triggerConfirmationEmailToParticipant(payload.email());
            }

            return ResponseEntity.ok(participantCreateResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantData>> getParticipants(@PathVariable UUID id) {
        List<ParticipantData> participantsList = this.participantService.getAllParticipantsFromTrip(id);

        return ResponseEntity.ok(participantsList);
    }

    // Activities

    @PostMapping("/{id}/activities")
    public ResponseEntity<ActivityResponse> registerActivity(@PathVariable UUID id,
            @RequestBody ActivityRequestPayload payload) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            ActivityResponse activityResponse = this.activityService.saveActivity(payload, rawTrip);

            return ResponseEntity.ok(activityResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<List<ActivityData>> getActivities(@PathVariable UUID id) {
        List<ActivityData> activitiesList = this.activityService.getAllActivitiesFromTripId(id);

        return ResponseEntity.ok(activitiesList);
    }

    // Links

    @PostMapping("/{id}/links")
    public ResponseEntity<LinkResponse> registerLink(@PathVariable UUID id,
            @RequestBody LinkRequestPayload payload) {
        Optional<Trip> trip = this.tripRepository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            LinkResponse linkResponse = this.linkService.saveLink(payload, rawTrip);

            return ResponseEntity.ok(linkResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/links")
    public ResponseEntity<List<LinkData>> getLinks(@PathVariable UUID id) {
        List<LinkData> linkList = this.linkService.getAllLinksFromTripId(id);

        return ResponseEntity.ok(linkList);
    }

}
