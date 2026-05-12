package com.nci.fiuza.domain;

import jakarta.persistence.*;

//domain class User, it's also a table in the database
@Entity
@Table(name = "users")
public class User {

    @Id //primary key on the db
    @GeneratedValue(strategy = GenerationType.IDENTITY) //to generate the ids automatically incremental
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    //will be CUSTOMER or ADMIN
    //type will be Role because i created that Enum to accomodate the possible values to avoid hardcoding
    @Enumerated(EnumType.STRING) //so it stores the enum string on the database, instead of storing 1/2
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(length = 50)
    private String phone;

    @Column(length = 100)
    private String address;

    @Column(nullable = false)
    private boolean enabled = true;

    //constructor
    public User(){
    }

    //getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
