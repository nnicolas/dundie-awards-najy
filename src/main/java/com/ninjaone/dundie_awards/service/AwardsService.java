package com.ninjaone.dundie_awards.service;

public interface AwardsService {

    int giveAwards(long organizationId);

    int compensateAwards(long organizationId);
}
