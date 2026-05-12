package com.nci.fiuza.controller.customer;

import com.nci.fiuza.domain.Order;
import com.nci.fiuza.service.OrderService;
import com.nci.fiuza.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

//controller for mapping the requests related to order in the customer area
@Controller
public class CustomerOrderController {

    //i'll need to use OrderService on my methods
    private final OrderService orderService;
    private final UserService userService;

    //constructor injection so i can use the services on my methods
    public CustomerOrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    /* -------------------- show current order --------------------*/
    @GetMapping("/customer/current_order")
    public String showCurrentOrder(Model model, Principal principal){

        //get the user id of the logged user (customer)
        Long customerId = userService.findByEmail(principal.getName()).getId();

        //get the current order (DRAFT)
        Order currentOrder = orderService.getCustomerCurrentOrder(customerId);

        //get the customer current order (DRAFT), pass it as a parameter to the view
        model.addAttribute("currentOrder", currentOrder);

        return "customer/current_order";
    }

    /* -------------------- show order history --------------------*/
    @GetMapping("/customer/orders")
    public String showOrders(Model model, Principal principal){

        //get the user id of the logged user (customer)
        Long customerId = userService.findByEmail(principal.getName()).getId();

        //get the customer order history list and pass it as a parameter to the view
        model.addAttribute("listOrders", orderService.listCustomerOrderHistory(customerId));

        return "customer/orders";
    }

    /* -------------------- add a product to the current order --------------------*/
    @PostMapping("/customer/order/add/{productId}")
    public String addToCurrentOrder(@PathVariable Long productId, //getting product id via parameter
                                    RedirectAttributes ra,
                                    Principal principal){ //getting logged user

        try {
            //get the id of the logged user
            Long customerId = userService.findByEmail(principal.getName()).getId();

            //try adding the product to current order
            orderService.addProductToCurrentOrder(customerId, productId);

            //success message
            ra.addFlashAttribute("message", "order.message.added");
        } catch (Exception e) {
            //error message
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        //redirect to public products
        return "redirect:/public/products";
    }

    /* -------------------- delete an item from the current order --------------------*/
    @PostMapping("/customer/current_order/delete/{itemId}")
    public String deleteItemCurrentOrder(@PathVariable Long itemId, //getting product id via parameter
                                         RedirectAttributes ra,
                                         Model model,
                                         Principal principal) {


        try {

            //get the id of the logged user
            Long customerId = userService.findByEmail(principal.getName()).getId();

            //try to delete
            orderService.removeItemFromCurrentOrder(customerId, itemId);

            //if success adds a message
            ra.addFlashAttribute("message", "product.message.deleted");

        } catch (Exception e) {
            //if couldnt delete adds an error message
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        //redirects to current order page
        return "redirect:/customer/current_order";

    }

    /* -------------------- place current order --------------------*/
    @PostMapping("/customer/current_order/place")
    public String placeOrder(Principal principal,
                             RedirectAttributes ra) {

        try {

            //get the id of the logged user
            Long customerId = userService.findByEmail(principal.getName()).getId();

            //place customer current order
            orderService.placeCurrentOrder(customerId);

            //if success adds a message
            ra.addFlashAttribute("message", "order.message.placed");

            //if ok redirects to my orders page
            return "redirect:/customer/orders";

        } catch (Exception e) {
            //if couldnt delete adds an error message
            ra.addFlashAttribute("errorMessage", e.getMessage());

            //if not ok keeps in the current order page
            return "redirect:/customer/current_order";

        }

    }

    /* -------------------- cancel an order --------------------*/
    @PostMapping("/customer/order/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId, //getting the orderId id by parameter via url
                              Principal principal, //getting the logged user to prevent cancel orders from other users via url
                              RedirectAttributes ra) {

        try {
            //get the id of the logged user
            Long loggedUserId = userService.findByEmail(principal.getName()).getId();

            orderService.cancelByCustomerAndId(loggedUserId, orderId);

            ra.addFlashAttribute("message", "order.message.cancelled");

        } catch (Exception e) {
            //if error set an error message
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/customer/orders";

    }

    /* -------------------- decrease item quantity from current order --------------------*/
    @PostMapping("/customer/current_order/decreaseItemQuantity/{itemId}")
    public String decreaseItemQuantity(@PathVariable Long itemId, //getting the itemId id by parameter via url
                                       Principal principal, //getting the logged user to prevent changing items from other users via url
                                       RedirectAttributes ra) {

        try {

            //get the id of the logged user
            Long customerId = userService.findByEmail(principal.getName()).getId();

            //try to decrease item quantity
            orderService.decreaseItemQuantityFromCurrentOrder(customerId, itemId);

        } catch (Exception e) {
            //if couldnt delete adds an error message
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        //redirects to current order page
        return "redirect:/customer/current_order";

    }

    /* -------------------- increase item quantity from current order --------------------*/
    @PostMapping("/customer/current_order/increaseItemQuantity/{itemId}")
    public String increaseItemQuantity(@PathVariable Long itemId, //getting the itemId id by parameter via url
                                       Principal principal, //getting the logged user to prevent changing items from other users via url
                                       RedirectAttributes ra) {

        try {

            //get the id of the logged user
            Long customerId = userService.findByEmail(principal.getName()).getId();

            //try to increase item quantity
            orderService.increaseItemQuantityFromCurrentOrder(customerId, itemId);

        } catch (Exception e) {
            //if couldnt delete adds an error message
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        //redirects to current order page
        return "redirect:/customer/current_order";

    }

}
