package com.nci.fiuza.configuration;

import com.nci.fiuza.domain.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

//code adapted from Coding Complex: Spring Boot Login & Registration with Thymeleaf & MySQL (https://www.youtube.com/watch?v=jPzsikw2qFY)

//makes this class a spring configuration class
@Configuration
public class WebSecurityConfig {

    //this is used to store encrypted user passwords, and it's called automatically during login by spring security
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    //configure security for each http request of the project, what would be permitted to all (public pages) and what would require login
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth

                        //no authentication required for the below paths
                        .requestMatchers(
                                "/",
                                "/public/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/uploads/**"
                        ).permitAll()

                        //accessible by any authenticated user, admin or customer
                        .requestMatchers("/profile/**").authenticated()

                        //only accessible by users authenticated with role CUSTOMER
                        .requestMatchers("/customer/**").hasRole(Role.CUSTOMER.name())

                        //only accessible by users authenticated with role ADMIN
                        .requestMatchers("/admin/**").hasRole(Role.ADMIN.name())

                        //requires authentication for any other request apart from the mentioned above
                        .anyRequest().authenticated()

                )

                //to use my custom login page instead of the default login page provided by spring security
                .formLogin(form -> {
                    form
                            .loginPage("/public/login") //my custom login page
                            .loginProcessingUrl("/public/login") //url that spring security will process on the post form
                            .usernameParameter("email") //in my case the username is email
                            .defaultSuccessUrl("/", true) //after successful login redirects to home page
                            .failureUrl("/public/login?error") //case error on login, stays on login page
                            .permitAll(); //anyone can access login page
                })

                //when logout goes back to the custom login page
                .logout(logout -> logout.logoutSuccessUrl("/public/login?logout"));

        //returns the SecurityFilterChain configuration built
        return http.build();
    }

}
