package com.nci.fiuza.controller.admin;

import com.nci.fiuza.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//controller for mapping the requests related to customer in admin portal
@Controller
public class AdminCustomerController {

    //i'll access UserService on my methods
    private final UserService userService;

    //constructor injection so i can use UserService in my methods
    public AdminCustomerController(UserService userService) {
        this.userService = userService;
    }

    /* -------------------- manage customers --------------------*/
    @GetMapping("/admin/manage_customers")
    public String showManageCustomers(Model model){

        //adding parameter from service to be used on the template (view)
        model.addAttribute("listCustomers", userService.listCustomers());

        return "admin/manage_customers";
    }

    /* -------------------- enable/disable customers --------------------*/
    @PostMapping("/admin/customers/toggleActive/{customerId}")
    public String toggleCustomerActive(@PathVariable Long customerId, RedirectAttributes ra) {

        try {
            //call toggle active from the service layer
            userService.toggleActive(customerId);

            //add a success message parameter
            ra.addFlashAttribute("message", "customer.message.toggled");

        } catch (Exception e) {
            //if exception adds an error message
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/manage_customers";

    }

    /* -------------------- delete a customer --------------------*/
    @PostMapping("/admin/customers/delete/{customerId}")
    public String deleteCustomer(@PathVariable Long customerId, RedirectAttributes ra) {

        try {
            //try to delete
            userService.delete(customerId);

            //if success adds a message
            ra.addFlashAttribute("message", "customer.message.deleted");

        } catch (Exception e) {
            //if couldnt delete adds an error message
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        //redirects to list customers page
        return "redirect:/admin/manage_customers";

    }


}
