package com.nci.fiuza.controller.admin;

import com.nci.fiuza.domain.Product;
import com.nci.fiuza.service.ProductService;
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

import java.io.IOException;

//controller for mapping the requests related to product in admin portal
@Controller
public class AdminProductController {

    //i'll access ProductService on my methods
    private final ProductService productService;

    //constructor injection so i can use ProductService in my methods
    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    /* -------------------- manage products --------------------*/
    @GetMapping("/admin/manage_products")
    public String showManageProducts(Model model){

        //getting the list of products from ProductService and passing it into a parameter
        model.addAttribute("listProducts", productService.listAll());

        return "admin/manage_products";

    }

    //create new product
    @GetMapping("/admin/products/new")
    public String newProduct(Model model){

        //passing a product object as parameter
        model.addAttribute("product", new Product());

        //passing page title as parameter to change from new/edit
        model.addAttribute("pageTitle", "product.form.new");

        return "admin/product_form";

    }

    //edit an existing product by id
    @GetMapping("/admin/products/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model, RedirectAttributes ra) { //getting the id by parameter via url

        try{
            //passing the product entity via parameter so it can be loaded in the form
            model.addAttribute("product", productService.get(id));

            //passing page title as parameter to change from new/edit
            model.addAttribute("pageTitle", "product.form.edit");

            return "admin/product_form";

        } catch (Exception e) {
            //setting error message if exception
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/manage_products";
        }

    }

    //to save a product from the post method of the form (edit or new)
    @PostMapping("/admin/products/save")
    public String saveProduct(@Valid Product product, //take the data from the html form and converts (binds) into the Product object and validate it against the domain/entity class
                              BindingResult bindingResult, //this must be just after the @Valid, it stores validation errors if they exist
                              @RequestParam("imageFile") MultipartFile imageFile, //get the image file by parameter
                              @RequestParam(value = "removeImage", required = false) boolean removeImage, //get the checkbox removeImage from parameter
                              RedirectAttributes ra, //to send a message before redirect
                              Model model) {

        //is product object doesnt have id it's because it's inserting a new product
        boolean isNewProduct = product.getId() == null;

        //if errors occurred when saving
        if(bindingResult.hasErrors()){

            //dynamically setting the page title, using the model because it's a normal return to a page
            if(isNewProduct){
                model.addAttribute("pageTitle", "product.form.new");
            } else {
                model.addAttribute("pageTitle", "product.form.edit");
            }

            //and return to the form
            return "admin/product_form";
        }

        //no errors occurred, proceed

        //try to save the product
        try {

            productService.save(product, imageFile, removeImage);

        } catch (Exception e) {

            //dynamically setting the page title, using the model because it's a normal return to a page
            model.addAttribute("pageTitle", isNewProduct ? "product.form.new" : "product.form.edit");

            //set error message
            model.addAttribute("errorMessage", e.getMessage());

            //and return to the form
            return "admin/product_form";

        }

        //dynamically setting success message, using the ra because it's a return redirect to a page
        if(isNewProduct){
            ra.addFlashAttribute("message", "product.message.created");
        } else {
            ra.addFlashAttribute("message", "product.message.updated");
        }

        return "redirect:/admin/manage_products";
    }

    //delete an existing product by id
    @PostMapping("/admin/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {

        try {
            //try to delete
            productService.delete(id);

            //if success adds a message
            ra.addFlashAttribute("message", "product.message.deleted");

        } catch (Exception e) {
            //if couldnt delete adds an error message
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        //redirects to list products page
        return "redirect:/admin/manage_products";
    }

    //toggle product active
    @PostMapping("/admin/products/toggleActive/{id}")
    public String toggleProduct(@PathVariable Long id, RedirectAttributes ra) { //getting the id by parameter via url

        try {
            //call toggle active from the service layer
            productService.toggleActive(id);

            //add a success message parameter
            ra.addFlashAttribute("message", "product.message.toggled");

        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/manage_products";
    }

}
