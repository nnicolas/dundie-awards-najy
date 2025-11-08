package com.ninjaone.dundie_awards.dto;

import com.ninjaone.dundie_awards.common.Constraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmployeeCreateDto {

    @NotBlank
    @Size(min = Constraints.NAME_MIN, max = Constraints.NAME_MAX, message = "First Name must be between " + Constraints.NAME_MIN + " and " + Constraints.NAME_MAX + " characters")
    private String firstName;

    @NotBlank
    @Size(min = Constraints.NAME_MIN, max = Constraints.NAME_MAX, message = "Last Name must be between " + Constraints.NAME_MIN + " and " + Constraints.NAME_MAX + " characters")
    private String lastName;

    private OrganizationDto organization;

    public EmployeeCreateDto() {
    }

    public EmployeeCreateDto(String firstName, String lastName, OrganizationDto organization) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.organization = organization;
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
}
