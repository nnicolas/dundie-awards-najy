package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.exception.FailedToGiveAwardsException;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import com.ninjaone.dundie_awards.service.impl.AwardsServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class AwardServiceIntegrationTest {

    @MockBean(name = "applicationEventMulticaster")
    private ApplicationEventMulticaster multicaster;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AwardsServiceImpl awardService;

    @Autowired
    EntityManager entityManager;

    private Organization organization;
    private static final int NUMBER_OF_EMPLOYEES = 10;

    @BeforeEach
    void setUp() {
        this.organization = organizationRepository.save(new Organization("Test Organization"));

        for (int i = 0; i < NUMBER_OF_EMPLOYEES; i++) {
            employeeRepository.save(new Employee("First_" + i, "Last_" + i, organization));
        }
    }

    @Test
    @Transactional
    void testGiveAwards_SuccessfulPublish() throws FailedToGiveAwardsException {

        // When
        int result = awardService.giveAwards(organization.getId());

        entityManager.flush();
        entityManager.clear();

        // Then
        // all employees of the org updated
        assertEquals(NUMBER_OF_EMPLOYEES, result);

        // Assert by querying the db
        long numUpdated = employeeRepository.findAll().stream().filter(e -> (e.getOrganization().getId() == organization.getId() && e.getDundieAwards() == 1)).count();
        assertEquals(NUMBER_OF_EMPLOYEES, numUpdated);
    }

    @Test
    @Transactional
    void testGiveAwards_PublishFails_RollsBack() {
        doThrow(new RuntimeException("Publish failed"))
                .when(multicaster)
                .multicastEvent(any(), any());

        // Act + Assert
        assertThrows(FailedToGiveAwardsException.class, () -> awardService.giveAwards(organization.getId()));
        reset(multicaster);

        // Verify DB changes rolled back
        boolean anyAwarded = employeeRepository.findAll().stream().anyMatch(e -> (e.getOrganization().getId() == organization.getId() && e.getDundieAwards() == 1));
        assertFalse(anyAwarded, "Transaction should have rolled back updates");
    }
}
