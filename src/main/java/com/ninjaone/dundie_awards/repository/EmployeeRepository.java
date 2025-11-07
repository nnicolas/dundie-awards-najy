package com.ninjaone.dundie_awards.repository;

import com.ninjaone.dundie_awards.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Modifying
    @Transactional
    @Query("update Employee e set e.dundieAwards = e.dundieAwards + 1 where e.organization.id = :organizationId")
    int incrementDundieAwardsForOrgEmployees(@Param("organizationId") long organizationId);


    @Modifying
    @Transactional
    @Query("update Employee e set e.dundieAwards = e.dundieAwards - 1 where e.organization.id = :organizationId")
    int decrementDundieAwardsForOrgEmployees(@Param("organizationId") long organizationId);
}
