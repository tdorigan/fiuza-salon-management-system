package com.nci.fiuza.init;

import com.nci.fiuza.domain.Role;
import com.nci.fiuza.domain.User;
import com.nci.fiuza.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component //spring will automatically detect and run this class on initialization
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    //constructor injection
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //this method runs automatically when the application starts
    @Override
    @Transactional
    public void run(String... args) {

        //create admin user only if it does not exist
        createAdminIfNotExists(
                "admin@fiuza.com",
                "admin",
                "Fiuza Administrator",
                "123456789",
                "Dublin, Ireland"
        );

    }

    //helper method
    private void createAdminIfNotExists(String email,
                                       String rawPassword,
                                       String fullName,
                                       String phone,
                                       String address) {

        //if user with this email already exists in the db, do nothing
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        //otherwise create new user
        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setFullName(fullName);
        admin.setPhone(phone);
        admin.setAddress(address);
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);

        //save user into db
        userRepository.save(admin);

    }

}
