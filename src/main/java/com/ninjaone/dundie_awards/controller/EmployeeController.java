package com.ninjaone.dundie_awards.controller;

import com.ninjaone.dundie_awards.dto.EmployeeCreateDto;
import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.EmployeeUpdateDto;
import com.ninjaone.dundie_awards.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping()
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // get all employees
    @GetMapping(value = "/employees", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EmployeeDto> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    // create employee rest api
    @PostMapping(value = "/employees", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeCreateDto employeeDto) {
        EmployeeDto savedEmployee = employeeService.save(employeeDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedEmployee.getId())
                .toUri();
        return ResponseEntity.created(location).body(savedEmployee);
    }

    // get employee by id rest api
    @GetMapping(value = "/employees/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        Optional<EmployeeDto> optionalEmployeeDtoOpt = employeeService.getEmployeeById(id);
        return optionalEmployeeDtoOpt.map(ResponseEntity::ok).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // update employee rest api
    @PutMapping(value = "/employees/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDto employeeDetails) {
        Optional<EmployeeDto> employeeDto = employeeService.update(id, employeeDetails);
        return employeeDto.map(ResponseEntity::ok).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // delete employee rest api
    @DeleteMapping(value = "/employees/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {

        if (!employeeService.deleteEmployeeById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.noContent().build(); // returns HTTP 204
    }
}
