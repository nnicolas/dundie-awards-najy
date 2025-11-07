package com.ninjaone.dundie_awards.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    @Column(name = "event")
    private String event;

    public Activity() {

    }

    public Activity(LocalDateTime occurredAt, String event) {
        super();
        this.occurredAt = occurredAt;
        this.event = event;
    }

    public long getId() {
        return this.id;
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
        return this.event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

}
