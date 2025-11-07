package com.ninjaone.dundie_awards.dto;


import java.time.LocalDateTime;

public class ActivityDto {

    private long id;

    private LocalDateTime occuredAt;

    private String event;

    public ActivityDto() {

    }

    public ActivityDto(LocalDateTime occuredAt, String event) {
        super();
        this.occuredAt = occuredAt;
        this.event = event;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getOccuredAt() {
        return occuredAt;
    }

    public void setOccuredAt(LocalDateTime occuredAt) {
        this.occuredAt = occuredAt;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

}
