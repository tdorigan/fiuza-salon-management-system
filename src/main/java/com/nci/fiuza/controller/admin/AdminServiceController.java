package com.nci.fiuza.controller.admin;

import com.nci.fiuza.domain.Service;
import com.nci.fiuza.service.ServiceService;
import com.nci.fiuza.util.DurationUtils;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//controller for mapping the requests related to services in admin portal
@Controller
public class AdminServiceController {

    //i'll access ServiceService on my methods
    private final ServiceService serviceService;

    //constructor injection so i can use ServiceService in my methods
    public AdminServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    //manage/list services page
    @GetMapping("/admin/manage_services")
    public String showManageServices(Model model){

        //getting the list of services from ServiceService and passing it into a parameter
        model.addAttribute("listServices", serviceService.listAll());

        return "admin/manage_services";

    }

    //create new service
    @GetMapping("/admin/services/new")
    public String newService(Model model){

        //passing a service object as parameter
        model.addAttribute("service", new Service());

        //passing page title as parameter to change from new/edit
        model.addAttribute("pageTitle", "service.form.new");

        //get the service durations options from the utility class and pass to the view
        model.addAttribute("durationOptions", DurationUtils.getServiceDurationOptions());

        return "admin/service_form";

    }

    //edit an existing service by id
    @GetMapping("/admin/services/edit/{id}")
    public String editService(@PathVariable Long id, Model model, RedirectAttributes ra) { //getting the id by parameter via url

        try{
            //passing the service entity via parameter so it can be loaded in the form
            model.addAttribute("service", serviceService.get(id));

            //passing page title as parameter to change from new/edit
            model.addAttribute("pageTitle", "service.form.edit");

            //get the service durations options from the utility class and pass to the view
            model.addAttribute("durationOptions", DurationUtils.getServiceDurationOptions());

            return "admin/service_form";

        } catch (Exception e) {
            //setting error message if exception
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/manage_services";
        }

    }

    //to save a service from the post method of the form (edit or new)
    @PostMapping("/admin/services/save")
    public String saveService(@Valid Service service, //take the data from the html form and converts (binds) into the Service object and validate it against the domain/entity class
                              BindingResult bindingResult, //this must be just after the @Valid, it stores validation errors if they exist
                              @RequestParam("imageFile") MultipartFile imageFile, //receives the uploaded image file
                              @RequestParam(value = "removeImage", required = false) boolean removeImage, //receives the remove image checkbox value
                              RedirectAttributes ra, //to send a message after redirect
                              Model model) {

        //is service object doesnt have id it's because it's inserting a new service
        boolean isNewService = service.getId() == null;

        //if errors occurred when saving
        if(bindingResult.hasErrors()){

            //dynamically setting the page title, using the model because it's a normal return to a page
            if(isNewService){
                model.addAttribute("pageTitle", "service.form.new");
            } else {
                model.addAttribute("pageTitle", "service.form.edit");
            }

            //get the service durations options from the utility class and pass to the view
            model.addAttribute("durationOptions", DurationUtils.getServiceDurationOptions());

            //and return to the form
            return "admin/service_form";
        }

        //no errors occurred

        //save the service
        try {
            //saves the service with the uploaded image or image removal option
            serviceService.save(service, imageFile, removeImage);

        } catch (Exception e) {

            //sets the correct page title again if something goes wrong while saving the image
            if (isNewService) {
                model.addAttribute("pageTitle", "service.form.new");
            } else {
                model.addAttribute("pageTitle", "service.form.edit");
            }

            //get the service durations options from the utility class and pass to the view
            model.addAttribute("durationOptions", DurationUtils.getServiceDurationOptions());

            //sends the error message back to the form
            model.addAttribute("errorMessage", e.getMessage());

            //returns to the service form
            return "admin/service_form";
        }

        //dynamically setting success message, using the ra because it's a return redirect to a page
        if(isNewService){
            ra.addFlashAttribute("message", "service.message.created");
        } else {
            ra.addFlashAttribute("message", "service.message.updated");
        }

        return "redirect:/admin/manage_services";
    }

    //delete an existing service by id
    @PostMapping("/admin/services/delete/{id}")
    public String deleteService(@PathVariable Long id, RedirectAttributes ra) {

        try {
            //try to delete
            serviceService.delete(id);

            //if success adds a message
            ra.addFlashAttribute("message", "service.message.deleted");

        } catch (Exception e) {
            //if couldnt delete adds an error message
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        //redirects to list services page
        return "redirect:/admin/manage_services";
    }

    //toggle service active
    @PostMapping("/admin/services/toggleActive/{id}")
    public String toggleService(@PathVariable Long id, RedirectAttributes ra) { //getting the id by parameter via url

        try {
            //call toggle active form the service layer
            serviceService.toggleActive(id);

            //add a success message parameter
            ra.addFlashAttribute("message", "service.message.toggled");

        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/manage_services";
    }

}
