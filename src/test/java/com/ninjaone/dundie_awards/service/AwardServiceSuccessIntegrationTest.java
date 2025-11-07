package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.exception.FailedToGiveAwardsException;
import com.ninjaone.dundie_awards.model.Activity;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.ActivityRepository;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import com.ninjaone.dundie_awards.service.impl.AwardsServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
class AwardServiceSuccessIntegrationTest {


    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AwardsServiceImpl awardService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ActivityRepository activityRepository;

    private Organization organization;
    private static final int NUMBER_OF_EMPLOYEES = 10;

    @BeforeEach
    void setUp() {
        this.organization = organizationRepository.save(new Organization("Test Organization"));

        for (int i = 0; i < NUMBER_OF_EMPLOYEES; i++) {
            employeeRepository.save(new Employee("First_" + i, "Last_" + i, organization));
        }
    }

    /**
     * In this test case we:
     * - Successfully give awards to all users of the org
     * - Successfully publish the AwardsEvent
     * - Successfully create an Activity for AwardsGiven
     **/
    @Test
    @Transactional
    void testGiveAwards_success() throws FailedToGiveAwardsException {
        // When
        int result = awardService.giveAwards(organization.getId());

        entityManager.flush();
        entityManager.clear();

        // Then
        // all employees of the org updated
        assertEquals(NUMBER_OF_EMPLOYEES, result);

        // Assert all employees of the org have been given awards
        long numUpdated = employeeRepository.findAll().stream().filter(e -> (e.getOrganization().getId() == organization.getId() && e.getDundieAwards() == 1)).count();
        assertEquals(NUMBER_OF_EMPLOYEES, numUpdated);

        // Assert that the Activity was created
        List<Activity> activitiesList = activityRepository.findAll();
        assertEquals(1, activitiesList.size());
    }
}
