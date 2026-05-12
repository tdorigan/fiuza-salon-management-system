package com.nci.fiuza.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id //primary key on the db
    @GeneratedValue(strategy = GenerationType.IDENTITY) //to generate the ids automatically incremental
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    //store price at purchase, so when changing product price it wont affect orders
    @Column(name = "unit_price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPriceAtPurchase;

    //RELATIONSHIPS
    //many orderItems belong to one order
    @ManyToOne(optional = false, fetch = FetchType.LAZY) //instead of the default EAGER, so it won't load the objects (orders) from the db until it is actually used (getOrders())
    @JoinColumn(name = "order_id", nullable = false) //this will generate column order_id, FK to orders.id
    private Order order;

    //many orderItems belong to one product
    @ManyToOne(optional = false, fetch = FetchType.LAZY) //instead of the default EAGER, so it won't load the objects (product) from the db until it is actually used (getProduct())
    @JoinColumn(name = "product_id", nullable = false) //this will generate column product_id, FK to product.id
    private Product product;

    public OrderItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPriceAtPurchase() {
        return unitPriceAtPurchase;
    }

    public void setUnitPriceAtPurchase(BigDecimal unitPriceAtPurchase) {
        this.unitPriceAtPurchase = unitPriceAtPurchase;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
