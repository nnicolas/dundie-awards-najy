package com.ninjaone.dundie_awards.dto;


public class OrganizationDto {

  private long id;

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
