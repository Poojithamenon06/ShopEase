package com.shopease.controller;

import com.shopease.repository.UserRepository;
import com.shopease.service.CartService;
import com.shopease.service.ProductService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/** Adds data needed by the navbar to every page. */
@ControllerAdvice
public class GlobalModelAdvice {

    private final UserRepository userRepository;
    private final CartService cartService;
    private final ProductService productService;

    public GlobalModelAdvice(UserRepository userRepository, CartService cartService, ProductService productService) {
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.productService = productService;
    }

    @ModelAttribute
    public void addGlobals(Model model, Principal principal) {
        model.addAttribute("navCategories", productService.categories());
        if (principal != null) {
            userRepository.findByEmail(principal.getName()).ifPresent(user -> {
                model.addAttribute("displayName", user.getName());
                model.addAttribute("cartCount", cartService.count(user));
            });
        }
    }
}
