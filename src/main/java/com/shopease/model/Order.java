package com.shopease.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** A customer order (payment: Cash on Delivery). */
@Entity
@Table(name = "orders")
public class Order {

    public static final String PLACED = "PLACED";
    public static final String SHIPPED = "SHIPPED";
    public static final String DELIVERED = "DELIVERED";
    public static final String CANCELLED = "CANCELLED";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String status = PLACED;

    @Column(nullable = false)
    private String paymentMethod = "COD";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal mrpTotal = BigDecimal.ZERO;

    private String fullName;
    private String phone;
    @Column(length = 500)
    private String address;
    private String city;
    private String state;
    private String pincode;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public String getOrderNumber() { return "SE" + (100000 + (id == null ? 0 : id)); }
    public BigDecimal getSavings() { return mrpTotal.subtract(total); }
    public int getItemCount() { return items.stream().mapToInt(OrderItem::getQuantity).sum(); }
    public boolean isCancellable() { return PLACED.equals(status); }
    public String getStatusLabel() {
        switch (status) {
            case SHIPPED: return "Shipped";
            case DELIVERED: return "Delivered";
            case CANCELLED: return "Cancelled";
            default: return "Order placed";
        }
    }
    public String getPaymentLabel() { return "Cash on Delivery"; }
    public String getPlacedOnText() { return createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")); }
    public String getExpectedDeliveryText() { return createdAt.plusDays(5).format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public BigDecimal getMrpTotal() { return mrpTotal; }
    public void setMrpTotal(BigDecimal mrpTotal) { this.mrpTotal = mrpTotal; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
