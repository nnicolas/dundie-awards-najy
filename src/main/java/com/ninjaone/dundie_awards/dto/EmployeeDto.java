package com.ninjaone.dundie_awards.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class EmployeeDto {

    private long id;

    @Size(min = 3, max = 20, message = "First Name must be between 3 and 20 characters")
    private String firstName;

    @Size(min = 3, max = 20, message = "Last Name must be between 3 and 20 characters")
    private String lastName;

    @Min(value = 0, message = "Number of awards can't be negative")
    private int dundieAwards;

    private OrganizationDto organization;

    public EmployeeDto() {

    }

    public EmployeeDto(String firstName, String lastName, OrganizationDto organization) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.organization = organization;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public OrganizationDto getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationDto organization) {
        this.organization = organization;
    }

    public void setDundieAwards(int dundieAwards){
        this.dundieAwards = dundieAwards;
    }

    public int getDundieAwards(){
        return dundieAwards;
    }
}