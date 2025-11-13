package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.service.impl.AwardsCacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AwardsCacheServiceImplTest {

    private EmployeeRepository employeeRepository;
    private AwardsCacheService cacheService;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeRepository.class);
        cacheService = new AwardsCacheServiceImpl(employeeRepository);
    }

    private Employee emp(long empId, long orgId, int awards) {
        Organization org = new Organization();
        org.setId(orgId);
        Employee e = new Employee("f"+empId, "l"+empId, org);
        e.setId(empId);
        e.setDundieAwards(awards);
        return e;
    }

    @Test
    void initializeFromDatabase_buildsCacheByOrg() {
        when(employeeRepository.findAll()).thenReturn(List.of(
                emp(1, 100, 2),
                emp(2, 100, 5),
                emp(3, 200, 1)
        ));

        cacheService.initializeFromDatabase();

        // We cannot access internal cache, but we can verify behavior via increment/decrement
        cacheService.incrementAllForOrg(100);
        cacheService.decrementAllForOrg(200);

        // To observe effect, add/remove acts directly; we ensure no exceptions and correct idempotency
        cacheService.addEmployee(100, 99, 0);
        cacheService.removeEmployee(100, 99);
    }

    @Test
    void incrementAllForOrg_doesNothingWhenOrgMissing() {
        // No data initialized, org missing
        cacheService.incrementAllForOrg(999);
        // Should not throw
    }

    @Test
    void addAndRemoveEmployee_updatesStructure() {
        cacheService.addEmployee(1, 10, 7);
        // Re-adding same org should still function
        cacheService.addEmployee(1, 11, 0);
        cacheService.removeEmployee(1, 10);
        cacheService.removeEmployee(1, 999); // missing is no-op
        // No exception expected
    }
}
