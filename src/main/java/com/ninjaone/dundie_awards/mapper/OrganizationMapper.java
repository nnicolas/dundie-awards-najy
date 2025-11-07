package com.ninjaone.dundie_awards.mapper;

import com.ninjaone.dundie_awards.dto.OrganizationDto;
import com.ninjaone.dundie_awards.model.Organization;

public class OrganizationMapper {


    public static OrganizationDto toDto(Organization organization) {
        if (organization == null) {
            return null;
        }
        OrganizationDto organizationDto = new OrganizationDto();
        organizationDto.setId(organization.getId());
        organizationDto.setName(organization.getName());
        return organizationDto;
    }

    public static Organization toEntity(OrganizationDto organizationDto) {
        if (organizationDto == null) {
            return null;
        }
        Organization organization = new Organization();
        organization.setId(organizationDto.getId());
        organization.setName(organizationDto.getName());
        return organization;
    }
}
