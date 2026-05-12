package com.nci.fiuza.repository;

import com.nci.fiuza.domain.Appointment;
import com.nci.fiuza.domain.Order;
import com.nci.fiuza.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    //this is like doing a select * from orders where customer = XXXX and status != XXXX
    //so i can take the order history of a customer, that is, all their orders with status different of DRAFT
    List<Order> findOrderByCustomerIdAndStatusNot(Long customerId, OrderStatus status);

    //this is like doing a select * from orders where customer = XXXX and status = XXXX
    //so i can take the draft order of a customer
    Order findOrderByCustomerIdAndStatus(Long customerId, OrderStatus status);

    //this is like doing a select * from orders where customer_id = XXXX
    boolean existsByCustomerId(Long customerId);

}
