package com.ninjaone.dundie_awards.dto;

public class GiveAwardsResponse {

    private long organizationId;
    private int awardsGiven;

    public GiveAwardsResponse(long organizationId, int awardsGiven) {
        this.organizationId = organizationId;
        this.awardsGiven = awardsGiven;
    }

    public int getAwardsGiven() {
        return awardsGiven;
    }

    public long getOrganizationId() {
        return organizationId;
    }
}
