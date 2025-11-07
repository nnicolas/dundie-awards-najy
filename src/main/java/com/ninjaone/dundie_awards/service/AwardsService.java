package com.ninjaone.dundie_awards.service;

public interface AwardsService {

    int giveAwards(long organizationId);

    int rollbackAwards(long organizationId);
}
