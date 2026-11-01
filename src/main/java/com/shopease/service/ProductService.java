package com.shopease.service;

import com.shopease.model.CategoryInfo;
import com.shopease.model.Product;
import com.shopease.repository.CartItemRepository;
import com.shopease.repository.ProductRepository;
import com.shopease.repository.ProductSpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ProductService {

    private static final List<String> SIZE_ORDER = List.of("XS", "S", "M", "L", "XL", "XXL", "XXXL");
    private static final Map<String, String> COLOR_CSS = new HashMap<>();
    static {
        COLOR_CSS.put("black", "#111827"); COLOR_CSS.put("white", "#ffffff");
        COLOR_CSS.put("blue", "#2563eb"); COLOR_CSS.put("navy blue", "#1e3a8a");
        COLOR_CSS.put("red", "#dc2626"); COLOR_CSS.put("maroon", "#7f1d1d");
        COLOR_CSS.put("green", "#16a34a"); COLOR_CSS.put("olive", "#6b7d3a");
        COLOR_CSS.put("grey", "#6b7280"); COLOR_CSS.put("silver", "#cbd5e1");
        COLOR_CSS.put("beige", "#d6c3a1"); COLOR_CSS.put("brown", "#92400e");
        COLOR_CSS.put("pink", "#ec4899"); COLOR_CSS.put("yellow", "#eab308");
        COLOR_CSS.put("orange", "#f97316"); COLOR_CSS.put("purple", "#7c3aed");
        COLOR_CSS.put("multicolor", "conic-gradient(#ef4444,#eab308,#22c55e,#3b82f6,#a855f7,#ef4444)");
    }

    /** Values shown in the sidebar filter. */
    public static class Facets {
        private final List<String> colors, sizes, fabrics, brands;
        public Facets(List<String> colors, List<String> sizes, List<String> fabrics, List<String> brands) {
            this.colors = colors; this.sizes = sizes; this.fabrics = fabrics; this.brands = brands;
        }
        public List<String> getColors() { return colors; }
        public List<String> getSizes() { return sizes; }
        public List<String> getFabrics() { return fabrics; }
        public List<String> getBrands() { return brands; }
        public String hexOf(String color) {
            return COLOR_CSS.getOrDefault(color == null ? "" : color.toLowerCase(), "#cbd5e1");
        }
    }

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public ProductService(ProductRepository productRepository, CartItemRepository cartItemRepository) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    // ---------- Search & filters ----------
    public List<Product> search(String q, String category, List<String> colors, List<String> sizes,
                                List<String> fabrics, List<String> brands, BigDecimal min, BigDecimal max,
                                boolean inStockOnly, String sort) {
        return productRepository.findAll(
                ProductSpecifications.build(q, category, colors, sizes, fabrics, brands, min, max, inStockOnly),
                sortOf(sort));
    }

    public Facets facets(String q, String category) {
        List<Product> pool = productRepository.findAll(
                ProductSpecifications.build(q, category, null, null, null, null, null, null, false));
        return new Facets(
                pool.stream().map(Product::getColor).filter(ProductService::hasText).distinct().sorted().toList(),
                pool.stream().flatMap(p -> p.getSizeList().stream()).distinct().sorted(ProductService::compareSizes).toList(),
                pool.stream().map(Product::getFabric).filter(ProductService::hasText).distinct().sorted().toList(),
                pool.stream().map(Product::getBrand).filter(ProductService::hasText).distinct().sorted().toList());
    }

    public List<Product> related(Product product) {
        return search("", product.getCategory(), null, null, null, null, null, null, true, "rating").stream()
                .filter(p -> !p.getId().equals(product.getId()))
                .limit(4).toList();
    }

    private static Sort sortOf(String sort) {
        if (sort == null) sort = "";
        switch (sort) {
            case "priceAsc": return Sort.by(Sort.Order.asc("price"));
            case "priceDesc": return Sort.by(Sort.Order.desc("price"));
            case "rating": return Sort.by(Sort.Order.desc("rating"), Sort.Order.desc("id"));
            case "nameAsc": return Sort.by(Sort.Order.asc("name"));
            default: return Sort.by(Sort.Order.desc("id"));
        }
    }

    private static boolean hasText(String s) { return s != null && !s.isBlank(); }

    private static int sizeRank(String s) {
        int i = SIZE_ORDER.indexOf(s);
        return i >= 0 ? i : 100;
    }

    private static Double asNumber(String s) {
        try { return Double.valueOf(s); } catch (NumberFormatException e) { return null; }
    }

    private static int compareSizes(String a, String b) {
        int ra = sizeRank(a), rb = sizeRank(b);
        if (ra != rb) return Integer.compare(ra, rb);
        Double na = asNumber(a), nb = asNumber(b);
        if (na != null && nb != null) return Double.compare(na, nb);
        return a.compareTo(b);
    }

    // ---------- Home page ----------
    public List<Product> latest() { return productRepository.findTop8ByOrderByIdDesc(); }
    public List<Product> topRated() { return productRepository.findTop8ByRatingNotNullOrderByRatingDesc(); }

    public List<CategoryInfo> categoryInfos() {
        List<CategoryInfo> list = new ArrayList<>();
        for (String c : productRepository.findCategories()) {
            Product dummy = new Product();
            dummy.setCategory(c);
            list.add(new CategoryInfo(c, dummy.getEmoji()));
        }
        return list;
    }

    // ---------- Basic CRUD ----------
    public List<Product> findAll() { return productRepository.findAll(Sort.by("id").descending()); }
    public List<String> categories() { return productRepository.findCategories(); }
    public Optional<Product> findById(Long id) { return productRepository.findById(id); }
    public long count() { return productRepository.count(); }

    public Product save(Product p) {
        // the admin form does not carry externalId, so keep the existing one when editing
        if (p.getId() != null) {
            productRepository.findById(p.getId()).ifPresent(old -> p.setExternalId(old.getExternalId()));
        }
        p.setBrand(clean(p.getBrand()));
        p.setColor(clean(p.getColor()));
        p.setFabric(clean(p.getFabric()));
        p.setImageUrl(clean(p.getImageUrl()));
        String sizes = clean(p.getSizes());
        p.setSizes(sizes == null ? null : sizes.replaceAll("\\s*,\\s*", ","));
        p.setCategory(p.getCategory().trim());
        if (p.getRating() != null) {
            p.setRating(Math.min(5.0, p.getRating()));
        }
        return productRepository.save(p);
    }

    private static String clean(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }

    @Transactional
    public void delete(Long id) {
        productRepository.findById(id).ifPresent(p -> {
            cartItemRepository.deleteByProduct(p);
            productRepository.delete(p);
        });
    }
}
