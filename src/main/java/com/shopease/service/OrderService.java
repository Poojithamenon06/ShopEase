package com.shopease.service;

import com.shopease.model.*;
import com.shopease.repository.CartItemRepository;
import com.shopease.repository.OrderRepository;
import com.shopease.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    /** Turns the user's cart into a Cash on Delivery order, reduces stock and empties the cart. */
    @Transactional
    public Order placeOrder(User user, CheckoutForm f) {
        List<CartItem> cart = cartItemRepository.findByUser(user);
        if (cart.isEmpty()) {
            throw new IllegalStateException("Your cart is empty.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setPaymentMethod("COD");
        order.setFullName(f.getFullName().trim());
        order.setPhone(f.getPhone().trim());
        order.setAddress(f.getAddress().trim());
        order.setCity(f.getCity().trim());
        order.setState(f.getState().trim());
        order.setPincode(f.getPincode().trim());

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal mrpTotal = BigDecimal.ZERO;
        for (CartItem ci : cart) {
            Product p = productRepository.findById(ci.getProduct().getId())
                    .orElseThrow(() -> new IllegalStateException("A product in your cart is no longer available."));
            if (p.getStock() < ci.getQuantity()) {
                throw new IllegalStateException(p.getStock() <= 0
                        ? p.getName() + " is out of stock. Please remove it from your cart."
                        : "Only " + p.getStock() + " left for " + p.getName() + ". Please update your cart.");
            }
            p.setStock(p.getStock() - ci.getQuantity());
            productRepository.save(p);

            OrderItem line = new OrderItem(order, p, ci.getSize(), ci.getQuantity());
            order.getItems().add(line);
            total = total.add(line.getSubtotal());
            mrpTotal = mrpTotal.add(line.getMrpSubtotal());
        }
        order.setTotal(total);
        order.setMrpTotal(mrpTotal);

        Order saved = orderRepository.save(order);
        cartItemRepository.deleteAll(cart);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Order> findByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public Optional<Order> findForUser(User user, Long id) {
        return orderRepository.findByIdAndUser(id, user);
    }

    /** Cancels an order that has not shipped yet and puts the items back in stock. */
    @Transactional
    public boolean cancel(User user, Long id) {
        Optional<Order> found = orderRepository.findByIdAndUser(id, user);
        if (found.isEmpty() || !found.get().isCancellable()) return false;
        Order order = found.get();
        for (OrderItem item : order.getItems()) {
            if (item.getProductId() == null) continue;
            productRepository.findById(item.getProductId()).ifPresent(p -> {
                p.setStock(p.getStock() + item.getQuantity());
                productRepository.save(p);
            });
        }
        order.setStatus(Order.CANCELLED);
        orderRepository.save(order);
        return true;
    }
}
