package com.nci.fiuza.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//Data Transfer Object to securely transfer data between layers
//class for validation of the change profile form, it is not a database entity
public class ProfileForm {

    //full name is required
    @NotBlank(message = "{register.fullName.required}")
    @Size(max = 100)
    private String fullName;

    //phone is optional but limited to 50 characters
    @Size(max = 50)
    private String phone;

    //address is optional but limited to 100 characters
    @Size(max = 100)
    private String address;

    //empty constructor required by spring
    public ProfileForm() {
    }

    //getters and setters
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


}
