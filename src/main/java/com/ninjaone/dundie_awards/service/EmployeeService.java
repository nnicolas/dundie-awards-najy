package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.EmployeeDto;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {

    EmployeeDto save(EmployeeDto employeeDto);

    List<EmployeeDto> getAllEmployees();

    Optional<EmployeeDto> getEmployeeById(Long id);

    boolean deleteEmployeeById(Long id);

    Optional<EmployeeDto>  update(EmployeeDto employeeDto);
}
