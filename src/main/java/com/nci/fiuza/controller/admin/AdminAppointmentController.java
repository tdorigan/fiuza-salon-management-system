package com.nci.fiuza.controller.admin;

import com.nci.fiuza.domain.Appointment;
import com.nci.fiuza.service.AppointmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

//controller for mapping the requests related to appointments in admin portal
@Controller
public class AdminAppointmentController {

    //i'll need to use appointment service on my methods
    private final AppointmentService appointmentService;

    //constructor injection so i can use appointment service on my methods
    public AdminAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /* -------------------- manage appointments page --------------------*/
    @GetMapping("/admin/manage_appointments")
    public String showManageAppointments(Model model){

        //get the list of all appointments
        List<Appointment> listAppointments = appointmentService.listAllAppointments();

        //add the list to a parameter to be used in the view
        model.addAttribute("listAppointments", listAppointments);

        //return the view
        return "admin/manage_appointments";

    }

    //cancel an appointment
    @PostMapping("/admin/appointment/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id, //getting the appointment id by parameter via url
                                    RedirectAttributes ra) {

        try {
            //try to cancel
            appointmentService.cancelByAdminAndId(id);
            //if success set a message
            ra.addFlashAttribute("message", "appointment.message.cancelled");
        } catch (IllegalStateException ex) {
            //if error set an error message
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/manage_appointments";

    }

    //mark an appointment as completed
    @PostMapping("/admin/appointment/complete/{id}")
    public String completeAppointment(@PathVariable Long id, //getting the appointment id by parameter via url
                                    RedirectAttributes ra) {

        try {
            //try to cancel
            appointmentService.completeByAdminAndId(id);
            //if success set a message
            ra.addFlashAttribute("message", "appointment.message.completed");
        } catch (IllegalStateException ex) {
            //if error set an error message
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/manage_appointments";

    }

}
