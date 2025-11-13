package com.ninjaone.dundie_awards.service.impl;

import com.ninjaone.dundie_awards.dto.EmployeeCreateDto;
import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.EmployeeUpdateDto;
import com.ninjaone.dundie_awards.mapper.EmployeeMapper;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import com.ninjaone.dundie_awards.service.AwardsCacheService;
import com.ninjaone.dundie_awards.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final AwardsCacheService awardsCacheService;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, OrganizationRepository organizationRepository, AwardsCacheService awardsCacheService) {
        this.employeeRepository = employeeRepository;
        this.organizationRepository = organizationRepository;
        this.awardsCacheService = awardsCacheService;
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
        Optional<Employee> employeeOpt = employeeRepository.findById(id);
        if(employeeOpt.isEmpty()) {
            return false;
        }
        employeeRepository.deleteById(id);
        awardsCacheService.removeEmployee(employeeOpt.get().getOrganization().getId(), id);
        return true;
    }

    @Override
    @Transactional
    public Optional<EmployeeDto> update(Long id, EmployeeUpdateDto employeeDto) {
        Optional<Employee> employeeOpt = employeeRepository.findById(id);
        if (employeeOpt.isEmpty()) {
            return Optional.empty();
        }

        Employee employee = employeeOpt.get();
        EmployeeMapper.applyUpdate(employee, employeeDto);
        return Optional.of(EmployeeMapper.toDto(employeeRepository.save(employee)));
    }

    @Override
    @Transactional
    public EmployeeDto save(EmployeeCreateDto employeeDto) {

        if(employeeDto.getOrganization() == null) {
            throw new InvalidParameterException("Organization is required");
        }
        Organization org = organizationRepository.findById(employeeDto.getOrganization().getId()).orElse(null);
        if(org == null) {
            throw new InvalidParameterException("Organization is required");
        }

        Employee employee = EmployeeMapper.toEntity(employeeDto);
        employee.setDundieAwards(0);
        employee.setOrganization(org);
        Employee saved = employeeRepository.save(employee);
        // Add to cache with initial 0 awards
        awardsCacheService.addEmployee(org.getId(), saved.getId(), saved.getDundieAwards());
        return EmployeeMapper.toDto(saved);
    }
}
