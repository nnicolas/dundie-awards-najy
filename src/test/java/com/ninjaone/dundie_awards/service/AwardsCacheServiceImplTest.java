package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.service.impl.AwardsCacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        Employee e = new Employee("f" + empId, "l" + empId, org);
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

        // Assert initial snapshot
        assertThat(cacheService.getEmployeeAwards(100, 1)).isEqualTo(2);
        assertThat(cacheService.getEmployeeAwards(100, 2)).isEqualTo(5);
        assertThat(cacheService.getEmployeeAwards(200, 3)).isEqualTo(1);

        // Verify increment/decrement impact using accessor
        cacheService.incrementAllForOrg(100);
        cacheService.decrementAllForOrg(200);

        assertThat(cacheService.getEmployeeAwards(100, 1)).isEqualTo(3);
        assertThat(cacheService.getEmployeeAwards(100, 2)).isEqualTo(6);
        assertThat(cacheService.getEmployeeAwards(200, 3)).isEqualTo(0);

        // Add and remove employee and check values
        cacheService.addEmployee(100, 99, 0);
        assertThat(cacheService.getEmployeeAwards(100, 99)).isZero();
        cacheService.removeEmployee(100, 99);
        // After removal, unknown returns 0
        assertThat(cacheService.getEmployeeAwards(100, 99)).isZero();
    }

    @Test
    void getEmployeeAwards_returnsFromDbSnapshot_andZeroWhenAbsent() {
        when(employeeRepository.findAll()).thenReturn(List.of(
                emp(1, 100, 2),
                emp(2, 100, 5),
                emp(3, 200, 1)
        ));

        cacheService.initializeFromDatabase();

        // Existing employees reflect DB values
        assertThat(cacheService.getEmployeeAwards(100, 1)).isEqualTo(2);
        assertThat(cacheService.getEmployeeAwards(100, 2)).isEqualTo(5);
        assertThat(cacheService.getEmployeeAwards(200, 3)).isEqualTo(1);

        // Unknown employee returns 0 by contract
        assertThat(cacheService.getEmployeeAwards(100, 999)).isZero();
        assertThat(cacheService.getEmployeeAwards(999, 1)).isZero();
    }


    @Test
    void incrementAllForOrg_doesNothingWhenOrgMissing() {
        when(employeeRepository.findAll()).thenReturn(List.of(
                emp(1, 100, 2),
                emp(2, 100, 5)
        ));
        cacheService.initializeFromDatabase();

        // Act on a non-existent organization
        cacheService.incrementAllForOrg(999);

        // Existing org values should remain unchanged
        assertThat(cacheService.getEmployeeAwards(100, 1)).isEqualTo(2);
        assertThat(cacheService.getEmployeeAwards(100, 2)).isEqualTo(5);
    }

    @Test
    void addAndRemoveEmployee_updatesStructure() {
        // Add employees to a new org
        cacheService.addEmployee(1, 10, 7);
        assertThat(cacheService.getEmployeeAwards(1, 10)).isEqualTo(7);

        // Adding another employee under same org
        cacheService.addEmployee(1, 11, 0);
        assertThat(cacheService.getEmployeeAwards(1, 11)).isZero();

        // Remove an existing employee
        cacheService.removeEmployee(1, 10);
        assertThat(cacheService.getEmployeeAwards(1, 10)).isZero();

        // Removing a non-existent employee should be a no-op
        cacheService.removeEmployee(1, 999);
        assertThat(cacheService.getEmployeeAwards(1, 11)).isZero();
    }
}
