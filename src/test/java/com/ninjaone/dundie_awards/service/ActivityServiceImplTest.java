package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.ActivityDto;
import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import com.ninjaone.dundie_awards.service.impl.ActivityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ActivityServiceImplTest {

    private ActivityRepository activityRepository;
    private ActivityService activityService;

    @BeforeEach
    void setUp() {
        activityRepository = Mockito.mock(ActivityRepository.class);
        activityService = new ActivityServiceImpl(activityRepository);
    }

    @Test
    void createActivityForAwardsGiven_persistsWithFormattedMessage() {
        // Arrange
        long orgId = 42L;
        int numAwards = 5;
        Activity saved = new Activity(LocalDateTime.now(), "");
        saved.setId(10L);
        when(activityRepository.save(any(Activity.class))).thenReturn(saved);

        // Act
        Activity result = activityService.createActivityForAwardsGiven(orgId, numAwards);

        // Assert
        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        verify(activityRepository).save(captor.capture());
        Activity toSave = captor.getValue();
        assertThat(toSave.getEvent()).isEqualTo("OrgId: 42 given 5 awards");
        assertThat(toSave.getOccurredAt()).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void getAllActivities_mapsEntitiesToDtos() {
        // Arrange
        Activity a1 = new Activity(LocalDateTime.now(), "E1");
        a1.setId(1L);
        Activity a2 = new Activity(LocalDateTime.now(), "E2");
        a2.setId(2L);
        when(activityRepository.findAll()).thenReturn(List.of(a1, a2));

        // Act
        List<ActivityDto> dtos = activityService.getAllActivities();

        // Assert
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getId()).isEqualTo(1L);
        assertThat(dtos.get(0).getEvent()).isEqualTo("E1");
        assertThat(dtos.get(1).getId()).isEqualTo(2L);
        assertThat(dtos.get(1).getEvent()).isEqualTo("E2");
    }
}
