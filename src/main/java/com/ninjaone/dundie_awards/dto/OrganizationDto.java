package com.ninjaone.dundie_awards.dto;


import com.ninjaone.dundie_awards.common.Constraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OrganizationDto {

  private long id;

  @NotBlank
  @Size(min= Constraints.NAME_MIN, max = Constraints.NAME_MAX, message = "Organization Name must be between " + Constraints.NAME_MIN + " and " + Constraints.NAME_MAX + " characters")
  private String name;

  public OrganizationDto() {

  }

  public OrganizationDto(String name) {
    super();
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
