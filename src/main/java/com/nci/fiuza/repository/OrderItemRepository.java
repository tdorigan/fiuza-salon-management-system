package com.nci.fiuza.repository;

import com.nci.fiuza.domain.Order;
import com.nci.fiuza.domain.OrderItem;
import com.nci.fiuza.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    //this is like doing a select * from order_items where product_id = XXXX
    boolean existsByProductId(Long productId);

    //this is like a select * from order_items where order_id = XXXX and product_id = XXXX
    //using optional because may not find anything
    Optional<OrderItem> findByOrderAndProduct(Order order, Product product);

}
