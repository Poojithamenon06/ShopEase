package com.shopease.repository;

import com.shopease.model.CartItem;
import com.shopease.model.Product;
import com.shopease.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProductAndSize(User user, Product product, String size);
    void deleteByProduct(Product product);
}
