package com.ninjaone.dundie_awards.service.impl;

import com.ninjaone.dundie_awards.events.AwardsEvent;
import com.ninjaone.dundie_awards.exception.FailedToGiveAwardsException;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.service.AwardsCacheService;
import com.ninjaone.dundie_awards.service.AwardsService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AwardsServiceImpl implements AwardsService {

    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher publisher;
    private final AwardsCacheService awardsCacheService;

    public AwardsServiceImpl(EmployeeRepository employeeRepository, ApplicationEventPublisher publisher, AwardsCacheService awardsCacheService) {
        this.employeeRepository = employeeRepository;
        this.publisher = publisher;
        this.awardsCacheService = awardsCacheService;
    }

    /**
     * Gives Dundee awards to all employees of a given organization and publishes an AwardsEvent.
     * The AwardsEvent is handled asynchronously in the AwardsEventListener
     * ------------
     * If incrementDundieAwardsForOrgEmployees we will return without calling the compensateAwards
     * If publishing the AwardsEvent fails, it will call compensateAwards to decrement the awards that have been given.
     *
     */
    public int giveAwards(long organizationId) {

        Integer changedRows = null;
        try {
            changedRows = employeeRepository.incrementDundieAwardsForOrgEmployees(organizationId);
            // Update in-memory cache to reflect DB change
            awardsCacheService.incrementAllForOrg(organizationId);
            publisher.publishEvent(new AwardsEvent(this, changedRows, organizationId));
            Logger.getGlobal().log(Level.INFO, String.format("Awards given and event published for organizationId: %d", organizationId));
            return changedRows;
        } catch (Exception e) {
            if(changedRows != null) {
                this.compensateAwards(organizationId);
            }
            Logger.getGlobal().log(Level.SEVERE, String.format("Failed to submit Awards for organizationId: %d", organizationId));
            throw new FailedToGiveAwardsException(e);
        }
    }

    public int compensateAwards(long organizationId) {
        // First revert in DB
        int rows = employeeRepository.decrementDundieAwardsForOrgEmployees(organizationId);
        // Then update cache to stay in sync
        awardsCacheService.decrementAllForOrg(organizationId);
        return rows;
    }
}
