package com.ninjaone.dundie_awards.mapper;

import com.ninjaone.dundie_awards.dto.EmployeeCreateDto;
import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.EmployeeUpdateDto;
import com.ninjaone.dundie_awards.model.Employee;

public class EmployeeMapper {


    public static EmployeeDto toDto(Employee employee) {
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setId(employee.getId());
        employeeDto.setFirstName(employee.getFirstName());
        employeeDto.setLastName(employee.getLastName());
        employeeDto.setDundieAwards(employee.getDundieAwards());
        employeeDto.setOrganization(OrganizationMapper.toDto(employee.getOrganization()));
        return employeeDto;
    }

    public static Employee toEntity(EmployeeCreateDto employeeDto) {
        Employee employee = new Employee();
        employee.setFirstName(employeeDto.getFirstName());
        employee.setLastName(employeeDto.getLastName());
        employee.setOrganization(OrganizationMapper.toEntity(employeeDto.getOrganization()));
        return employee;
    }

    public static void applyUpdate(Employee employee, EmployeeUpdateDto updateDto) {
        employee.setFirstName(updateDto.getFirstName());
        employee.setLastName(updateDto.getLastName());
    }
}
