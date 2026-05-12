package com.nci.fiuza.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//Data Transfer Object to securely transfer data between layers
//class for validation of the register user form, it is not a database entity
public class RegisterUserForm {

    //validating the fields using validation maven dependency

    @NotBlank(message = "{register.email.required}")
    @Email(message = "{register.email.invalid}")
    @Size(max = 50)
    private String email;

    @NotBlank(message = "{register.password.required}")
    @Size(min = 6, max = 100)
    private String password;

    private String confirmPassword;

    @NotBlank(message = "{register.fullName.required}")
    @Size(max = 100)
    private String fullName;

    @Size(max = 50)
    private String phone;

    @Size(max = 100)
    private String address;

    //empty constructor required for spring to instantiate the object
    public RegisterUserForm() {
    }

    //standard getters and setters required for spring to bind form fields to this object
    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
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
