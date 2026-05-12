package com.nci.fiuza.controller.admin;

import com.nci.fiuza.domain.Appointment;
import com.nci.fiuza.domain.AppointmentStatus;
import com.nci.fiuza.domain.Order;
import com.nci.fiuza.domain.OrderStatus;
import com.nci.fiuza.service.AppointmentService;
import com.nci.fiuza.service.OrderService;
import com.nci.fiuza.service.ServiceService;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

//controller for mapping the requests related to reports in admin portal
@Controller
public class AdminReportController {

    private final AppointmentService appointmentService;
    private final ServiceService serviceService;
    private final OrderService orderService;

    public AdminReportController(AppointmentService appointmentService, ServiceService serviceService, OrderService orderService) {
        this.appointmentService = appointmentService;
        this.serviceService = serviceService;
        this.orderService = orderService;
    }

    /* -------------------- report appointments page --------------------*/
    //get mapping used when called the page via menu, or when submitting the filters form
    @GetMapping("/admin/reports/appointments")
    public String showAppointmentsReport(Model model,
                                         //getting the filters via parameter, but not required, so when calling the page via menu they are null
                                         @RequestParam(required = false) String btnGenerate,
                                         @RequestParam(required = false) String strStartDate,
                                         @RequestParam(required = false) String strEndDate,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) Long serviceId,
                                         @RequestParam(required = false) String customerNameOrEmail
                                         ){

        //pass list of possible status via parameter for the dropdown
        model.addAttribute("statuses", appointmentService.listAppointmentStatuses());

        //pass list of services via parameter for the dropdown
        model.addAttribute("listServices", serviceService.listAll());

        //passing the filters back to the view via parameter
        model.addAttribute("strStartDate", strStartDate);
        model.addAttribute("strEndDate", strEndDate);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedServiceId", serviceId);
        model.addAttribute("customerNameOrEmail", customerNameOrEmail);

        //boolean to know if the generate button was pressed
        boolean clickedGenerate = btnGenerate != null;
        model.addAttribute("reportGenerated", clickedGenerate);

        //only generate report if generate button was clicked
        if (clickedGenerate) {

            //Convert String dates into LocalDate
            LocalDate startDate = null;
            LocalDate endDate = null;

            //convert start date
            if (strStartDate != null && !strStartDate.isBlank()) {
                try {
                    startDate = LocalDate.parse(strStartDate);
                } catch (Exception e) {
                    model.addAttribute("errorMessage", "reports.message.invalidStartDate");
                    return "admin/report_appointments";
                }
            }

            //convert end date
            if (strEndDate != null && !strEndDate.isBlank()) {
                try {
                    endDate = LocalDate.parse(strEndDate);
                } catch (Exception e) {
                    model.addAttribute("errorMessage", "reports.message.invalidEndDate");
                    return "admin/report_appointments";
                }
            }

            //convert status to AppointmentStatus enum
            AppointmentStatus appointmentStatus = null;

            if (status != null && !status.isBlank()) {

                try {
                    //convert String status into enum
                    appointmentStatus = AppointmentStatus.valueOf(status);
                } catch (Exception e) {
                    model.addAttribute("errorMessage", "reports.message.invalidAppointmentStatus");
                    return "admin/report_appointments";
                }

            }

            //get the list of all appointments by filters
            List<Appointment> appointmentsReportList = appointmentService.listAllAppointmentsByFilters(startDate, endDate, appointmentStatus, serviceId, customerNameOrEmail);

            //add the list to a parameter to be used in the view
            model.addAttribute("appointmentsReportList", appointmentsReportList);

            //calculate totals
            Integer totalAppointments = 0, totalBooked = 0, totalCompleted = 0, totalCancelled = 0;

            for (Appointment appointment : appointmentsReportList) {

                //total appointments
                totalAppointments++;

                //total booked
                if (appointment.getStatus().equals(AppointmentStatus.BOOKED)) {
                    totalBooked++;
                }

                // total completed
                if (appointment.getStatus().equals(AppointmentStatus.COMPLETED)) {
                    totalCompleted++;
                }

                // total cancelled
                if (appointment.getStatus().equals(AppointmentStatus.CANCELLED)) {
                    totalCancelled++;
                }

            }

            model.addAttribute("totalAppointments", totalAppointments);
            model.addAttribute("totalBooked", totalBooked);
            model.addAttribute("totalCompleted", totalCompleted);
            model.addAttribute("totalCancelled", totalCancelled);

        }

        return "admin/report_appointments";
    }

    /* -------------------- report orders page --------------------*/
    //get mapping used when called the page via menu, or when submitting the filters form
    @GetMapping("/admin/reports/orders")
    public String showOrdersReport(Model model,
                                   //getting the filters via parameter, but not required, so when calling the page via menu they are null
                                   @RequestParam(required = false) String btnGenerate,
                                   @RequestParam(required = false) String strStartDate,
                                   @RequestParam(required = false) String strEndDate,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String customerNameOrEmail
                                   ){

        //pass list of possible status via parameter for the dropdown
        model.addAttribute("statuses", orderService.listOrderStatuses());

        //passing the filters back to the view via parameter
        model.addAttribute("strStartDate", strStartDate);
        model.addAttribute("strEndDate", strEndDate);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("customerNameOrEmail", customerNameOrEmail);

        //boolean to know if the generate button was pressed
        boolean clickedGenerate = btnGenerate != null;
        model.addAttribute("reportGenerated", clickedGenerate);

        //only generate report if generate button was clicked
        if (clickedGenerate) {

            //Convert String dates into LocalDate
            LocalDate startDate = null;
            LocalDate endDate = null;

            //convert start date
            if (strStartDate != null && !strStartDate.isBlank()) {
                try {
                    startDate = LocalDate.parse(strStartDate);
                } catch (Exception e) {
                    model.addAttribute("errorMessage", "reports.message.invalidStartDate");
                    return "admin/report_orders";
                }
            }

            //convert end date
            if (strEndDate != null && !strEndDate.isBlank()) {
                try {
                    endDate = LocalDate.parse(strEndDate);
                } catch (Exception e) {
                    model.addAttribute("errorMessage", "reports.message.invalidEndDate");
                    return "admin/report_orders";
                }
            }

            //convert status to OrderStatus enum
            OrderStatus orderStatus = null;

            if (status != null && !status.isBlank()) {

                try {
                    //convert String status into enum
                    orderStatus = OrderStatus.valueOf(status);
                } catch (Exception e) {
                    model.addAttribute("errorMessage", "reports.message.invalidOrderStatus");
                    return "admin/report_orders";
                }

            }

            //get the list of all orders by filters
            List<Order> orderReportList = orderService.listAllOrdersByFilters(startDate, endDate, orderStatus, customerNameOrEmail);

            //add the list to a parameter to be used in the view
            model.addAttribute("orderReportList", orderReportList);

            //calculate totals
            Integer totalOrders = 0, totalCancelled = 0, totalDraft = 0, totalPending = 0, totalPaid = 0;
            BigDecimal totalOrdersValue = BigDecimal.ZERO,
                    totalCancelledValue = BigDecimal.ZERO,
                    totalDraftValue = BigDecimal.ZERO,
                    totalPendingValue = BigDecimal.ZERO,
                    totalPaidValue = BigDecimal.ZERO;

            for (Order order : orderReportList) {

                //total order
                totalOrders++;

                //total orders value
                totalOrdersValue = totalOrdersValue.add(order.getTotalOrder());

                //total cancelled
                if (order.getStatus().equals(OrderStatus.CANCELLED)) {
                    totalCancelled++;
                    totalCancelledValue = totalCancelledValue.add(order.getTotalOrder());
                }

                //total draft
                if (order.getStatus().equals(OrderStatus.DRAFT)) {
                    totalDraft++;
                    totalDraftValue = totalDraftValue.add(order.getTotalOrder());
                }

                //total pending
                if (order.getStatus().equals(OrderStatus.PENDING)) {
                    totalPending++;
                    totalPendingValue = totalPendingValue.add(order.getTotalOrder());
                }

                //total paid
                if (order.getStatus().equals(OrderStatus.PAID)) {
                    totalPaid++;
                    totalPaidValue = totalPaidValue.add(order.getTotalOrder());
                }

            }

            //pass the totals to the view
            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("totalOrdersValue", totalOrdersValue);
            model.addAttribute("totalCancelled", totalCancelled);
            model.addAttribute("totalCancelledValue", totalCancelledValue);
            model.addAttribute("totalDraft", totalDraft);
            model.addAttribute("totalDraftValue", totalDraftValue);
            model.addAttribute("totalPending", totalPending);
            model.addAttribute("totalPendingValue", totalPendingValue);
            model.addAttribute("totalPaid", totalPaid);
            model.addAttribute("totalPaidValue", totalPaidValue);

        }

        return "admin/report_orders";
    }

}
