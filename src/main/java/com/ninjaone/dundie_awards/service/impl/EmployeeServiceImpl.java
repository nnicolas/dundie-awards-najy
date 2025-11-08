package com.ninjaone.dundie_awards.service.impl;

import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.mapper.EmployeeMapper;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import com.ninjaone.dundie_awards.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    public List<EmployeeDto> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream().map(EmployeeMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeDto> getEmployeeById(Long id) {
        return employeeRepository.findById(id).map(EmployeeMapper::toDto);
    }

    @Override
    @Transactional
    public boolean deleteEmployeeById(Long id) {
        if (!employeeRepository.existsById(id)) {
            return false;
        }
        employeeRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public Optional<EmployeeDto> update(EmployeeDto employeeDto) {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeDto.getId());
        if (employeeOpt.isEmpty()) {
            return Optional.empty();
        }

        Employee employee = employeeOpt.get();
        employee.setFirstName(employeeDto.getFirstName());
        employee.setLastName(employeeDto.getLastName());
        return Optional.of(EmployeeMapper.toDto(employeeRepository.save(employee)));
    }

    @Override
    @Transactional
    public EmployeeDto save(EmployeeDto employeeDto) {

        Long orgId = employeeDto.getOrganization() != null ? employeeDto.getOrganization().getId() : null;
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + orgId));

        Employee employee = EmployeeMapper.toEntity(employeeDto);
        employee.setDundieAwards(0);
        employee.setOrganization(org);
        return EmployeeMapper.toDto(employeeRepository.save(employee));
    }
}
