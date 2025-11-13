package com.ninjaone.dundie_awards.service;

/**
 * In-memory cache mapping employeeId -> dundieAwards.
 * The cache is initialized from the database in a single transaction to ensure an exact snapshot
 * and is kept in sync with award operations and employee lifecycle changes.
 */
public interface AwardsCacheService {

    /**
     * Initialize the cache from the database within a single transaction to ensure
     * the in-memory data matches the persisted state.
     */
    void initializeFromDatabase();

    /**
     * Increment dundie awards by 1 for all employees in the given organization.
     */
    void incrementAllForOrg(long organizationId);

    /**
     * Decrement dundie awards by 1 for all employees in the given organization.
     */
    void decrementAllForOrg(long organizationId);

    /**
     * Add a new employee to the cache.
     */
    void addEmployee(long orgId, long employeeId, int initialAwards);

    /**
     * Remove an employee from the cache.
     */
    void removeEmployee(long orgId, long employeeId);
}
