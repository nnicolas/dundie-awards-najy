package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.events.AwardsEvent;
import com.ninjaone.dundie_awards.exception.FailedToGiveAwardsException;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.service.impl.AwardsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AwardsServiceImplTest {

    private EmployeeRepository employeeRepository;
    private ApplicationEventPublisher publisher;
    private AwardsCacheService awardsCacheService;

    private AwardsServiceImpl awardsService;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeRepository.class);
        publisher = mock(ApplicationEventPublisher.class);
        awardsCacheService = mock(AwardsCacheService.class);
        awardsService = new AwardsServiceImpl(employeeRepository, publisher, awardsCacheService);
    }

    @Test
    void giveAwards_success_incrementsRepoAndCache_andPublishesEvent_andReturnsCount() {
        long orgId = 7L;
        int expectedAwardsCount = 3;
        when(employeeRepository.incrementDundieAwardsForOrgEmployees(orgId)).thenReturn(expectedAwardsCount);

        int actualResult = awardsService.giveAwards(orgId);

        assertThat(actualResult).isEqualTo(expectedAwardsCount);

        //Verify Repo
        verify(employeeRepository).incrementDundieAwardsForOrgEmployees(orgId);

        //Verify Cache
        verify(awardsCacheService).incrementAllForOrg(orgId);

        //Verify publish event
        ArgumentCaptor<AwardsEvent> eventCaptor = ArgumentCaptor.forClass(AwardsEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        AwardsEvent evt = eventCaptor.getValue();
        assertThat(evt.getOrganizationId()).isEqualTo(orgId);
        assertThat(evt.getNumAwards()).isEqualTo(expectedAwardsCount);
        verifyNoMoreInteractions(awardsCacheService);
    }

    @Test
    void giveAwards_failureAfterIncrement_compensatesAndThrows() {
        long orgId = 9L;
        int resultCount = 2;
        when(employeeRepository.incrementDundieAwardsForOrgEmployees(orgId)).thenReturn(resultCount);
        doThrow(new RuntimeException("publish failed")).when(publisher).publishEvent(any());
        when(employeeRepository.decrementDundieAwardsForOrgEmployees(orgId)).thenReturn(resultCount);

        assertThatThrownBy(() -> awardsService.giveAwards(orgId))
                .isInstanceOf(FailedToGiveAwardsException.class);

        // Verify Repo, Service, Event
        verify(employeeRepository).incrementDundieAwardsForOrgEmployees(orgId);
        verify(awardsCacheService).incrementAllForOrg(orgId);
        verify(publisher).publishEvent(any());

        // Verify Compensation
        verify(employeeRepository).decrementDundieAwardsForOrgEmployees(orgId);
        verify(awardsCacheService).decrementAllForOrg(orgId);
    }

    @Test
    void giveAwards_failureBeforeIncrement_doesNotCompensate() {
        long orgId = 5L;
        when(employeeRepository.incrementDundieAwardsForOrgEmployees(orgId)).thenThrow(new RuntimeException("db fail"));

        assertThatThrownBy(() -> awardsService.giveAwards(orgId))
                .isInstanceOf(FailedToGiveAwardsException.class);

        verify(employeeRepository).incrementDundieAwardsForOrgEmployees(orgId);
        verify(awardsCacheService, never()).decrementAllForOrg(anyLong());
        verify(employeeRepository, never()).decrementDundieAwardsForOrgEmployees(anyLong());
    }

    @Test
    void giveAwards_cacheIncrementFails_compensatesDbOnly_andThrows() {
        long orgId = 11L;
        int changedRows = 4;
        when(employeeRepository.incrementDundieAwardsForOrgEmployees(orgId)).thenReturn(changedRows);
        doThrow(new RuntimeException("cache fail")).when(awardsCacheService).incrementAllForOrg(orgId);
        when(employeeRepository.decrementDundieAwardsForOrgEmployees(orgId)).thenReturn(changedRows);

        assertThatThrownBy(() -> awardsService.giveAwards(orgId))
                .isInstanceOf(FailedToGiveAwardsException.class);

        // DB increment happened
        verify(employeeRepository).incrementDundieAwardsForOrgEmployees(orgId);
        // Cache increment attempted and failed
        verify(awardsCacheService).incrementAllForOrg(orgId);
        // Compensation inside updateDbAndCache: only DB is compensated
        verify(employeeRepository).decrementDundieAwardsForOrgEmployees(orgId);
        verify(awardsCacheService, never()).decrementAllForOrg(anyLong());
        // No event should be published since updateDbAndCache didn't succeed
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void compensateAwards_decrementsRepoAndCache_andReturnsRows() {
        long orgId = 3L;
        when(employeeRepository.decrementDundieAwardsForOrgEmployees(orgId)).thenReturn(4);
        int rows = awardsService.compensateAwardsInDbAndCache(orgId);
        assertThat(rows).isEqualTo(4);
        verify(employeeRepository).decrementDundieAwardsForOrgEmployees(orgId);
        verify(awardsCacheService).decrementAllForOrg(orgId);
    }
}
