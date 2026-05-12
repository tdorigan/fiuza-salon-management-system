package com.nci.fiuza.controller;

import com.nci.fiuza.domain.User;
import com.nci.fiuza.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.Optional;

//makes this class run for all controllers in the application
@ControllerAdvice
public class GlobalControllerAdvice {

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

}
