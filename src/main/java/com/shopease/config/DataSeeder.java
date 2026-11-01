package com.shopease.config;

import com.shopease.model.Product;
import com.shopease.model.User;
import com.shopease.repository.CartItemRepository;
import com.shopease.repository.ProductRepository;
import com.shopease.repository.UserRepository;
import com.shopease.service.DummyJsonImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Creates demo accounts and a sample catalogue on first run. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final PasswordEncoder encoder;
    private final DummyJsonImportService dummyJsonImportService;
    private final boolean dummyJsonEnabled;
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private int imageCounter = 0;

    public DataSeeder(UserRepository userRepository, ProductRepository productRepository,
                      CartItemRepository cartItemRepository, PasswordEncoder encoder,
                      DummyJsonImportService dummyJsonImportService,
                      @Value("${shopease.dummyjson.enabled:true}") boolean dummyJsonEnabled) {
        this.dummyJsonImportService = dummyJsonImportService;
        this.dummyJsonEnabled = dummyJsonEnabled;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.encoder = encoder;
    }

    private static String unsplash(String id) {
        return "https://images.unsplash.com/" + id + "?auto=format&fit=crop&w=800&q=80";
    }

    /** Keyword based photo; if it fails to load the page shows an emoji instead. */
    private String photo(String keyword) {
        return "https://loremflickr.com/640/480/" + keyword + "?lock=" + (++imageCounter);
    }

    private Product p(String name, String category, String brand, String color, String sizes, String fabric,
                      String price, String mrp, int stock, double rating, String image, String description) {
        Product x = new Product();
        x.setName(name); x.setCategory(category); x.setBrand(brand); x.setColor(color);
        x.setSizes(sizes); x.setFabric(fabric); x.setPrice(new BigDecimal(price));
        x.setMrp(mrp == null ? null : new BigDecimal(mrp)); x.setStock(stock); x.setRating(rating);
        x.setImageUrl(image); x.setDescription(description);
        return x;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(new User("Store Admin", "admin@shopease.com", encoder.encode("admin123"), "ROLE_ADMIN"));
            userRepository.save(new User("Demo Customer", "user@shopease.com", encoder.encode("user123"), "ROLE_USER"));
        }
        // 1) Load the real catalogue from DummyJSON (once)
        boolean imported = productRepository.countByExternalIdIsNotNull() > 0;
        if (!imported && dummyJsonEnabled) {
            try {
                dummyJsonImportService.importAll(true);
                imported = true;
            } catch (Exception e) {
                log.warn("Could not load products from DummyJSON ({}). Using the built-in demo catalogue instead. "
                        + "Restart the app with internet access, or use Admin > Import from DummyJSON.", e.getMessage());
            }
        }
        if (imported) return;

        // 2) Fallback: built-in demo catalogue when DummyJSON is unreachable
        boolean needsCatalogue = productRepository.count() == 0
                || productRepository.findAll().stream().noneMatch(pr -> pr.getBrand() != null);
        if (needsCatalogue) {
            cartItemRepository.deleteAll();
            productRepository.deleteAll();
            productRepository.saveAll(catalogue());
        }
    }

    private List<Product> catalogue() {
        List<Product> l = new ArrayList<>();
        String tee = "S,M,L,XL,XXL";

        // ---------------- MEN ----------------
        l.add(p("Crew Neck Cotton T-Shirt (Black)", "Men", "UrbanEdge", "Black", tee, "Cotton", "499", "999", 60, 4.3, photo("tshirt"), "Soft 100% cotton crew neck t-shirt with a regular fit. Breathable and perfect for everyday wear."));
        l.add(p("Crew Neck Cotton T-Shirt (White)", "Men", "UrbanEdge", "White", tee, "Cotton", "499", "999", 55, 4.2, photo("tshirt"), "Classic white t-shirt in pre-shrunk cotton. Easy to pair with jeans, chinos or shorts."));
        l.add(p("Polo T-Shirt (Navy Blue)", "Men", "UrbanEdge", "Navy Blue", tee, "Cotton Blend", "699", "1499", 40, 4.4, photo("polo,shirt"), "Smart polo t-shirt with a ribbed collar and a comfortable cotton-blend fabric."));
        l.add(p("Slim Fit Formal Shirt (Blue)", "Men", "Weave & Co", "Blue", "S,M,L,XL", "Cotton", "899", "1999", 35, 4.1, photo("shirt,formal"), "Wrinkle-resistant slim fit formal shirt, ideal for office and interviews."));
        l.add(p("Checked Casual Shirt (Maroon)", "Men", "Weave & Co", "Maroon", "S,M,L,XL,XXL", "Cotton", "799", "1799", 30, 4.0, photo("flannel,shirt"), "Relaxed fit checked shirt with a soft brushed finish."));
        l.add(p("Linen Summer Shirt (Olive)", "Men", "Weave & Co", "Olive", "M,L,XL", "Linen", "1099", "2199", 20, 4.5, photo("linen,shirt"), "Lightweight pure linen shirt that keeps you cool in hot weather."));
        l.add(p("Slim Fit Stretch Jeans (Blue)", "Men", "Denimora", "Blue", "28,30,32,34,36", "Denim", "1199", "2499", 45, 4.3, photo("jeans"), "Mid-rise slim fit jeans with a hint of stretch for all-day comfort."));
        l.add(p("Cotton Chino Trousers (Beige)", "Men", "Denimora", "Beige", "30,32,34,36", "Cotton", "999", "1999", 32, 4.2, photo("chinos,trousers"), "Tailored chinos in durable cotton twill. Smart-casual and versatile."));
        l.add(p("Hooded Sweatshirt (Grey)", "Men", "NorthPeak", "Grey", "S,M,L,XL", "Fleece", "1299", "2599", 28, 4.6, photo("hoodie"), "Warm fleece-lined hoodie with a kangaroo pocket and adjustable drawstring."));

        // ---------------- WOMEN ----------------
        l.add(p("Floral Maxi Dress (Pink)", "Women", "Aria", "Pink", "XS,S,M,L,XL", "Georgette", "1299", "2999", 25, 4.5, photo("dress"), "Flowy georgette maxi dress with a floral print, perfect for parties and outings."));
        l.add(p("Printed Cotton Kurti (Yellow)", "Women", "Aria", "Yellow", tee, "Cotton", "599", "1299", 50, 4.3, photo("kurti,indian"), "Straight-cut cotton kurti with a beautiful print. Comfortable for daily wear."));
        l.add(p("Silk Blend Saree (Maroon)", "Women", "Aria", "Maroon", "Free Size", "Silk", "2499", "4999", 15, 4.7, photo("saree"), "Elegant silk blend saree with a woven border and matching blouse piece."));
        l.add(p("High-Rise Skinny Jeans (Black)", "Women", "Denimora", "Black", "26,28,30,32", "Denim", "1299", "2599", 38, 4.2, photo("jeans,women"), "High-rise skinny fit jeans with excellent stretch and shape retention."));
        l.add(p("Casual Rayon Top (White)", "Women", "Aria", "White", "XS,S,M,L,XL", "Rayon", "449", "999", 42, 4.1, photo("blouse,top"), "Lightweight rayon top with a relaxed fit for casual days."));
        l.add(p("Woolen Cardigan (Beige)", "Women", "NorthPeak", "Beige", "S,M,L", "Wool", "1499", "2999", 18, 4.6, photo("cardigan,sweater"), "Cozy knitted cardigan that layers over everything in winter."));
        l.add(p("Cotton Palazzo Pants (Green)", "Women", "Aria", "Green", "S,M,L,XL", "Cotton", "699", "1499", 30, 4.2, photo("palazzo,pants"), "Wide-leg palazzo pants with an elasticated waist for maximum comfort."));

        // ---------------- FOOTWEAR ----------------
        l.add(p("Running Shoes (Blue)", "Footwear", "FitFlex", "Blue", "7,8,9,10,11", "Mesh", "3499", "4999", 22, 4.5, unsplash("photo-1491553895911-0055eca6402d"), "Lightweight breathable running shoes with cushioned sole and strong grip."));
        l.add(p("Street Sneakers (Red)", "Footwear", "FitFlex", "Red", "6,7,8,9,10", "Canvas", "2799", "3999", 30, 4.4, unsplash("photo-1542291026-7eec264c27ff"), "Trendy everyday sneakers with a cushioned insole and durable rubber outsole."));
        l.add(p("Leather Formal Shoes (Black)", "Footwear", "Stridely", "Black", "7,8,9,10", "Leather", "2499", "4499", 20, 4.3, photo("shoes,leather"), "Classic genuine leather lace-up formal shoes with a polished finish."));
        l.add(p("Women's Flat Sandals (Beige)", "Footwear", "Stridely", "Beige", "5,6,7,8", "Synthetic", "899", "1799", 40, 4.0, photo("sandals"), "Comfortable flat sandals with a soft footbed. Great for daily use."));
        l.add(p("Casual Suede Loafers (Brown)", "Footwear", "Stridely", "Brown", "7,8,9,10", "Suede", "1799", "3299", 16, 4.4, photo("loafers"), "Slip-on suede loafers that look sharp with both jeans and chinos."));

        // ---------------- ELECTRONICS ----------------
        l.add(p("Wireless Bluetooth Headphones", "Electronics", "SoundWave", "Black", null, null, "2999", "5999", 25, 4.4, unsplash("photo-1505740420928-5e560c06d30e"), "Over-ear Bluetooth headphones with deep bass, noise isolation and 30-hour battery life."));
        l.add(p("Smart Watch Series 5", "Electronics", "TechNova", "Black", null, null, "4499", "8999", 18, 4.2, unsplash("photo-1523275335684-37898b6baf30"), "Track steps, heart rate and sleep. AMOLED display, water resistant, 7-day battery."));
        l.add(p("Ultrabook Pro 14\"", "Electronics", "TechNova", "Silver", null, null, "54999", "69999", 8, 4.6, unsplash("photo-1496181133206-80ce9b88a853"), "Lightweight 14-inch laptop with fast SSD storage, all-day battery and a crisp display."));
        l.add(p("Smartphone X (128 GB)", "Electronics", "TechNova", "Blue", null, null, "24999", "29999", 15, 4.3, unsplash("photo-1511707171634-5f897ff02aa9"), "6.5-inch display, dual camera system, 128 GB storage and fast charging."));
        l.add(p("Mirrorless Camera 24MP", "Electronics", "LensCraft", "Black", null, null, "32999", "39999", 6, 4.5, unsplash("photo-1526170375885-4d8ecf77b99f"), "24 MP mirrorless camera with 4K video, perfect for photography enthusiasts."));
        l.add(p("Portable Bluetooth Speaker", "Electronics", "SoundWave", "Black", null, null, "1999", "3999", 35, 4.3, photo("speaker"), "Waterproof portable speaker with powerful sound and 12 hours of playtime."));
        l.add(p("Power Bank 20000mAh", "Electronics", "TechNova", "White", null, null, "1299", "2499", 50, 4.1, photo("powerbank"), "Fast-charging 20000mAh power bank with dual USB output."));
        l.add(p("Wireless Optical Mouse", "Electronics", "TechNova", "Grey", null, null, "599", "1299", 60, 4.0, photo("computer,mouse"), "Silent-click wireless mouse with adjustable DPI and long battery life."));

        // ---------------- ACCESSORIES ----------------
        l.add(p("Classic UV400 Sunglasses", "Accessories", "Shade&Co", "Black", null, null, "1299", "2999", 40, 4.2, unsplash("photo-1572635196237-14b3f281503f"), "UV400 protection with a timeless frame that suits every face shape."));
        l.add(p("Water-Resistant Travel Backpack 30L", "Accessories", "NorthPeak", "Black", null, "Polyester", "1899", "3499", 35, 4.4, unsplash("photo-1553062407-98eeb64c6a62"), "Water-resistant backpack with a padded laptop sleeve and multiple compartments."));
        l.add(p("Genuine Leather Wallet", "Accessories", "Stridely", "Brown", null, "Leather", "699", "1499", 45, 4.1, photo("wallet"), "Slim bifold wallet with card slots and a coin pocket."));
        l.add(p("Cotton Baseball Cap (Navy)", "Accessories", "UrbanEdge", "Navy Blue", "Free Size", "Cotton", "399", "799", 70, 4.0, photo("cap,hat"), "Adjustable cotton cap with a curved brim and breathable lining."));

        // ---------------- HOME & KITCHEN ----------------
        l.add(p("Cotton Bedsheet Set with Pillow Covers", "Home & Kitchen", "Casa Nova", "Blue", "Single,Double,King", "Cotton", "899", "1999", 30, 4.3, photo("bedsheet,bedroom"), "Soft 144 TC cotton bedsheet with two matching pillow covers."));
        l.add(p("Ceramic Coffee Mug Set of 4", "Home & Kitchen", "Casa Nova", "White", null, "Ceramic", "499", "999", 55, 4.2, photo("mug,coffee"), "Microwave-safe ceramic mugs, a stylish set for tea and coffee lovers."));
        l.add(p("Indoor Plant with Ceramic Pot", "Home & Kitchen", "GreenNest", "Green", null, null, "499", "799", 50, 4.5, unsplash("photo-1485955900006-10f4d324d411"), "Fresh green indoor plant in a ceramic pot. Brightens up any room or desk."));
        l.add(p("Non-Stick Cookware Set (3 pcs)", "Home & Kitchen", "Casa Nova", "Black", null, "Aluminium", "1999", "3999", 20, 4.4, photo("cookware,pan"), "Induction-friendly non-stick set with a frying pan, kadhai and tawa."));
        l.add(p("Cotton Cushion Covers (Set of 5)", "Home & Kitchen", "Casa Nova", "Yellow", null, "Cotton", "399", "899", 48, 4.1, photo("cushion,sofa"), "Vibrant cushion covers that instantly refresh your living room."));
        l.add(p("LED Table Lamp", "Home & Kitchen", "GreenNest", "White", null, null, "899", "1599", 26, 4.2, photo("lamp,desk"), "Touch-control LED lamp with three brightness levels and eye-care light."));

        // ---------------- SPORTS ----------------
        l.add(p("Anti-Slip Yoga Mat 6mm", "Sports", "FitFlex", "Purple", null, "EVA Foam", "599", "1299", 40, 4.3, photo("yoga,mat"), "Extra-thick anti-slip yoga mat with a carry strap."));
        l.add(p("Hex Dumbbell Set 10kg", "Sports", "FitFlex", "Black", null, "Rubber", "1499", "2499", 22, 4.4, photo("dumbbell"), "Rubber-coated hex dumbbells for a complete home workout."));
        l.add(p("Training Football (Size 5)", "Sports", "Kickoff", "Multicolor", "5", "Synthetic", "699", "1299", 38, 4.2, photo("football,soccer"), "Durable machine-stitched football suitable for all surfaces."));
        l.add(p("English Willow Cricket Bat", "Sports", "Kickoff", "Brown", null, "Willow", "1999", "3499", 12, 4.5, photo("cricket,bat"), "Well-balanced willow bat with a comfortable grip for club-level play."));
        l.add(p("Badminton Racket (Pair)", "Sports", "Kickoff", "Red", null, "Graphite", "899", "1699", 30, 4.1, photo("badminton"), "Lightweight graphite rackets with a full cover, great for beginners."));

        // ---------------- BOOKS ----------------
        l.add(p("Bestseller Fiction Collection (3 books)", "Books", "PageTurner", null, null, null, "899", "1499", 45, 4.6, unsplash("photo-1544947950-fa07a98d237f"), "A curated set of three bestselling novels for your reading list."));
        l.add(p("Self-Help Classics Box Set", "Books", "PageTurner", null, null, null, "749", "1299", 33, 4.5, photo("books,stack"), "Four timeless self-help books on habits, focus and personal growth."));
        l.add(p("Children's Story Books (Set of 5)", "Books", "PageTurner", null, null, null, "399", "699", 60, 4.4, photo("children,books"), "Illustrated short stories that kids love, with large friendly print."));
        return l;
    }
}
