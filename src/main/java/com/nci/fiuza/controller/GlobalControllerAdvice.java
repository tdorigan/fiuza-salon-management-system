package com.nci.fiuza.controller;

import com.nci.fiuza.domain.User;
import com.nci.fiuza.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

//makes this class run for all controllers in the application
@ControllerAdvice
public class GlobalControllerAdvice {

    @Value("${app.upload.max-image-size-bytes}") //reads the configured max image size from application.properties
    private long maxImageSizeBytes; //stores the value so it can be shared with all Thymeleaf pages

    private final UserRepository userRepository;

    //constructor injection
    public GlobalControllerAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //this method runs BEFORE every controller method, adding and attribute called loggedUserFullName to the model
    //it's like using model.addAttribute("xxx", 123); in every controller
    //i'll get the loggedUserFullName on the fragment_navbar
    @ModelAttribute("loggedUserFullName")
    public String loggedUserFullName(Principal principal) {

        //if no user authenticated, return null
        if (principal == null) {
            return null;
        }

        //get the username of the logged in user, in my case is the email
        String email = principal.getName();

        //get the full name from the logged user
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return user.getFullName();
        } else {
            return email;
        }

        //(this is another way of doing the code above)
        /*return userRepository.findByEmail(email)
                .map(User::getFullName)
                .orElse(email);*/

    }

    //adding the current path to the model, to be used by the navbar
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("maxImageSizeBytes") //makes this value available in every Thymeleaf template as ${maxImageSizeBytes}
    public long maxImageSizeBytes() { //method called by Spring before rendering controller views
        return maxImageSizeBytes; //returns the configured upload size limit, for example 5242880 bytes (5MB)
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class) //this method will run when a file upload exceeds Spring's configured max size
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e,
                                              HttpServletRequest request,
                                              RedirectAttributes ra) {

        //add a flash attribute to be shown on the next page after redirect
        ra.addFlashAttribute("errorMessage", "image.message.fileTooLarge");

        //get the url of the page the user came from (the form page)
        String referer = request.getHeader("Referer");

        //if the referer exists and is not empty
        if (referer != null && !referer.isBlank()) {
            //redirect the user back to the same page they came from
            return "redirect:" + referer;
        }

        //fallback: if for some reason referer is missing, redirect to home page
        return "redirect:/";

    }

}
