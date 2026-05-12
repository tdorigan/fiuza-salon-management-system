package com.nci.fiuza.controller;

import com.nci.fiuza.domain.User;
import com.nci.fiuza.dto.ChangePasswordForm;
import com.nci.fiuza.dto.ProfileForm;
import com.nci.fiuza.dto.RegisterUserForm;
import com.nci.fiuza.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class ProfileController {

    private final UserService userService;

    //constructor injection
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String showProfile(Model model,
                              Principal principal){

        //get the logged user (in my case username is email)
        User loggedUser = userService.findByEmail(principal.getName());

        //send the user email to the view
        model.addAttribute("email", loggedUser.getEmail());

        //get the profile form by email (in my case username is email) and send it to the view
        model.addAttribute("profileForm", userService.getProfileFormByEmail(loggedUser.getEmail()));

        //send a new change password form to the view
        model.addAttribute("changePasswordForm", new ChangePasswordForm());

        return "profile";
    }

    /*---------------------------- update profile details -------------------------------*/
    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileForm profileForm, //take the form data from the html and convert (binds) into the ProfileForm object and validate against the DTO
                                BindingResult bindingResult, //this must be just after the @Valid, it stores validation errors if they exist
                                RedirectAttributes ra, //to send a message after redirect
                                Model model,
                                Principal principal){

        //bean validation errors, in case any error occurred on the dto, fills the change password form and return
        if (bindingResult.hasErrors()) {

            User loggedUser = userService.findByEmail(principal.getName());

            model.addAttribute("email", loggedUser.getEmail());

            //send a new change password form to the view
            model.addAttribute("changePasswordForm", new ChangePasswordForm());

            //in case has errors, return to the profile form
            return "profile";
        }

        try {

            //get the logged user
            User loggedUser = userService.findByEmail(principal.getName());

            //calls update profile service
            userService.updateProfile(loggedUser.getEmail(), profileForm);

            //if success adds a message
            ra.addFlashAttribute("message", "profile.message.updated");

        } catch (Exception e) {
            //if error adds an error message
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/profile";
    }

    /*---------------------------- change password -------------------------------*/
    @PostMapping("/profile/changePassword")
    public String changePassword(@Valid @ModelAttribute("changePasswordForm") ChangePasswordForm changePasswordForm, //take the form data from the html and convert (binds) into the ChangePasswordForm object and validate against the DTO
                                 BindingResult bindingResult, //this must be just after the @Valid, it stores validation errors if they exist
                                 RedirectAttributes ra, //to send a message after redirect
                                 Model model,
                                 Principal principal){

        //bean validation errors, in case any error occurred on the dto, fills the data from the profile details form and return
        if (bindingResult.hasErrors()) {

            //get the logged user (in my case username is email)
            User loggedUser = userService.findByEmail(principal.getName());

            //send the user email to the view
            model.addAttribute("email", loggedUser.getEmail());

            //get the profile form by email (in my case username is email) and send it to the view
            model.addAttribute("profileForm", userService.getProfileFormByEmail(loggedUser.getEmail()));

            return "profile";

        }

        try {

            //get the logged user
            User loggedUser = userService.findByEmail(principal.getName());

            //calls change password service
            userService.changePassword(loggedUser.getEmail(), changePasswordForm);

            //if success adds a message
            ra.addFlashAttribute("message", "profile.message.passwordChanged");

        } catch (Exception e) {
            //if error adds an error message
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/profile";
    }


}
