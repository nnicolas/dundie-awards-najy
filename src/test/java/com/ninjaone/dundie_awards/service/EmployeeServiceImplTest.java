package com.ninjaone.dundie_awards.service;

import com.ninjaone.dundie_awards.dto.EmployeeCreateDto;
import com.ninjaone.dundie_awards.dto.EmployeeDto;
import com.ninjaone.dundie_awards.dto.EmployeeUpdateDto;
import com.ninjaone.dundie_awards.dto.OrganizationDto;
import com.ninjaone.dundie_awards.mapper.EmployeeMapper;
import com.ninjaone.dundie_awards.model.Employee;
import com.ninjaone.dundie_awards.model.Organization;
import com.ninjaone.dundie_awards.repository.EmployeeRepository;
import com.ninjaone.dundie_awards.repository.OrganizationRepository;
import com.ninjaone.dundie_awards.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class EmployeeServiceImplTest {

    private EmployeeRepository employeeRepository;
    private OrganizationRepository organizationRepository;
    private AwardsCacheService awardsCacheService;

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeRepository.class);
        organizationRepository = mock(OrganizationRepository.class);
        awardsCacheService = mock(AwardsCacheService.class);
        employeeService = new EmployeeServiceImpl(employeeRepository, organizationRepository, awardsCacheService);
    }

    private Organization org(long id) {
        Organization o = new Organization();
        o.setId(id);
        o.setName("Org"+id);
        return o;
    }

    private Employee emp(long id, long orgId) {
        Organization o = org(orgId);
        Employee e = new Employee("f"+id, "l"+id, o);
        e.setId(id);
        e.setDundieAwards(0);
        return e;
    }

    @Test
    void getAllEmployees_mapsToDtos() {
        when(employeeRepository.findAll()).thenReturn(List.of(emp(1, 100), emp(2, 100)));
        List<EmployeeDto> list = employeeService.getAllEmployees();
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getEmployeeById_foundReturnsDto() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp(1, 100)));
        Optional<EmployeeDto> dto = employeeService.getEmployeeById(1L);
        assertThat(dto).isPresent();
        assertThat(dto.get().getId()).isEqualTo(1L);
    }

    @Test
    void getEmployeeById_notFoundReturnsEmpty() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<EmployeeDto> dto = employeeService.getEmployeeById(1L);
        assertThat(dto).isEmpty();
    }

    @Test
    void save_throwsWhenNoOrganization() {
        EmployeeCreateDto req = new EmployeeCreateDto("A", "B", null);
        assertThatThrownBy(() -> employeeService.save(req))
                .isInstanceOf(InvalidParameterException.class);
    }

    @Test
    void save_throwsWhenOrganizationNotFound() {
        OrganizationDto orgDto = new OrganizationDto();
        orgDto.setId(99L);
        EmployeeCreateDto req = new EmployeeCreateDto("A", "B", orgDto);
        when(organizationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> employeeService.save(req))
                .isInstanceOf(InvalidParameterException.class);
    }

    @Test
    void save_persistsEmployee_andAddsToCache_andReturnsDto() {
        OrganizationDto orgDto = new OrganizationDto();
        orgDto.setId(10L);
        EmployeeCreateDto req = new EmployeeCreateDto("Jane", "Doe", orgDto);
        Organization org = org(10L);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        Employee saved = new Employee("Jane", "Doe", org);
        saved.setId(123L);
        saved.setDundieAwards(0);
        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);

        EmployeeDto dto = employeeService.save(req);

        assertThat(dto.getId()).isEqualTo(123L);
        verify(employeeRepository).save(any(Employee.class));
        verify(awardsCacheService).addEmployee(10L, 123L, 0);
    }

    @Test
    void update_whenFound_updatesAndReturnsDto() {
        Employee e = emp(1, 100);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(e));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeUpdateDto upd = new EmployeeUpdateDto("NewF", "NewL", null);
        Optional<EmployeeDto> result = employeeService.update(1L, upd);
        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("NewF");
        assertThat(result.get().getLastName()).isEqualTo("NewL");
    }

    @Test
    void update_whenNotFound_returnsEmpty() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<EmployeeDto> result = employeeService.update(1L, new EmployeeUpdateDto("A","B", null));
        assertThat(result).isEmpty();
    }

    @Test
    void delete_whenFound_deletesAndRemovesFromCache_returnsTrue() {
        Employee e = emp(5, 55);
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(e));
        boolean ok = employeeService.deleteEmployeeById(5L);
        assertThat(ok).isTrue();
        verify(employeeRepository).deleteById(5L);
        verify(awardsCacheService).removeEmployee(55L, 5L);
    }

    @Test
    void delete_whenNotFound_returnsFalse() {
        when(employeeRepository.findById(5L)).thenReturn(Optional.empty());
        boolean ok = employeeService.deleteEmployeeById(5L);
        assertThat(ok).isFalse();
        verify(employeeRepository, never()).deleteById(anyLong());
        verify(awardsCacheService, never()).removeEmployee(anyLong(), anyLong());
    }
}
