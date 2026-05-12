package com.nci.fiuza.service;

import com.nci.fiuza.domain.Role;
import com.nci.fiuza.domain.User;
import com.nci.fiuza.dto.ChangePasswordForm;
import com.nci.fiuza.dto.ProfileForm;
import com.nci.fiuza.dto.RegisterUserForm;
import com.nci.fiuza.repository.AppointmentRepository;
import com.nci.fiuza.repository.OrderRepository;
import com.nci.fiuza.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//business logic class
@Service
public class UserService {

    //i'll need access to the UserRepository
    private final UserRepository repo;

    //i'll need access to the AppointmentRepository
    private final AppointmentRepository appointmentRepo;

    private final OrderRepository orderRepo;

    //to encode password before saving user
    private final PasswordEncoder passwordEncoder;

    //constructor injection so i can use repo and password encoder on my methods
    public UserService(UserRepository repo, AppointmentRepository appointmentRepo, OrderRepository orderRepo, PasswordEncoder passwordEncoder){
        this.repo = repo;
        this.appointmentRepo = appointmentRepo;
        this.orderRepo = orderRepo;
        this.passwordEncoder = passwordEncoder;
    }

    //get only customers from users
    public List<User> listCustomers() {
        //using the enum instead of hardcoding
        return repo.findByRole(Role.CUSTOMER);
    }

    //find a user by email
    public User findByEmail(String email) {
        return repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("user.message.notFound"));
    }

    //method the register a new user, getting the dto validated object
    @Transactional
    public void registerCustomer(RegisterUserForm form){

        //check if confirm password match password
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalStateException("register.email.passwordsDontMatch");
        }

        //check if email already exists in the database
        if (repo.findByEmail(form.getEmail()).isPresent()) {
            throw new IllegalStateException("register.email.exists");
        }

        //create the new user to be inserted
        //getting the data from the validated dto form
        User user = new User();
        user.setEmail(form.getEmail());
        user.setFullName(form.getFullName());
        user.setAddress(form.getAddress());
        user.setPhone(form.getPhone());

        //role will always be customer
        user.setRole(Role.CUSTOMER);

        //always enabled
        user.setEnabled(true);

        //encode password
        user.setPassword(passwordEncoder.encode(form.getPassword()));

        //save new user to the database
        repo.save(user);

    }

    //enable/disable a user
    @Transactional
    public void toggleActive(Long customerId) {

        //get the user
        User user = repo.findById(customerId).orElseThrow(() -> new UsernameNotFoundException("user.message.notFound"));

        //set enabled as the opposite of the current value
        user.setEnabled(!user.isEnabled());

        //save the user
        repo.save(user);

    }

    //delete a user
    @Transactional
    public void delete(Long customerId) {

        //get the user
        User user = repo.findById(customerId).orElseThrow(() -> new UsernameNotFoundException("user.message.notFound"));

        //check if user has appointments
        if (appointmentRepo.existsByCustomerId(user.getId())) {
            throw new IllegalStateException("customer.message.cannotDeleteHasAppointments");
        }

        //check if user has orders
        if (orderRepo.existsByCustomerId(user.getId())) {
            throw new IllegalStateException("customer.message.cannotDeleteHasOrders");
        }

        //delete user
        repo.delete(user);

    }

    //get a user by email and fill the profile form
    public ProfileForm getProfileFormByEmail(String email){

        //get the user by email
        User user = findByEmail(email);

        //create and populate the form with user data
        ProfileForm profileForm = new ProfileForm();
        profileForm.setAddress(user.getAddress());
        profileForm.setPhone(user.getPhone());
        profileForm.setFullName(user.getFullName());

        //return the form
        return profileForm;

    }

    //get a user by email and updates the attributes according to the profile form
    @Transactional
    public void updateProfile(String email, ProfileForm profileForm){

        //get the user by email
        User user = findByEmail(email);

        //update user fields
        user.setFullName(profileForm.getFullName());
        user.setAddress(profileForm.getAddress());
        user.setPhone(profileForm.getPhone());

        //save
        repo.save(user);

    }

    //updates user password
    @Transactional
    public void changePassword(String email, ChangePasswordForm changePasswordForm){

        //check if confirm password matches new password
        if (!changePasswordForm.getNewPassword().equals(changePasswordForm.getConfirmNewPassword())) {
            throw new IllegalStateException("profile.message.passwordsDoNotMatch");
        }

        //get the user by email
        User user = findByEmail(email);

        //check if informed current password matches the one from the db
        if (!passwordEncoder.matches(changePasswordForm.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("profile.message.currentPasswordIncorrect");
        }

        //if passes validations, saves the new password
        user.setPassword(passwordEncoder.encode(changePasswordForm.getNewPassword()));

        //saves the user
        repo.save(user);

    }

}
