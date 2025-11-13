package com.ninjaone.dundie_awards.eventlisteners;

import com.ninjaone.dundie_awards.events.AwardsEvent;
import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.service.ActivityService;
import com.ninjaone.dundie_awards.service.AwardsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class AwardsEventListenerTest {

    private ActivityService activityService;
    private AwardsService awardsService;

    private AwardsEventListener listener;

    @BeforeEach
    void setUp() {
        activityService = mock(ActivityService.class);
        awardsService = mock(AwardsService.class);
        listener = new AwardsEventListener(activityService, awardsService);
    }

    @Test
    void handleAwardsGivenToOrgMembers_success_createsActivity_andDoesNotCompensate() {
        long orgId = 42L;
        int numAwards = 5;
        AwardsEvent event = new AwardsEvent(this, numAwards, orgId);

        Activity act = new Activity();
        // we don't care about fields for this test, just ensure call does not throw
        when(activityService.createActivityForAwardsGiven(orgId, numAwards)).thenReturn(act);

        assertThatCode(() -> listener.handleAwardsGivenToOrgMembers(event))
                .doesNotThrowAnyException();

        verify(activityService).createActivityForAwardsGiven(orgId, numAwards);
        verify(awardsService, never()).compensateAwards(anyLong());
        verifyNoMoreInteractions(awardsService);
    }

    @Test
    void handleAwardsGivenToOrgMembers_failure_compensatesAwards() {
        long orgId = 7L;
        int numAwards = 3;
        AwardsEvent event = new AwardsEvent(this, numAwards, orgId);

        when(activityService.createActivityForAwardsGiven(orgId, numAwards))
                .thenThrow(new RuntimeException("activity creation failed"));
        when(awardsService.compensateAwards(orgId)).thenReturn(numAwards);

        assertThatCode(() -> listener.handleAwardsGivenToOrgMembers(event))
                .doesNotThrowAnyException();

        verify(activityService).createActivityForAwardsGiven(orgId, numAwards);
        verify(awardsService).compensateAwards(orgId);
        verifyNoMoreInteractions(awardsService);
    }
}
