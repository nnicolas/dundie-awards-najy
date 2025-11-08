package com.ninjaone.dundie_awards.dto;

import com.ninjaone.dundie_awards.common.Constraints;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmployeeDto {

    private long id;

    @NotBlank(message = "{validation.firstName.notBlank}")
    @Size(min= Constraints.NAME_MIN, max = Constraints.NAME_MAX, message = "{validation.firstName.size}")
    private String firstName;

    @NotBlank(message = "{validation.lastName.notBlank}")
    @Size(min= Constraints.NAME_MIN, max = Constraints.NAME_MAX, message = "{validation.lastName.size}")
    private String lastName;

    @Min(value = 0, message = "{validation.awards.min}")
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