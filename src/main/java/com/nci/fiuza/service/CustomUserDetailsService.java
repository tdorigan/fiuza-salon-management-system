package com.nci.fiuza.service;

import com.nci.fiuza.domain.User;
import com.nci.fiuza.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//code adapted from Coding Complex: Spring Boot Login & Registration with Thymeleaf & MySQL (https://www.youtube.com/watch?v=jPzsikw2qFY)

//mark this class as a spring service component
@Service
public class CustomUserDetailsService implements UserDetailsService { //implements UserDetailsService interface used by spring security

    //instantiating UserRepository
    private UserRepository userRepository;

    //constructor injection
    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    //method automatically called by spring security during login
    //username parameter in my case will be the email
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //calling UserRepository.findByEmail
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        //returns a UserDetails object from the user, to be used by spring security on authentication
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()) //in my case username is the user email
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(!user.isEnabled())
                .build();

    }
}
