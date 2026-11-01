package com.shopease.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Pure helper methods that turn DummyJSON data into ShopEase fields.
 * DummyJSON has no size / colour / fabric fields, so they are worked out here:
 * colour and fabric are read from the title/description when mentioned, otherwise a
 * sensible default for the category is used; sizes depend on the category.
 */
public final class ProductEnricher {

    private ProductEnricher() {}

    /** keyword in text -> colour name used by the sidebar filter (order matters: longest first). */
    private static final Map<String, String> COLORS = new LinkedHashMap<>();
    static {
        COLORS.put("navy blue", "Navy Blue"); COLORS.put("navy", "Navy Blue");
        COLORS.put("black", "Black"); COLORS.put("white", "White");
        COLORS.put("blue", "Blue"); COLORS.put("red", "Red");
        COLORS.put("maroon", "Maroon"); COLORS.put("green", "Green");
        COLORS.put("olive", "Olive"); COLORS.put("grey", "Grey"); COLORS.put("gray", "Grey");
        COLORS.put("silver", "Silver"); COLORS.put("gold", "Gold");
        COLORS.put("beige", "Beige"); COLORS.put("brown", "Brown");
        COLORS.put("pink", "Pink"); COLORS.put("yellow", "Yellow");
        COLORS.put("orange", "Orange"); COLORS.put("purple", "Purple");
    }

    private static final Map<String, String> FABRICS = new LinkedHashMap<>();
    static {
        FABRICS.put("cotton", "Cotton"); FABRICS.put("silk", "Silk");
        FABRICS.put("leather", "Leather"); FABRICS.put("denim", "Denim");
        FABRICS.put("wool", "Wool"); FABRICS.put("linen", "Linen");
        FABRICS.put("polyester", "Polyester"); FABRICS.put("nylon", "Nylon");
        FABRICS.put("satin", "Satin"); FABRICS.put("velvet", "Velvet");
        FABRICS.put("chiffon", "Chiffon"); FABRICS.put("rayon", "Rayon");
        FABRICS.put("suede", "Suede"); FABRICS.put("canvas", "Canvas");
        FABRICS.put("stainless steel", "Stainless Steel"); FABRICS.put("ceramic", "Ceramic");
        FABRICS.put("wood", "Wood"); FABRICS.put("glass", "Glass");
        FABRICS.put("rubber", "Rubber"); FABRICS.put("aluminum", "Aluminium");
        FABRICS.put("aluminium", "Aluminium");
    }

    private static final Map<String, List<String>> DEFAULT_FABRICS = Map.of(
            "mens-shirts", List.of("Cotton", "Cotton Blend", "Linen", "Polyester"),
            "tops", List.of("Cotton", "Rayon", "Polyester", "Georgette"),
            "womens-dresses", List.of("Georgette", "Chiffon", "Cotton", "Satin"),
            "mens-shoes", List.of("Leather", "Synthetic", "Mesh", "Canvas"),
            "womens-shoes", List.of("Leather", "Synthetic", "Mesh", "Canvas"),
            "womens-bags", List.of("Leather", "Canvas", "Synthetic"));

    private static final Map<String, String> SIZES = Map.of(
            "mens-shirts", "S,M,L,XL,XXL",
            "tops", "XS,S,M,L,XL",
            "womens-dresses", "XS,S,M,L,XL",
            "mens-shoes", "7,8,9,10,11",
            "womens-shoes", "5,6,7,8,9");

