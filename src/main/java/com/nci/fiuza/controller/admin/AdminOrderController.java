package com.nci.fiuza.controller.admin;

import com.nci.fiuza.domain.Appointment;
import com.nci.fiuza.domain.Order;
import com.nci.fiuza.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

//controller for mapping the requests related to order in admin portal
@Controller
public class AdminOrderController {

    //i'll need to access OrderService on my methods
    private final OrderService orderService;

    //constructor injection so i can access the services on my methods
    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /* -------------------- manage orders --------------------*/
    @GetMapping("/admin/manage_orders")
    public String showManageOrders(Model model){

        //get the list of all orders
        List<Order> listOrders = orderService.listAllOrders();

        //add the list to a parameter to be used in the view
        model.addAttribute("listOrders", listOrders);

        return "admin/manage_orders";
    }

    /* -------------------- cancel an order --------------------*/
    @PostMapping("/admin/order/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId, //getting the order id by parameter via url
                              RedirectAttributes ra) {


        try {
            //try to cancel
            orderService.cancelOrderById(orderId);

            //if success set a message
            ra.addFlashAttribute("message", "order.message.cancelled");
        } catch (Exception e) {
            //if error set an error message
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/manage_orders";

    }

    /* -------------------- mark an order as paid --------------------*/
    @PostMapping("/admin/order/markAsPaid/{orderId}")
    public String markAsPaid(@PathVariable Long orderId, //getting the order id by parameter via url
                             RedirectAttributes ra) {
        try {
            //try to mark as paid
            orderService.markAsPaid(orderId);

            //if success set a message
            ra.addFlashAttribute("message", "order.message.paid");
        } catch (Exception e) {
            //if error set an error message
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/manage_orders";
    }

}
