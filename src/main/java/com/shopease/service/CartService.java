package com.shopease.service;

import com.shopease.model.CartItem;
import com.shopease.model.Product;
import com.shopease.model.User;
import com.shopease.repository.CartItemRepository;
import com.shopease.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CartService {

    private final CartItemRepository cartRepo;
    private final ProductRepository productRepo;

    public CartService(CartItemRepository cartRepo, ProductRepository productRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
    }

    public List<CartItem> getItems(User user) {
        return cartRepo.findByUser(user);
    }

    public int count(User user) {
        return cartRepo.findByUser(user).stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal total(List<CartItem> items) {
        return items.stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Total of the original (struck-through) prices, used to show the discount. */
    public BigDecimal mrpTotal(List<CartItem> items) {
        return items.stream().map(i -> {
            Product p = i.getProduct();
            BigDecimal base = (p.getMrp() != null && p.getMrp().compareTo(p.getPrice()) > 0) ? p.getMrp() : p.getPrice();
            return base.multiply(BigDecimal.valueOf(i.getQuantity()));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void add(User user, Long productId, int qty, String size) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalStateException("Product not found."));
        if (product.getStock() <= 0) {
            throw new IllegalStateException(product.getName() + " is out of stock.");
        }
        String chosen = (size == null || size.isBlank()) ? null : size.trim();
        if (!product.getSizeList().isEmpty()) {
            if (chosen == null || !product.getSizeList().contains(chosen)) {
                throw new IllegalStateException("Please select a size.");
            }
        } else {
            chosen = null;
        }
        final String finalSize = chosen;
        CartItem item = cartRepo.findByUserAndProductAndSize(user, product, finalSize)
                .orElseGet(() -> new CartItem(user, product, 0, finalSize));
        int wanted = item.getQuantity() + Math.max(qty, 1);
        item.setQuantity(Math.min(wanted, product.getStock()));
        cartRepo.save(item);
    }

    public void updateQuantity(User user, Long itemId, int qty) {
        cartRepo.findById(itemId).ifPresent(item -> {
            if (!item.getUser().getId().equals(user.getId())) return;
            if (qty <= 0) {
                cartRepo.delete(item);
            } else {
                item.setQuantity(Math.min(qty, item.getProduct().getStock()));
                cartRepo.save(item);
            }
        });
    }

    public void remove(User user, Long itemId) {
        cartRepo.findById(itemId).ifPresent(item -> {
            if (item.getUser().getId().equals(user.getId())) cartRepo.delete(item);
        });
    }

    public void clear(User user) {
        cartRepo.deleteAll(cartRepo.findByUser(user));
    }
}