    /** "mens-shirts" -> "Men's Shirts", "home-decoration" -> "Home Decoration". */
    public static String prettyCategory(String slug) {
        if (slug == null || slug.isBlank()) return "Other";
        StringBuilder sb = new StringBuilder();
        for (String part : slug.trim().split("-")) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            switch (part.toLowerCase()) {
                case "mens": sb.append("Men's"); break;
                case "womens": sb.append("Women's"); break;
                default: sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    public static String sizesFor(String categorySlug) {
        return SIZES.get(categorySlug);
    }

    /** First colour word found in the title, otherwise in the description; null if none. */
    public static String colorFrom(String title, String description) {
        String c = firstMatch(title, COLORS);
        return c != null ? c : firstMatch(description, COLORS);
    }

    /** Material mentioned in the text, otherwise a category default (rotated by id); null if none. */
    public static String fabricFrom(String title, String description, String categorySlug, long id) {
        String f = firstMatch(title, FABRICS);
        if (f == null) f = firstMatch(description, FABRICS);
        if (f != null) return f;
        List<String> defaults = DEFAULT_FABRICS.get(categorySlug);
        return defaults == null ? null : defaults.get((int) (Math.abs(id) % defaults.size()));
    }

    /** Categories that do not belong in a shopping store and are skipped during import. */
    public static final java.util.Set<String> EXCLUDED_CATEGORIES = java.util.Set.of("vehicle", "motorcycle");

    /** category -> {INR per USD, max price in INR}. DummyJSON's USD prices are scaled to Indian store levels. */
    private static final Map<String, double[]> PRICE_RULES = new java.util.HashMap<>();
    static {
        PRICE_RULES.put("groceries",            new double[] {60,    800});
        PRICE_RULES.put("beauty",               new double[] {45,   1500});
        PRICE_RULES.put("skin-care",            new double[] {55,   1800});
        PRICE_RULES.put("fragrances",           new double[] {50,   5000});
        PRICE_RULES.put("home-decoration",      new double[] {45,   3000});
        PRICE_RULES.put("kitchen-accessories",  new double[] {40,   2500});
        PRICE_RULES.put("furniture",            new double[] {40,  25000});
        PRICE_RULES.put("laptops",              new double[] {55, 120000});
        PRICE_RULES.put("smartphones",          new double[] {55,  90000});
        PRICE_RULES.put("tablets",              new double[] {50,  80000});
        PRICE_RULES.put("mobile-accessories",   new double[] {45,  25000});
        PRICE_RULES.put("mens-shirts",          new double[] {35,   3500});
        PRICE_RULES.put("tops",                 new double[] {35,   3000});
        PRICE_RULES.put("womens-dresses",       new double[] {35,   4500});
        PRICE_RULES.put("mens-shoes",           new double[] {40,   6000});
        PRICE_RULES.put("womens-shoes",         new double[] {40,   6000});
        PRICE_RULES.put("mens-watches",         new double[] {35,  15000});
        PRICE_RULES.put("womens-watches",       new double[] {35,  15000});
        PRICE_RULES.put("sunglasses",           new double[] {35,   3000});
        PRICE_RULES.put("womens-bags",          new double[] {35,   6000});
        PRICE_RULES.put("womens-jewellery",     new double[] {35,   8000});
        PRICE_RULES.put("sports-accessories",   new double[] {40,   5000});
    }
    private static final double[] DEFAULT_PRICE_RULE = {45, 20000};

    /**
     * Returns {sellingPrice, mrp} in INR with store-style endings (e.g. 499, 1,499, 24,999).
     * DummyJSON "price" is the price before discount, so it becomes the MRP and the discount is applied to it.
     * Expensive items are capped per category so nothing looks unrealistic.
     */
    public static BigDecimal[] inrPrices(double usdPrice, double discountPercent, String categorySlug, long id) {
        double[] rule = PRICE_RULES.getOrDefault(categorySlug, DEFAULT_PRICE_RULE);
        double mrpRaw = usdPrice * rule[0];
        if (mrpRaw > rule[1]) {
            // compress very expensive items into 55%-95% of the category cap (varies by id)
            mrpRaw = rule[1] * (0.55 + 0.10 * (Math.abs(id) % 5));
        }
        double d = Math.max(0, Math.min(discountPercent, 60));
        BigDecimal mrp = storePrice(mrpRaw);
        BigDecimal selling = storePrice(mrpRaw * (100 - d) / 100.0);
        if (selling.compareTo(mrp) > 0) selling = mrp;
        return new BigDecimal[] { selling, mrp };
    }

    /** Rounds to a typical shop price: 79 / 499 / 1,499 / 24,999. */
    private static BigDecimal storePrice(double v) {
        double step = v < 100 ? 10 : v < 1000 ? 10 : v < 10000 ? 100 : 1000;
        double rounded = Math.round(v / step) * step - 1;
        if (rounded < 9) rounded = 9;
        return BigDecimal.valueOf(Math.round(rounded)).setScale(0, RoundingMode.HALF_UP);
    }

    /** The keyword that appears earliest in the text ("Blue & Black Shirt" -> Blue). */
    private static String firstMatch(String text, Map<String, String> words) {
        if (text == null || text.isBlank()) return null;
        String t = text.toLowerCase();
        String best = null;
        int bestPos = Integer.MAX_VALUE;
        for (Map.Entry<String, String> e : words.entrySet()) {
            java.util.regex.Matcher m = Pattern.compile("\\b" + Pattern.quote(e.getKey()) + "\\b").matcher(t);
            if (m.find() && m.start() < bestPos) {
                bestPos = m.start();
                best = e.getValue();
            }
        }
        return best;
    }
}
