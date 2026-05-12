package com.nci.fiuza.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id //primary key on the db
    @GeneratedValue(strategy = GenerationType.IDENTITY) //to generate the ids automatically incremental
    private Long id;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status; //PENDING / PAID / CANCELLED / DRAFT

    //RELATIONSHIPS
    //many orders belong to one customer (user)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    //one order has many items
    //mappedBy because the foreign key order_id is in OrderItem
    //cascade so when we delete orders it will delete the items as well
    //orphanremoval so that when we remove an item from order.getItems().remove(item), it will delete the row in order_items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    //constructor
    public Order() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    /*public method to calculate the total of the order*/
    public BigDecimal getTotalOrder() {

        //initializes total
        BigDecimal total = BigDecimal.ZERO;

        //for each item
        for (OrderItem item : this.getItems()) {
            //accumulate item price at purchase * quantity
            total = total.add(item.getUnitPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        //return total
        return total;
    }

}
