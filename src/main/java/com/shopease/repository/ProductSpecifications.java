package com.shopease.repository;

import com.shopease.model.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Builds the dynamic database query used by search + sidebar filters. */
public final class ProductSpecifications {

    private ProductSpecifications() {}

    public static Specification<Product> build(String q, String category,
                                               List<String> colors, List<String> sizes,
                                               List<String> fabrics, List<String> brands,
                                               BigDecimal minPrice, BigDecimal maxPrice,
                                               boolean inStockOnly) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.<String>get("name")), like),
                        cb.like(cb.lower(root.<String>get("category")), like),
                        cb.like(cb.lower(root.<String>get("brand")), like),
                        cb.like(cb.lower(root.<String>get("color")), like),
                        cb.like(cb.lower(root.<String>get("fabric")), like),
                        cb.like(cb.lower(root.<String>get("description")), like)));
            }
            if (category != null && !category.isBlank()) {
                ps.add(cb.equal(cb.lower(root.<String>get("category")), category.trim().toLowerCase()));
            }
            if (colors != null && !colors.isEmpty()) {
                ps.add(root.<String>get("color").in(colors));
            }
            if (fabrics != null && !fabrics.isEmpty()) {
                ps.add(root.<String>get("fabric").in(fabrics));
            }
            if (brands != null && !brands.isEmpty()) {
                ps.add(root.<String>get("brand").in(brands));
            }
            if (sizes != null && !sizes.isEmpty()) {
                List<Predicate> sp = new ArrayList<>();
                for (String s : sizes) {
                    sp.add(cb.like(cb.concat(cb.concat(",", root.<String>get("sizes")), ","),
                                   "%," + s.trim() + ",%"));
                }
                ps.add(cb.or(sp.toArray(new Predicate[0])));
            }
            if (minPrice != null) {
                ps.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("price"), minPrice));
            }
            if (maxPrice != null) {
                ps.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("price"), maxPrice));
            }
            if (inStockOnly) {
                ps.add(cb.greaterThan(root.<Integer>get("stock"), 0));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
