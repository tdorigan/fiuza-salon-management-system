package com.nci.fiuza.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//Data Transfer Object for change password form
public class ChangePasswordForm {

    //current password is required to confirm user's identity
    @NotBlank(message = "{profile.currentPassword.required}")
    private String currentPassword;

    //new password is required and must have at least 6 characters
    @NotBlank(message = "{profile.newPassword.required}")
    @Size(min = 6, max = 100, message = "{profile.message.passwordTooShort}")
    private String newPassword;

    //confirmation password is required
    @NotBlank(message = "{profile.confirmNewPassword.required}")
    private String confirmNewPassword;

    //empty constructor required by spring
    public ChangePasswordForm() {
    }

    //getters and setters
    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }
}
