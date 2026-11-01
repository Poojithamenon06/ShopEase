package com.shopease.controller;

import com.shopease.model.RegisterForm;
import com.shopease.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult result, RedirectAttributes ra) {
        if (form.getPassword() != null && form.getConfirmPassword() != null
                && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (form.getEmail() != null && !result.hasFieldErrors("email") && userService.emailExists(form.getEmail())) {
            result.rejectValue("email", "exists", "An account with this email already exists");
        }
        if (result.hasErrors()) {
            return "register";
        }
        userService.register(form.getName(), form.getEmail(), form.getPassword());
        ra.addFlashAttribute("success", "Account created successfully! Please sign in.");
        return "redirect:/login";
    }
}
