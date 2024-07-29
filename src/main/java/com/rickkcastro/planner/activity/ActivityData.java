package com.rickkcastro.planner.activity;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActivityData(UUID id, String title, LocalDateTime occuts_at) {

}
