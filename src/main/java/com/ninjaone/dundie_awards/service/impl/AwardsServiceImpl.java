package com.ninjaone.dundie_awards.service.impl;

import com.ninjaone.dundie_awards.events.AwardsEvent;
import com.ninjaone.dundie_awards.exception.FailedToGiveAwardsException;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.service.AwardsService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AwardsServiceImpl implements AwardsService {

    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher publisher;

    public AwardsServiceImpl(EmployeeRepository employeeRepository, ApplicationEventPublisher publisher) {
        this.employeeRepository = employeeRepository;
        this.publisher = publisher;
    }

    /**
     * Gives Dundee awards to all employees of a given organization and publishes an AwardsEvent.
     * This method is Transactional:
     *  - If publishing the AwardsEvent fails, the increment is rolled back
     *  - A FailedToGiveAwardsException is thrown.
     * The AwardsEvent is handled asynchronously in the AwardsEventListener
     *
     * @param organizationId the ID of the organization whose employees will receive the awards
     * @return the number of rows updated in the database, representing the number of employees who received awards
     * @throws FailedToGiveAwardsException if the operation fails during execution
     */
    @Transactional
    public int giveAwards(long organizationId) {
        int changedRows;
        try {
            changedRows = employeeRepository.incrementDundieAwardsForOrgEmployees(organizationId);
            publisher.publishEvent(new AwardsEvent(this, changedRows, organizationId));
            Logger.getGlobal().log(Level.INFO, String.format("Awards given and event published for organizationId: %d", organizationId));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, String.format("Failed to submit Awards for organizationId: %d", organizationId));
            throw new FailedToGiveAwardsException(e);
        }

        return changedRows;
    }

    @Transactional
    public int compensateAwards(long organizationId) {
        return employeeRepository.decrementDundieAwardsForOrgEmployees(organizationId);
    }
}
