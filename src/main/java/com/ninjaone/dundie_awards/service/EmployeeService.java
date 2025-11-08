package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.EmployeeCreateDto;
import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.EmployeeUpdateDto;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {

    EmployeeDto save(EmployeeCreateDto employeeDto);

    List<EmployeeDto> getAllEmployees();

    Optional<EmployeeDto> getEmployeeById(Long id);

    boolean deleteEmployeeById(Long id);

    Optional<EmployeeDto> update(Long id, EmployeeUpdateDto employeeDto);
}
