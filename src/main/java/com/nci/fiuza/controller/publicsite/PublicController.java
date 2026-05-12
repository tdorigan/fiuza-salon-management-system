package com.nci.fiuza.controller.publicsite;

import com.nci.fiuza.util.BusinessHours;
import com.nci.fiuza.dto.RegisterUserForm;
import com.nci.fiuza.service.ProductService;
import com.nci.fiuza.service.ServiceService;
import com.nci.fiuza.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.Locale;

//public controller for mapping the requests of the public website
@Controller
public class PublicController {

    //i'll access UserService on my methods
    private final UserService userService;

    //i'll access ServiceService on my methods
    private final ServiceService serviceService;

    //i'll access ProductService on my methods
    private final ProductService productService;

    //constructor injection so i can use the services in my methods
    public PublicController(UserService userService, ServiceService serviceService, ProductService productService) {
        this.userService = userService;
        this.serviceService = serviceService;
        this.productService = productService;
    }

    /* -------------------- PUBLIC WEBSITE --------------------*/
    @GetMapping({"/", "/public", "/public/"})
    public String showHomePage(Model model, Locale locale){

        //getting the opening hours from the utility class
        model.addAttribute("openingHours", BusinessHours.getOpeningHoursText(locale));

        return "public/index";
    }

    //--------------- portfolio ---------------------
    @GetMapping("/public/portfolio")
    public String showPortfolio(){
        return "public/portfolio";
    }

    //--------------- products ---------------------
    @GetMapping("/public/products")
    public String showProducts(Model model){

        //sending the list of active products by parameter to the view
        model.addAttribute("listProducts", productService.listActive());

        return "public/products";

    }

    //--------------- services ---------------------
    @GetMapping("/public/services")
    public String showServices(Model model){

        //sending the list of active services by parameter to the view
        model.addAttribute("listServices", serviceService.listActive());

        return "public/services";

    }

    //--------------- register ---------------------
    @GetMapping("/public/register")
    public String showRegisterForm(Model model){

        //passing to the view a new dto RegiserUserForm as parameter to be used in the registration form
        model.addAttribute("regiserUserForm", new RegisterUserForm());

        return "public/register";

    }

    //mapping the post method from the registration form
    @PostMapping("/public/register")
    public String createAccount(
            @Valid @ModelAttribute("regiserUserForm") RegisterUserForm form, //take the form data from the html and convert (binds) into the RegisterUserForm object and validate against the DTO
            BindingResult bindingResult, //this must be just after the @Valid, it stores validation errors if they exist
            Model model){

        //bean validation errors, in case any error occured on the user dto
        if (bindingResult.hasErrors()) {
            //in case has errors, return to the registration form
            return "public/register";
        }

        try {

            //try to register the user
            userService.registerCustomer(form);

            //if success adds a message
            model.addAttribute("message", "register.account.success");

        } catch (Exception e) {
            //if error adds an error message
            model.addAttribute("errorMessage", e.getMessage());
        }

        //returns to the register page
        return "public/register";

    }

    //--------------- login page ---------------------
    @GetMapping("/public/login")
    public String showLoginPage(Principal principal){

        //check the logged user, if not null, redirects to home page instead of login page
        if (principal != null) {
            return "redirect:/";
        }

        //if not logged in, go to login page
        return "public/login";
    }

}
