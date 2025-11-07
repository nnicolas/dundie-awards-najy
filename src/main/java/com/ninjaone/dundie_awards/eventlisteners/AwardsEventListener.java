package com.ninjaone.dundie_awards.eventlisteners;

import com.ninjaone.dundie_awards.events.AwardsEvent;
import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.service.ActivityService;
import com.ninjaone.dundie_awards.service.AwardsService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class AwardsEventListener {

    private final ActivityService activityService;
    private final AwardsService awardsService;

    public AwardsEventListener(ActivityService activityService, AwardsService awardsService) {
        this.activityService = activityService;
        this.awardsService = awardsService;
    }

    @EventListener
    public void handleAwardsGivenToOrgMembers(AwardsEvent event) {
        try {
            Activity activity = activityService.createActivityForAwardsGiven(event.getOrganizationId(), event.getNumAwards());
            Logger.getGlobal().log(Level.INFO, String.format("Activity created for awards given event %s", activity.getEvent()));
        } catch (Exception e) {
            int rolledBackAwards = awardsService.rollbackAwards(event.getOrganizationId());
            Logger.getGlobal().log(Level.SEVERE, String.format("Error creating activity for orgId=%d. Rolled back %d awards. Details: %s %n", event.getOrganizationId(), rolledBackAwards, e.getMessage()), e);
        }
    }
}