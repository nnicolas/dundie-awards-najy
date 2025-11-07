package com.ninjaone.dundie_awards.service.impl;

import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.mapper.EmployeeMapper;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import com.ninjaone.dundie_awards.service.EmployeeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, OrganizationRepository organizationRepository) {
        this.employeeRepository = employeeRepository;
        this.organizationRepository = organizationRepository;
    }

    @Override
    public List<EmployeeDto> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream().map(EmployeeMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<EmployeeDto> getEmployeeById(Long id) {
        return employeeRepository.findById(id).map(EmployeeMapper::toDto);
    }

    @Override
    public boolean deleteEmployeeById(Long id) {
        Optional<Employee> employeeOpt = employeeRepository.findById(id);
        if (employeeOpt.isEmpty()) {
            return false;
        }
        employeeRepository.delete(employeeOpt.get());
        return true;
    }

    @Override
    public Optional<EmployeeDto> update(EmployeeDto employeeDto) {
        Optional<Employee> optionalEmployeeDtoOpt = employeeRepository.findById(employeeDto.getId());
        if (optionalEmployeeDtoOpt.isEmpty()) {
            return Optional.empty();
        }

        Employee employee = optionalEmployeeDtoOpt.get();
        employee.setFirstName(employeeDto.getFirstName());
        employee.setLastName(employeeDto.getLastName());
        return Optional.of(EmployeeMapper.toDto(employeeRepository.save(employee)));
    }

    @Override
    public EmployeeDto save(EmployeeDto employeeDto) {
        Employee employee = EmployeeMapper.toEntity(employeeDto);
        employee.setDundieAwards(0);
        employee.setOrganization(organizationRepository.getReferenceById(employeeDto.getOrganization().getId()));
        employee = employeeRepository.save(employee);
        return EmployeeMapper.toDto(employee);
    }
}
