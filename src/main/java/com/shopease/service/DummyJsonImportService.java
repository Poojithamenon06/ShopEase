package com.shopease.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.shopease.model.Product;
import com.shopease.repository.CartItemRepository;
import com.shopease.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/** Loads the product catalogue from https://dummyjson.com/products into MySQL. */
@Service
public class DummyJsonImportService {

    private static final Logger log = LoggerFactory.getLogger(DummyJsonImportService.class);

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final RestClient client;

    public DummyJsonImportService(ProductRepository productRepository,
                                  CartItemRepository cartItemRepository,
                                  @Value("${shopease.dummyjson.base-url:https://dummyjson.com}") String baseUrl) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(8000);
        factory.setReadTimeout(30000);
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    /**
     * Downloads every DummyJSON product and saves it (insert new, update existing by DummyJSON id).
     *
     * @param replaceDemoCatalogue true on the very first import: removes the old built-in demo products
     *                             (and cart rows that point to them) so only DummyJSON products remain.
     * @return number of products imported/updated
     */
    @Transactional
    public int importAll(boolean replaceDemoCatalogue) {
        // limit=0 asks DummyJSON for all products (194 at the time of writing) in one request
        JsonNode root = client.get().uri("/products?limit=0").retrieve().body(JsonNode.class);
        JsonNode items = root == null ? null : root.get("products");
        if (items == null || !items.isArray() || items.isEmpty()) {
            throw new IllegalStateException("DummyJSON returned no products");
        }

        if (replaceDemoCatalogue) {
            cartItemRepository.deleteAll();
            productRepository.deleteByExternalIdIsNull();
        }

        // skip categories that do not belong in the store (vehicles, motorcycles)
        List<JsonNode> wanted = new ArrayList<>();
        for (JsonNode n : items) {
            String slug = text(n, "category");
            if (slug == null || !ProductEnricher.EXCLUDED_CATEGORIES.contains(slug.toLowerCase())) wanted.add(n);
        }

        List<Long> ids = new ArrayList<>();
        for (JsonNode n : wanted) ids.add(n.get("id").asLong());
        Map<Long, Product> existing = productRepository.findByExternalIdIn(ids).stream()
                .collect(Collectors.toMap(Product::getExternalId, p -> p));

        List<Product> toSave = new ArrayList<>();
        for (JsonNode n : wanted) {
            Product p = existing.getOrDefault(n.get("id").asLong(), new Product());
            map(n, p);
            toSave.add(p);
        }
        productRepository.saveAll(toSave);
        log.info("Imported {} products from DummyJSON", toSave.size());
        return toSave.size();
    }

    private void map(JsonNode n, Product p) {
        long id = n.get("id").asLong();
        String slug = text(n, "category");
        String title = text(n, "title");
        String description = text(n, "description");

        p.setExternalId(id);
        p.setName(title == null ? "Product " + id : title);
        p.setDescription(description == null ? null
                : (description.length() > 1000 ? description.substring(0, 1000) : description));
        p.setCategory(ProductEnricher.prettyCategory(slug));
        p.setBrand(text(n, "brand"));
        p.setStock(Math.max(0, n.path("stock").asInt(0)));
        p.setRating(n.has("rating") ? Math.round(n.get("rating").asDouble() * 10.0) / 10.0 : null);
        p.setImageUrl(text(n, "thumbnail"));

        BigDecimal[] prices = ProductEnricher.inrPrices(
                n.path("price").asDouble(0), n.path("discountPercentage").asDouble(0), slug, id);
        p.setPrice(prices[0]);
        p.setMrp(prices[1]);

        p.setColor(ProductEnricher.colorFrom(title, description));
        p.setSizes(ProductEnricher.sizesFor(slug));
        p.setFabric(ProductEnricher.fabricFrom(title, description, slug, id));
    }

    private static String text(JsonNode n, String field) {
        JsonNode v = n.get(field);
        if (v == null || v.isNull()) return null;
        String s = v.asText().trim();
        return s.isEmpty() ? null : s;
    }
}
