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
     * Gives Dundee awards to all employees of a given organization, update cache, and publishes an AwardsEvent.
     * The AwardsEvent is handled asynchronously in the AwardsEventListener
     * ------------
     * Either all 3 of DB, cache, and create event are successful or non go thru.
     *
     * If Updating DB or cache fails, we throw an exception.
     * If publishing event fails, we rollback the compensations in DB and cache
     *
     */
    public int giveAwards(long organizationId) {

        Integer changedRows = null;
        try {
            changedRows = updateDbAndCache(organizationId);
            publisher.publishEvent(new AwardsEvent(this, changedRows, organizationId));
            Logger.getGlobal().log(Level.INFO, String.format("Awards given and event published for organizationId: %d", organizationId));
            return changedRows;
        } catch (Exception e) {
            if(changedRows != null) {
                this.compensateAwardsInDbAndCache(organizationId);
            }
            Logger.getGlobal().log(Level.SEVERE, String.format("Failed to submit Awards for organizationId: %d", organizationId));
            throw new FailedToGiveAwardsException(e);
        }
    }

    /**
        This method will update db and cache and return the number of updated records
        It either updates both DB and cache or updates neither of them.

        - If updating DB fails, it throws an exception
        - If updating cache fails, we compensate the DB update and throw an exception
     */
    private Integer updateDbAndCache(long organizationId) {
        Integer changedRows = null;
        try {
            changedRows = employeeRepository.incrementDundieAwardsForOrgEmployees(organizationId);
            awardsCacheService.incrementAllForOrg(organizationId);
            return changedRows;
        } catch (Exception e) {
            if(changedRows == null) {
                throw new RuntimeException(String.format("Failed to update Awards for organizationId: %d in DB, No compensation needed", organizationId));
            }
            int compensatedDbRows = compensateAwardsInDb(organizationId);
            throw new RuntimeException(String.format("Failed to update Awards for organizationId: %d in cache. Compensation needed for %d rows", organizationId, compensatedDbRows));
        }
    }

    public int compensateAwardsInDbAndCache(long organizationId) {
        int rows = employeeRepository.decrementDundieAwardsForOrgEmployees(organizationId);
        awardsCacheService.decrementAllForOrg(organizationId);
        return rows;
    }

    public int compensateAwardsInDb(long organizationId) {
        return employeeRepository.decrementDundieAwardsForOrgEmployees(organizationId);
    }
}
