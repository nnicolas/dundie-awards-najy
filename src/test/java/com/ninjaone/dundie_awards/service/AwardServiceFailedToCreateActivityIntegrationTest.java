package com.ninjaone.dundie_awards.service;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;

@SpringBootTest
class AwardServiceFailedToCreateActivityIntegrationTest {


    //We need to mock this to fail creating the activity in the event listener
    @MockBean
    private ActivityService activityService;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AwardsServiceImpl awardService;

    @Autowired
    private EntityManager entityManager;

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
     * This test case, test the case where we:
     * - Successfully giving awards to all users of the org
     * - Successfully publishes the event
     * - In the event listener fails to create an activity
     * - Roll back the awards given by decrementing the awards
     */
    @Test
    @Transactional
    void testGiveAwards_failedToCreateActivity() {
        doThrow(new RuntimeException("Create Activity Failed"))
                .when(activityService)
                .createActivityForAwardsGiven(anyLong(), anyInt());

        // When
        int result = awardService.giveAwards(organization.getId());

        assertEquals(NUMBER_OF_EMPLOYEES, result);

        entityManager.flush();
        entityManager.clear();

        // Then
        // all employees of the org updated

        // Verify DB changes rolled back
        boolean anyAwarded = employeeRepository.findAll().stream().anyMatch(e -> (e.getOrganization().getId() == organization.getId() && e.getDundieAwards() == 1));
        assertFalse(anyAwarded, "Transaction should have rolled back updates");

        //No activity created
        assertEquals(0, activityRepository.findAll().size());
    }
}
