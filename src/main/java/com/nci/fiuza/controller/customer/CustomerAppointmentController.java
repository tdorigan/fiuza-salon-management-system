package com.nci.fiuza.controller.customer;

import com.nci.fiuza.domain.Appointment;
import com.nci.fiuza.domain.AppointmentStatus;
import com.nci.fiuza.domain.Service;
import com.nci.fiuza.domain.User;
import com.nci.fiuza.service.AppointmentService;
import com.nci.fiuza.service.ServiceService;
import com.nci.fiuza.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//controller for mapping the requests related to appointments in the customer area
@Controller
public class CustomerAppointmentController {

    //i'll need to use appointment service on my methods
    private final AppointmentService appointmentService;

    //i'll need to use service service on my methods
    private final ServiceService serviceService;
    private final UserService userService;

    //constructor injection so i can use appointment and service service on my methods
    public CustomerAppointmentController(AppointmentService appointmentService, ServiceService serviceService, UserService userService) {
        this.appointmentService = appointmentService;
        this.serviceService = serviceService;
        this.userService = userService;
    }

    //customer appointments (my appointments)
    @GetMapping("/customer/appointments")
    public String showAppointmentsByCustomer(
            Principal principal, //getting the logged user from spring security
            Model model) {

        //get the user from the logged principal, in my case getName is the email
        User user = userService.findByEmail(principal.getName());

        //getting list of appointments of the logged user
        List<Appointment> listAppointments = appointmentService.listAppointmentsByCustomerId(user.getId());

        //setting list of appointments as parameter
        model.addAttribute("listAppointments", listAppointments);

        return "customer/appointments";
    }

    //book new appointment
    @GetMapping("/customer/appointments/new")
    public String newAppointment(Model model){

        //only create a new appointment if there is no appointment already coming from flash attributes, to prevent clearing the form if a backend error message occurs when saving
        if (!model.containsAttribute("appointment")) {

            //passing a new appointment object as parameter
            model.addAttribute("appointment", new Appointment());

        }

        //passing page title as parameter
        model.addAttribute("pageTitle", "customer.appointment.new");

        //sending the list of active services by parameter to the view
        model.addAttribute("listServices", serviceService.listActive());

        //setting max and min appointment dates, appointment booked should be from today to max 1 year ahead
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusYears(1);

        model.addAttribute("minDate", today);
        model.addAttribute("maxDate", maxDate);

        return "customer/appointment_form";

    }

    //save an appointment
    @PostMapping("/customer/appointments/save")
    public String saveAppointment(
            Appointment appointment, //getting the appointment from the form
            @RequestParam Long serviceId, //getitng serviceId from the dropdown
            Principal principal, //to get the logged user
            RedirectAttributes ra,
            Model model
    ) {

        //create an object of the logged customer/user
        User customer = userService.findByEmail(principal.getName());

        //create an object of the selected service
        Service service = serviceService.get(serviceId);

        //set customer and service
        appointment.setCustomer(customer);
        appointment.setService(service);

        //new appointments will be always status BOOKED
        appointment.setStatus(AppointmentStatus.BOOKED);

        //validate appointment booking times
        try{

            appointmentService.validateBooking(appointment);

        } catch (IllegalStateException e) {

            //send error message as parameter to the view
            ra.addFlashAttribute("errorMessage", e.getMessage());

            //send the appointment form back to the view so it won't clean just because a backend error message occurred
            ra.addFlashAttribute("appointment", appointment);

            //send the selected service
            ra.addFlashAttribute("selectedServiceId", serviceId);

            //back to the new appointment form
            return "redirect:/customer/appointments/new";

        }

        //if validations ok, save the new appointment
        appointmentService.save(appointment);

        //add a confirmation message to be takken on the list appointments page
        ra.addFlashAttribute("message", "appointment.message.saved");

        //as it's post method i redirect
        return "redirect:/customer/appointments";

    }

    //cancel an appointment
    @PostMapping("/customer/appointment/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id, //getting the appointment id by parameter via url
                                    Principal principal, //getting the logged user to prevent cancel bookings from other users via url
                                    RedirectAttributes ra) {

        //get the id of the logged user
        Long loggedUserId = userService.findByEmail(principal.getName()).getId();

        try {
            //try to cancel
            appointmentService.cancelByCustomerAndId(loggedUserId, id);
            //if success set a message
            ra.addFlashAttribute("message", "appointment.message.cancelled");
        } catch (IllegalStateException ex) {
            //if error set an error message
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/customer/appointments";

    }

    //API endpoint that returns available time slots as JSON
    @GetMapping("/customer/appointments/availableTimes")
    @ResponseBody //this is to indicate the return will be directly in the HTTP response body via JSON
    public List<String> getAvailableTimes(
            @RequestParam Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {

        //cals the service and return the json to the view
        return appointmentService.getAvailableTimeSlots(serviceId, date);

    }

}
