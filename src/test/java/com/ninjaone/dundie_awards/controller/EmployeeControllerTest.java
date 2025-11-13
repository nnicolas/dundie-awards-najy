package com.ninjaone.dundie_awards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjaone.dundie_awards.dto.EmployeeCreateDto;
import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.EmployeeUpdateDto;
import com.ninjaone.dundie_awards.dto.OrganizationDto;
import com.ninjaone.dundie_awards.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDto dto(long id) {
        EmployeeDto d = new EmployeeDto();
        d.setId(id);
        d.setFirstName("F"+id);
        d.setLastName("L"+id);
        d.setDundieAwards(0);
        OrganizationDto org = new OrganizationDto();
        org.setId(10L);
        d.setOrganization(org);
        return d;
    }

    @Test
    void getAllEmployees_returnsList() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(dto(1), dto(2)));
        mockMvc.perform(get("/employees").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    void getEmployeeById_found() throws Exception {
        when(employeeService.getEmployeeById(5L)).thenReturn(Optional.of(dto(5)));
        mockMvc.perform(get("/employees/{id}", 5L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)));
    }

    @Test
    void getEmployeeById_notFound() throws Exception {
        when(employeeService.getEmployeeById(5L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/employees/{id}", 5L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createEmployee_returnsCreatedWithLocation() throws Exception {
        EmployeeCreateDto req = new EmployeeCreateDto("Jane", "Doe", new OrganizationDto());
        EmployeeDto saved = dto(100);
        when(employeeService.save(any(EmployeeCreateDto.class))).thenReturn(saved);

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/employees/100")))
                .andExpect(jsonPath("$.id", is(100)));
    }

    @Test
    void updateEmployee_found() throws Exception {
        EmployeeUpdateDto upd = new EmployeeUpdateDto("NewF", "NewL", null);
        when(employeeService.update(eq(100L), any(EmployeeUpdateDto.class))).thenReturn(Optional.of(dto(100)));

        mockMvc.perform(put("/employees/{id}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)));
    }

    @Test
    void updateEmployee_notFound() throws Exception {
        EmployeeUpdateDto upd = new EmployeeUpdateDto("NewF", "NewL", null);
        when(employeeService.update(eq(100L), any(EmployeeUpdateDto.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/employees/{id}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upd)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEmployee_foundNoContent() throws Exception {
        when(employeeService.deleteEmployeeById(100L)).thenReturn(true);
        mockMvc.perform(delete("/employees/{id}", 100L))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEmployee_notFound() throws Exception {
        when(employeeService.deleteEmployeeById(100L)).thenReturn(false);
        mockMvc.perform(delete("/employees/{id}", 100L))
                .andExpect(status().isNotFound());
    }
}
