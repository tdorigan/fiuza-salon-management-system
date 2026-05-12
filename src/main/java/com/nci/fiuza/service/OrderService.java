package com.nci.fiuza.service;

import com.nci.fiuza.domain.*;
import com.nci.fiuza.repository.OrderItemRepository;
import com.nci.fiuza.repository.OrderRepository;
import com.nci.fiuza.repository.ProductRepository;
import com.nci.fiuza.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    //i'll need access to the OrderRepository
    private final OrderRepository orderRepo;

    //i'll need access to the ProductRepository
    private final ProductRepository productRepo;

    //i'll need access to the OrderItemRepository
    private final OrderItemRepository orderItemRepo;

    //i'll need access to the UserRepository
    private final UserRepository userRepo;

    //constructor injection so i can use the repos on my methods
    public OrderService(OrderRepository orderRepo, ProductRepository productRepo, OrderItemRepository orderItemRepo, UserRepository userRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.orderItemRepo = orderItemRepo;
        this.userRepo = userRepo;
    }

    //list customer order history (All except DRAFT)
    public List<Order> listCustomerOrderHistory(Long customerId) {

        //to get the list of orders of a customer with status different of DRAFT (to get the order history)
        return orderRepo.findOrderByCustomerIdAndStatusNot(customerId, OrderStatus.DRAFT);
    }

    //list customer current order (DRAFT)
    public Order getCustomerCurrentOrder(Long customerId) {

        //to get the list of orders of a customer with status equals DRAFT (current order)
        return orderRepo.findOrderByCustomerIdAndStatus(customerId, OrderStatus.DRAFT);

    }

    //get the list of all orders
    public List<Order> listAllOrders(){
        return orderRepo.findAll();
    }

    //to add a product to the customer's current order
    @Transactional
    public void addProductToCurrentOrder(Long customerId, Long productId) {

        //get the product, as findById returns an optional i can test and if not found i throw an exception
        Product product = productRepo.findById(productId).orElseThrow(() -> new IllegalStateException("product.message.notFound"));

        //check if the product is active, if not, throw exception
        if (!product.isActive()) {
            throw new IllegalStateException("order.message.productInactive");
        }

        //check stock quantity, if less than one, throw exception
        if (product.getQuantityInStock() < 1) {
            throw new IllegalStateException("order.message.outOfStock");
        }

        //create current order
        Order currentOrder;

        //check if current order (DRAFT) already exists for the customer
        if (getCustomerCurrentOrder(customerId) != null) {
            //if exists, get the customer current order
            currentOrder = getCustomerCurrentOrder(customerId);
        } else {

            //if there is no current order, create one

            //get the user (customer)
            User customer = userRepo.findById(customerId).orElseThrow(() -> new IllegalStateException("user.message.notFound"));

            currentOrder = new Order();
            currentOrder.setCustomer(customer);
            currentOrder.setDateTime(LocalDateTime.now()); //getting the current local date and time
            currentOrder.setStatus(OrderStatus.DRAFT); //current order is always DRAFT

            //save the current order
            orderRepo.save(currentOrder);

        }

        //check if added item (product) already exists in the current order, if so, increment quantity, otherwise create order item
        Optional<OrderItem> existingItem = orderItemRepo.findByOrderAndProduct(currentOrder, product);

        if (existingItem.isPresent()) {

            //get the existing item
            OrderItem item = existingItem.get();

            //check stock quantity again, if less than current quantity + one, throw exception
            if (product.getQuantityInStock() < item.getQuantity() + 1) {
                throw new IllegalStateException("order.message.outOfStock");
            }

            //if stock quantity ok proceeds

            item.setQuantity(item.getQuantity() + 1); //increment quantity by 1
            item.setUnitPriceAtPurchase(product.getPrice()); //set price at purchase

            //updated existing order item
            orderItemRepo.save(item);

        } else {

            //create new order item
            OrderItem item = new OrderItem();
            item.setOrder(currentOrder);
            item.setProduct(product);
            item.setQuantity(1); //every time user click buy, adds with quantity 1. if they want to increase quantity they do through current order page
            item.setUnitPriceAtPurchase(product.getPrice()); //get the current price of the product

            //inserts the new item
            orderItemRepo.save(item);

        }

    }

    //removes an item from the current order
    @Transactional
    public void removeItemFromCurrentOrder(Long customerId, Long itemId) {

        //get the current order (draft)
        Order currentOrder = getCustomerCurrentOrder(customerId);

        //get the item to be removed
        OrderItem item = orderItemRepo.findById(itemId).orElseThrow(() -> new IllegalStateException("order.message.itemNotFound"));

        //check if the item to be removed belongs to the correct current order, to prevent deleting items from other orders via url
        if (!item.getOrder().equals(currentOrder)) {
            throw new IllegalStateException("order.message.invalidAccess");
        }

        //delete the item
        orderItemRepo.delete(item);

        //after deletion, if there are no items on the current order, deletes the current order
        if (currentOrder.getItems().isEmpty()) {
            orderRepo.delete(currentOrder);
        }

    }

    //place customer current order
    @Transactional
    public void placeCurrentOrder(Long customerId) {

        //get the current order of the customer
        Order currentOrderorder = getCustomerCurrentOrder(customerId);

        //check if current order is available and has items, to prevent calling this directly through the url when there is no current order
        if (currentOrderorder == null || currentOrderorder.getItems().isEmpty()) {
            throw new IllegalStateException("order.current.empty");
        }

        //first a loop just to validate items before changing their state, so if i throw exception on the last items, the earlier items from the loop don't get changed
        for (OrderItem item : currentOrderorder.getItems()) { //for each item of the current order

            //get the product
            Product product = item.getProduct();

            //check if product is still active
            if (!product.isActive()) {
                throw new IllegalStateException("order.message.productInactive");
            }

            //check if theres enough quantity in stock
            if (item.getQuantity() > product.getQuantityInStock()) {
                throw new IllegalStateException("order.message.insufficientStock");
            }

        }

        //if validations ok, proceeds

        //now a loop to change the states of the items and products
        for (OrderItem item : currentOrderorder.getItems()) { //for each item of the current order

            //get the product
            Product product = item.getProduct();

            //decrements quantity in stock, current product quantity - orderItem quantity
            product.setQuantityInStock(product.getQuantityInStock() - item.getQuantity());

            //save product
            productRepo.save(product);

            //updates the price at purchase, in case it got changed from the time it was added on the order until when it got placed
            item.setUnitPriceAtPurchase(product.getPrice());

            //save the item
            orderItemRepo.save(item);

        }

        //set status as pending
        currentOrderorder.setStatus(OrderStatus.PENDING);

        //save the order
        orderRepo.save(currentOrderorder);

    }

    //cancel order by customer and id, just check if order belongs to logged user then calls cancel order by id
    @Transactional
    public void cancelByCustomerAndId(Long customerId, Long orderId) {

        //get the order
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new IllegalStateException("order.message.notFound"));

        //this is to avoid canceling orders from another user via url, check if the order id belongs to the logged customer
        if (order.getCustomer() == null || !order.getCustomer().getId().equals(customerId)) {
            throw new IllegalStateException("order.message.notYours");
        }

        //call cancel by id
        this.cancelOrderById(orderId);

    }

    //helper method to cancel order by id
    @Transactional
    public void cancelOrderById(Long orderId){

        //get the order
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new IllegalStateException("order.message.notFound"));

        //only cancel orders in PENDING status, no point cancelling if already CANCELLED or if PAID
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("order.message.cannotCancelStatus");
        }

        //update stock
        for (OrderItem item : order.getItems()) { //for each item in the order

            //get the product
            Product product = item.getProduct();

            //get the item quantity and adds it back to stock
            product.setQuantityInStock(product.getQuantityInStock() + item.getQuantity());

            //save the product
            productRepo.save(product);

        }

        //set the status to CANCELLED
        order.setStatus(OrderStatus.CANCELLED);

        //save the order
        orderRepo.save(order);

    }

    //mark order as paid
    @Transactional
    public void markAsPaid(Long orderId) {

        //get the order
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new IllegalStateException("order.message.notFound"));

        //only mark order as paid if in PENDING status, no point marking as paid if already paid or if cancelled or draft
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("order.message.cannotMarkAsPaidStatus");
        }

        //set status as paid
        order.setStatus(OrderStatus.PAID);

        //save the order
        orderRepo.save(order);

    }

    //decrease item quantity from current order
    @Transactional
    public void decreaseItemQuantityFromCurrentOrder(Long customerId, Long itemId) {

        //get the current order (draft)
        Order currentOrder = getCustomerCurrentOrder(customerId);

        //get the item to be changed
        OrderItem item = orderItemRepo.findById(itemId).orElseThrow(() -> new IllegalStateException("order.message.itemNotFound"));

        //check if the item to be changed belongs to the correct current order, to prevent changing items from other orders via url
        if (!item.getOrder().equals(currentOrder)) {
            throw new IllegalStateException("order.message.invalidAccess");
        }

        //if quantity equals to 1, can't decrease
        if (item.getQuantity() <= 1) {
            throw new IllegalStateException("item.message.invalidQuantity");
        }

        //decrease item quantity by 1
        item.setQuantity(item.getQuantity() - 1);

        //save item
        orderItemRepo.save(item);

    }

    //increase item quantity from current order
    @Transactional
    public void increaseItemQuantityFromCurrentOrder(Long customerId, Long itemId) {

        //get the current order (draft)
        Order currentOrder = getCustomerCurrentOrder(customerId);

        //get the item to be changed
        OrderItem item = orderItemRepo.findById(itemId).orElseThrow(() -> new IllegalStateException("order.message.itemNotFound"));

        //check if the item to be changed belongs to the correct current order, to prevent changing items from other orders via url
        if (!item.getOrder().equals(currentOrder)) {
            throw new IllegalStateException("order.message.invalidAccess");
        }

        //check stock quantity, if less than current quantity + one, throw exception
        if (item.getProduct().getQuantityInStock() < item.getQuantity() + 1) {
            throw new IllegalStateException("order.message.outOfStock");
        }

        //increase item quantity by 1
        item.setQuantity(item.getQuantity() + 1);

        //save item
        orderItemRepo.save(item);

    }

    //return the possible values for order status
    public OrderStatus[] listOrderStatuses() {

        //return the values from the enum
        return OrderStatus.values();

    }

    public List<Order> listAllOrdersByFilters(LocalDate startDate, LocalDate endDate, OrderStatus orderStatus, String customerNameOrEmail) {

        //adapted from https://medium.com/@AlexanderObregon/search-filters-in-spring-boot-apis-without-complex-query-builders-dcb69a0453c9
        Specification<Order> spec = Specification.where((root, query, cb) -> cb.conjunction());

        //filter by dates
        if (startDate != null && endDate != null) { //if both are not null, filter between

            //convert LocalDate into LocalDateTime, considering the whole day,
            //so start is from chosen date + time at the beginning of the day,
            //and end is from chosen date + time at the end of the day
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("dateTime"), startDateTime, endDateTime)
            );

        } else if (startDate != null && endDate == null) { //if only start date not null, filter greaterThanOrEqualTo

            //convert LocalDate into LocalDateTime, considering the whole day,
            //so start is from chosen date + time at the beginning of the day,
            LocalDateTime startDateTime = startDate.atStartOfDay();

            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("dateTime"), startDateTime)
            );

        } else if (startDate == null && endDate != null) { //if only end date not null, filter lessThanOrEqualTo

            //convert LocalDate into LocalDateTime, considering the whole day,
            //so end is from chosen date + time at the end of the day
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("dateTime"), endDateTime)
            );

        }

        //filter by status
        if (orderStatus != null) {

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), orderStatus)
            );

        }

        //filter by customer name or email
        if (customerNameOrEmail != null && !customerNameOrEmail.isBlank()) {

            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(root.get("customer").get("fullName"), "%" + customerNameOrEmail.toLowerCase() + "%"),
                            cb.like(root.get("customer").get("email"), "%" + customerNameOrEmail.toLowerCase() + "%")
                    )
            );

        }

        return orderRepo.findAll(spec);

    }
}
