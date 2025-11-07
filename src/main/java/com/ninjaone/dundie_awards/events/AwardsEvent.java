package com.ninjaone.dundie_awards.events;

import org.springframework.context.ApplicationEvent;

public class AwardsEvent extends ApplicationEvent {

    private final long organizationId;
    private final int numAwards;

    public AwardsEvent(Object source, int numAwards, long organizationId) {
        super(source);
        this.numAwards = numAwards;
        this.organizationId = organizationId;
    }


    public long getOrganizationId() {
        return organizationId;
    }

    public int getNumAwards() {
        return numAwards;
    }
}