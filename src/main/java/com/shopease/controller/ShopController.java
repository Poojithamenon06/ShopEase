package com.shopease.controller;

import com.shopease.model.Product;
import com.shopease.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
public class ShopController {

    private final ProductService productService;

    public ShopController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categoryTiles", productService.categoryInfos());
        model.addAttribute("trending", productService.topRated());
        model.addAttribute("featured", productService.latest());
        return "index";
    }

    @GetMapping("/products")
    public String products(@RequestParam(defaultValue = "") String q,
                           @RequestParam(defaultValue = "") String category,
                           @RequestParam(required = false) List<String> color,
                           @RequestParam(required = false) List<String> size,
                           @RequestParam(required = false) List<String> fabric,
                           @RequestParam(required = false) List<String> brand,
                           @RequestParam(required = false) BigDecimal minPrice,
                           @RequestParam(required = false) BigDecimal maxPrice,
                           @RequestParam(defaultValue = "false") boolean inStock,
                           @RequestParam(defaultValue = "newest") String sort,
                           Model model) {
        List<String> colors = color == null ? List.of() : color;
        List<String> sizes = size == null ? List.of() : size;
        List<String> fabrics = fabric == null ? List.of() : fabric;
        List<String> brands = brand == null ? List.of() : brand;

        model.addAttribute("products",
                productService.search(q, category, colors, sizes, fabrics, brands, minPrice, maxPrice, inStock, sort));
        model.addAttribute("categories", productService.categories());
        model.addAttribute("facets", productService.facets(q, category));

        model.addAttribute("q", q);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedColors", colors);
        model.addAttribute("selectedSizes", sizes);
        model.addAttribute("selectedFabrics", fabrics);
        model.addAttribute("selectedBrands", brands);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("inStock", inStock);
        model.addAttribute("sort", sort);
        return "products";
    }

    @GetMapping("/product/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Optional<Product> product = productService.findById(id);
        if (product.isEmpty()) {
            return "redirect:/products";
        }
        model.addAttribute("product", product.get());
        model.addAttribute("related", productService.related(product.get()));
        return "product-detail";
    }
}
