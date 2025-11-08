package com.ninjaone.dundie_awards.dto;


import com.ninjaone.dundie_awards.common.Constraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ActivityDto {

    private long id;

    @NotNull
    private LocalDateTime occurredAt;

    @NotBlank
    @Size(min= Constraints.EVENT_DESC_MIN, max = Constraints.EVENT_DESC_MAX, message = "Event Description must be between " + Constraints.EVENT_DESC_MIN + " and " + Constraints.EVENT_DESC_MAX + " characters")
    private String event;

    public ActivityDto() {

    }

    public ActivityDto(LocalDateTime occurredAt, String event) {
        super();
        this.occurredAt = occurredAt;
        this.event = event;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

}
