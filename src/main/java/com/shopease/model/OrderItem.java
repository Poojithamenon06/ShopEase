package com.shopease.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** One line of an order. Product details are copied so the order stays correct if the product changes later. */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    /** Plain id (not a foreign key) so products can still be deleted by the admin. */
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(length = 1000)
    private String imageUrl;

    @Column(name = "size_label")
    private String size;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitMrp;

    public OrderItem() {}

    public OrderItem(Order order, Product p, String size, int quantity) {
        this.order = order;
        this.productId = p.getId();
        this.productName = p.getName();
        this.imageUrl = p.getImageUrl();
        this.size = size;
        this.quantity = quantity;
        this.unitPrice = p.getPrice();
        this.unitMrp = (p.getMrp() != null && p.getMrp().compareTo(p.getPrice()) > 0) ? p.getMrp() : p.getPrice();
    }

    public BigDecimal getSubtotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
    public BigDecimal getMrpSubtotal() { return unitMrp.multiply(BigDecimal.valueOf(quantity)); }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getUnitMrp() { return unitMrp; }
    public void setUnitMrp(BigDecimal unitMrp) { this.unitMrp = unitMrp; }
}
