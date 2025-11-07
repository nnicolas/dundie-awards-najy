package com.ninjaone.dundie_awards.model;

import com.ninjaone.dundie_awards.common.Constraints;
import jakarta.persistence.*;

@Entity
@Table(name = "organizations")
public class Organization {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "name", nullable = false, length = Constraints.NAME_MAX)
  private String name;

  public Organization() {

  }

  public Organization(String name) {
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
