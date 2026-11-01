package com.shopease.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price cannot be negative")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** Original (struck-through) price, optional. */
    @Column(precision = 12, scale = 2)
    private BigDecimal mrp;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private Integer stock;

    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category;

    /** Id of the product in DummyJSON (null for products created by hand in the admin panel). */
    @Column(unique = true)
    private Long externalId;

    private String brand;
    private String color;
    private String fabric;

    /** Comma separated, e.g. "S,M,L,XL" or "7,8,9,10". */
    private String sizes;

    @PositiveOrZero(message = "Rating cannot be negative")
    private Double rating;

    @Column(length = 1000)
    private String imageUrl;

    public Product() {}

    /** Emoji used as a fallback when a product image is missing (also shown on category tiles). */
    public String getEmoji() {
        String c = category == null ? "" : category.toLowerCase();
        if (c.contains("shoe") || c.contains("foot")) return "👟";
        if (c.contains("watch")) return "⌚";
        if (c.contains("bag")) return "👜";
        if (c.contains("jewel")) return "💍";
        if (c.contains("sunglass")) return "🕶️";
        if (c.contains("laptop") || c.contains("tablet")) return "💻";
        if (c.contains("smartphone") || c.contains("mobile")) return "📱";
        if (c.contains("fragrance")) return "🌸";
        if (c.contains("beauty") || c.contains("skin")) return "💄";
        if (c.contains("furniture")) return "🛋️";
        if (c.contains("kitchen")) return "🍳";
        if (c.contains("grocer")) return "🛒";
        if (c.contains("motorcycle")) return "🏍️";
        if (c.contains("vehicle")) return "🚗";
        if (c.contains("sport")) return "⚽";
        if (c.contains("book")) return "📚";
        if (c.contains("home")) return "🏠";
        if (c.contains("electronic")) return "🎧";
        if (c.equals("tops")) return "👚";
        if (c.contains("dress") || c.contains("women")) return "👗";
        if (c.contains("men") || c.contains("shirt")) return "👔";
        if (c.contains("access")) return "🕶️";
        return "🛍️";
    }

    public List<String> getSizeList() {
        if (sizes == null || sizes.isBlank()) return List.of();
        return Arrays.stream(sizes.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    public int getDiscountPercent() {
        if (mrp == null || price == null || mrp.signum() <= 0 || mrp.compareTo(price) <= 0) return 0;
        return mrp.subtract(price).multiply(BigDecimal.valueOf(100))
                  .divide(mrp, 0, RoundingMode.HALF_UP).intValue();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getMrp() { return mrp; }
    public void setMrp(BigDecimal mrp) { this.mrp = mrp; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Long getExternalId() { return externalId; }
    public void setExternalId(Long externalId) { this.externalId = externalId; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getFabric() { return fabric; }
    public void setFabric(String fabric) { this.fabric = fabric; }
    public String getSizes() { return sizes; }
    public void setSizes(String sizes) { this.sizes = sizes; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
