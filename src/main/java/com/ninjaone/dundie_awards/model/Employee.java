package com.ninjaone.dundie_awards.model;

import com.ninjaone.dundie_awards.common.Constraints;
import jakarta.persistence.*;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "first_name", nullable = false, length = Constraints.NAME_MAX)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = Constraints.NAME_MAX)
    private String lastName;

    @Column(name = "dundie_awards", nullable = false)
    private int dundieAwards;

    @ManyToOne(optional = false)
    private Organization organization;

    public Employee() {

    }

    public Employee(String firstName, String lastName, Organization organization) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.organization = organization;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public void setDundieAwards(int dundieAwards){
        this.dundieAwards = dundieAwards;
    }

    public int getDundieAwards(){
        return dundieAwards;
    }
}