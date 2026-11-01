package com.shopease.controller;

import com.shopease.model.Product;
import com.shopease.repository.UserRepository;
import com.shopease.service.DummyJsonImportService;
import com.shopease.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final UserRepository userRepository;
    private final DummyJsonImportService importService;

    public AdminController(ProductService productService, UserRepository userRepository,
                           DummyJsonImportService importService) {
        this.productService = productService;
        this.userRepository = userRepository;
        this.importService = importService;
    }

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("productCount", productService.count());
        model.addAttribute("categoryCount", productService.categories().size());
        model.addAttribute("userCount", userRepository.count());
        var lowStock = productService.findAll().stream().filter(p -> p.getStock() <= 5).toList();
        model.addAttribute("lowStockCount", lowStock.size());
        model.addAttribute("lowStock", lowStock.stream().limit(8).toList());
        return "admin/dashboard";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.categories());
        return "admin/product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        return productService.findById(id).map(p -> {
            model.addAttribute("product", p);
            model.addAttribute("categories", productService.categories());
            return "admin/product-form";
        }).orElse("redirect:/admin/products");
    }

    @PostMapping("/products/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product, BindingResult result,
                              Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("categories", productService.categories());
            return "admin/product-form";
        }
        boolean isNew = product.getId() == null;
        productService.save(product);
        ra.addFlashAttribute("success", isNew ? "Product added." : "Product updated.");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("success", "Product deleted.");
        return "redirect:/admin/products";
    }

    /** Re-downloads the catalogue from DummyJSON (adds new products, refreshes imported ones). */
    @PostMapping("/import")
    public String importFromDummyJson(RedirectAttributes ra) {
        try {
            int n = importService.importAll(false);
            ra.addFlashAttribute("success", n + " products imported / refreshed from DummyJSON.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Import failed: " + e.getMessage());
        }
        return "redirect:/admin";
    }
}
