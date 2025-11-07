package com.ninjaone.dundie_awards.dto;


import java.time.LocalDateTime;

public class ActivityDto {

    private long id;

    private LocalDateTime occurredAt;

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
