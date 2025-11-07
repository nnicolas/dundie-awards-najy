package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.model.Activity;

public interface ActivityService {

    Activity createActivityForAwardsGiven(long organizationId, int numAwards);
}
