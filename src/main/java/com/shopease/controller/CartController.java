package com.shopease.controller;

import com.shopease.model.CartItem;
import com.shopease.model.User;
import com.shopease.service.CartService;
import com.shopease.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public String view(Principal principal, Model model) {
        User user = userService.getByEmail(principal.getName());
        List<CartItem> items = cartService.getItems(user);
        BigDecimal total = cartService.total(items);
        BigDecimal mrpTotal = cartService.mrpTotal(items);
        model.addAttribute("items", items);
        model.addAttribute("itemCount", items.stream().mapToInt(CartItem::getQuantity).sum());
        model.addAttribute("total", total);
        model.addAttribute("mrpTotal", mrpTotal);
        model.addAttribute("savings", mrpTotal.subtract(total));
        return "cart";
    }

    @PostMapping("/add/{productId}")
    public String add(@PathVariable Long productId,
                      @RequestParam(defaultValue = "1") int quantity,
                      @RequestParam(required = false) String size,
                      @RequestHeader(value = "Referer", required = false) String referer,
                      Principal principal, RedirectAttributes ra) {
        User user = userService.getByEmail(principal.getName());
        try {
            cartService.add(user, productId, quantity, size);
            ra.addFlashAttribute("success", "Item added to your cart.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/product/" + productId;
        }
        return "redirect:" + (referer != null ? referer : "/products");
    }

    @PostMapping("/update/{itemId}")
    public String update(@PathVariable Long itemId, @RequestParam int quantity,
                         Principal principal, RedirectAttributes ra) {
        cartService.updateQuantity(userService.getByEmail(principal.getName()), itemId, quantity);
        ra.addFlashAttribute("success", "Cart updated.");
        return "redirect:/cart";
    }

    @PostMapping("/remove/{itemId}")
    public String remove(@PathVariable Long itemId, Principal principal, RedirectAttributes ra) {
        cartService.remove(userService.getByEmail(principal.getName()), itemId);
        ra.addFlashAttribute("success", "Item removed from cart.");
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clear(Principal principal, RedirectAttributes ra) {
        cartService.clear(userService.getByEmail(principal.getName()));
        ra.addFlashAttribute("success", "Your cart is now empty.");
        return "redirect:/cart";
    }
}
