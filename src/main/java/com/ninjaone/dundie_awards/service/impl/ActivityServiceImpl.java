package com.ninjaone.dundie_awards.service.impl;

import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import com.ninjaone.dundie_awards.service.ActivityService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ActivityServiceImpl implements ActivityService {


    private final ActivityRepository activityRepository;

    public ActivityServiceImpl(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public Activity createActivityForAwardsGiven(long organizationId, int numAwards) {
        return activityRepository.save(new Activity(LocalDateTime.now(), String.format("OrgId: %d given %d awards", organizationId, numAwards)));
    }
}
