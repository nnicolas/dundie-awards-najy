package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.ActivityDto;
import com.ninjaone.dundie_awards.model.Activity;

import java.util.List;

public interface ActivityService {

    Activity createActivityForAwardsGiven(long organizationId, int numAwards);

    List<ActivityDto> getAllActivities();
}
