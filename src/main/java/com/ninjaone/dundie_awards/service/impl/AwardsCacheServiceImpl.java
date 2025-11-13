package com.ninjaone.dundie_awards.service.impl;

import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.service.AwardsCacheService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory cache mapping orgId -> (employeeId -> dundieAwards).
 * Initialized from DB in a single read-only transaction at application startup.
 */
@Service
public class AwardsCacheServiceImpl implements AwardsCacheService {

    private final EmployeeRepository employeeRepository;

    // orgId -> (employeeId -> awards)
    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, Integer>> cache = new ConcurrentHashMap<>();

    public AwardsCacheServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        initializeFromDatabase();
    }

    @Override
    @Transactional(readOnly = true)
    public void initializeFromDatabase() {
        // Build a brand new structure from the current DB snapshot to ensure exact copy
        List<Employee> all = employeeRepository.findAll();
        Map<Long, Map<Long, Integer>> grouped = all.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getOrganization().getId(),
                        Collectors.toMap(Employee::getId, Employee::getDundieAwards)));

        // Replace in-memory data atomically by rebuilding the top-level map
        cache.clear();
        grouped.forEach((orgId, empMap) -> {
            ConcurrentHashMap<Long, Integer> inner = new ConcurrentHashMap<>(empMap);
            cache.put(orgId, inner);
        });
    }


    @Override
    public void incrementAllForOrg(long organizationId) {
        ConcurrentHashMap<Long, Integer> userAwardsMap = cache.get(organizationId);
        if (userAwardsMap == null) {
            return;
        }
        userAwardsMap.replaceAll((empId, awards) -> awards + 1);
    }

    @Override
    public void decrementAllForOrg(long organizationId) {
        ConcurrentHashMap<Long, Integer> userAwardsMap = cache.get(organizationId);
        if (userAwardsMap == null) {
            return;
        }
        userAwardsMap.replaceAll((empId, awards) -> awards - 1);
    }

    @Override
    public void addEmployee(long orgId, long employeeId, int initialAwards) {
        cache.putIfAbsent(orgId, new ConcurrentHashMap<>());
        cache.get(orgId).put(employeeId, initialAwards);
    }

    @Override
    public void removeEmployee(long orgId, long employeeId) {
        cache.computeIfAbsent(orgId, k -> new ConcurrentHashMap<>()).remove(employeeId);
    }
}
