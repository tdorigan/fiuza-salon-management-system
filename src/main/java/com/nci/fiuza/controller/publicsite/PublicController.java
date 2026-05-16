package com.nci.fiuza.controller.publicsite;

import com.nci.fiuza.domain.Product;
import com.nci.fiuza.domain.Service;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

        //gets the active products from the database
        List<Product> products = productService.listActive();

        //creates a map where key = product id and value = product image URL
        Map<Long, String> productImageUrlMap = new HashMap<>();

        //loops through each product to prepare its image URL
        for (Product product : products) {

            //builds the Cloudflare R2 image URL only if the image exists
            String imageUrl = productService.buildProductImageUrl(product);

            //stores the image URL in the map using product id as key
            productImageUrlMap.put(product.getId(), imageUrl);

        }

        //sends the list of active products to the view
        model.addAttribute("listProducts", products);

        //sends the product image URL map to the view
        model.addAttribute("productImageUrlMap", productImageUrlMap);

        //returns the public products page
        return "public/products";

    }

    //--------------- services ---------------------
    @GetMapping("/public/services")
    public String showServices(Model model){

        //gets the active services from the database
        List<Service> services = serviceService.listActive();

        //creates a map where key = service id and value = service image URL
        Map<Long, String> serviceImageUrlMap = new HashMap<>();

        //loops through each service to prepare its image URL
        for (Service service : services) {

            //builds the Cloudflare R2 image URL only if the image exists
            String imageUrl = serviceService.buildServiceImageUrl(service);

            //stores the image URL in the map using service id as key
            serviceImageUrlMap.put(service.getId(), imageUrl);
        }

        //sends the list of active services to the view
        model.addAttribute("listServices", services);

        //sends the service image URL map to the view
        model.addAttribute("serviceImageUrlMap", serviceImageUrlMap);

        //returns the public services page
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
