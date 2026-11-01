package com.shopease.controller;

import com.shopease.model.CartItem;
import com.shopease.model.CheckoutForm;
import com.shopease.model.Order;
import com.shopease.model.User;
import com.shopease.service.CartService;
import com.shopease.service.OrderService;
import com.shopease.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;

    public OrderController(CartService cartService, OrderService orderService, UserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.userService = userService;
    }

    // ---------- Checkout (Cash on Delivery) ----------
    @GetMapping("/checkout")
    public String checkout(Principal principal, Model model, RedirectAttributes ra) {
        User user = userService.getByEmail(principal.getName());
        if (cartService.getItems(user).isEmpty()) {
            ra.addFlashAttribute("error", "Your cart is empty. Add some products first.");
            return "redirect:/cart";
        }
        CheckoutForm form = new CheckoutForm();
        form.setFullName(user.getName());
        model.addAttribute("form", form);
        addSummary(model, user);
        return "checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@Valid @ModelAttribute("form") CheckoutForm form, BindingResult result,
                             Principal principal, Model model, RedirectAttributes ra) {
        User user = userService.getByEmail(principal.getName());
        if (result.hasErrors()) {
            addSummary(model, user);
            return "checkout";
        }
        try {
            Order order = orderService.placeOrder(user, form);
            ra.addFlashAttribute("justPlaced", true);
            return "redirect:/orders/" + order.getId();
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    private void addSummary(Model model, User user) {
        List<CartItem> items = cartService.getItems(user);
        BigDecimal total = cartService.total(items);
        BigDecimal mrpTotal = cartService.mrpTotal(items);
        model.addAttribute("items", items);
        model.addAttribute("itemCount", items.stream().mapToInt(CartItem::getQuantity).sum());
        model.addAttribute("total", total);
        model.addAttribute("mrpTotal", mrpTotal);
        model.addAttribute("savings", mrpTotal.subtract(total));
    }

    // ---------- My orders ----------
    @GetMapping("/orders")
    public String orders(Principal principal, Model model) {
        User user = userService.getByEmail(principal.getName());
        model.addAttribute("orders", orderService.findByUser(user));
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Principal principal, Model model) {
        User user = userService.getByEmail(principal.getName());
        Optional<Order> order = orderService.findForUser(user, id);
        if (order.isEmpty()) {
            return "redirect:/orders";
        }
        model.addAttribute("order", order.get());
        return "order-detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        User user = userService.getByEmail(principal.getName());
        if (orderService.cancel(user, id)) {
            ra.addFlashAttribute("success", "Your order has been cancelled.");
        } else {
            ra.addFlashAttribute("error", "This order can no longer be cancelled.");
        }
        return "redirect:/orders/" + id;
    }
}
