# 🛍️ ShopEase – Full-Stack E-Commerce Application

ShopEase is a Flipkart / Amazon style online store built with **Spring Boot 3**, **Spring Security**, **Thymeleaf**, **Spring Data JPA** and **MySQL**. Products are loaded from the free [DummyJSON](https://dummyjson.com/products) API, and customers can browse, filter, add to cart and place **Cash on Delivery** orders.

---

## ✨ Features

**Shopping**
- Browse 190+ products across 20+ categories (fashion, electronics, beauty, home, groceries and more)
- Search box with category dropdown, category strip and category tiles
- Filter sidebar: category, price range, **size, color, fabric/material, brand**, availability
- Sorting: newest, top rated, price low to high, price high to low, name
- Product page with size picker, MRP / discount, rating and related products

**Cart & Orders**
- Cart: add items, change quantity, remove items, clear cart, savings summary
- **Checkout with Cash on Delivery**: delivery address form with validation and order summary
- Order confirmation page with order number and expected delivery date
- **My Orders**: order history, order details, cancel an order (stock is restored)
- Stock is checked and reduced automatically when an order is placed

**Accounts & Admin**
- Sign up / Sign in with BCrypt-hashed passwords and role-based access (User / Admin)
- Admin panel: dashboard, add / edit / delete products, low-stock list
- One-click **Import from DummyJSON** to refresh the product catalogue

---

## 🧰 Tech Stack

| Layer      | Technology                                   |
|------------|----------------------------------------------|
| Backend    | Java 17, Spring Boot 3.3, Spring MVC         |
| Security   | Spring Security 6 (form login, BCrypt, CSRF) |
| Database   | MySQL 8, Spring Data JPA / Hibernate         |
| Frontend   | Thymeleaf, HTML5, CSS3, vanilla JavaScript   |
| Product data | DummyJSON REST API (no API key needed)     |
| Build tool | Maven                                        |

---

## ✅ Prerequisites

1. **JDK 17** or newer – check with `java -version`
2. **Maven 3.9+** – check with `mvn -version`
3. **MySQL 8** running on `localhost:3306`
4. Internet access on the first start (to download products from DummyJSON)

---

## 🚀 Getting Started

```bash
# 1. Clone the repository
git clone https://github.com/<your-username>/shopease-ecommerce.git
cd shopease-ecommerce

# 2. Set your MySQL credentials (see Configuration below)

# 3. Run the application
mvn spring-boot:run
```

Open **http://localhost:8081** in your browser.

The database (`shopease_db`) and all tables are created automatically. On the first start the app also loads the product catalogue from DummyJSON.

### Demo accounts

| Role  | Email               | Password |
|-------|---------------------|----------|
| Admin | admin@shopease.com  | admin123 |
| User  | user@shopease.com   | user123  |

> Change or remove the demo accounts before deploying anywhere public.

---

## ⚙️ Configuration

Settings live in `src/main/resources/application.properties`.

| Property                      | Default                    | Description                              |
|-------------------------------|----------------------------|------------------------------------------|
| `server.port`                 | `8081`                     | Port the app runs on                     |
| `spring.datasource.username`  | `${DB_USERNAME:root}`      | MySQL username                           |
| `spring.datasource.password`  | `${DB_PASSWORD:root}`      | MySQL password                           |
| `shopease.dummyjson.enabled`  | `true`                     | Load products from DummyJSON on first start |
| `shopease.dummyjson.base-url` | `https://dummyjson.com`    | DummyJSON server                         |

**Do not commit your real database password.** Set it with environment variables instead:

```bash
# Linux / macOS
export DB_USERNAME=root
export DB_PASSWORD=your_password

# Windows (PowerShell)
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
```

---

## 📦 About the Product Data

- On the first start, products are downloaded from `https://dummyjson.com/products` and stored in MySQL. Vehicle and motorcycle categories are skipped.
- DummyJSON prices are in USD, so they are converted to realistic INR prices using a per-category scale and cap (see `ProductEnricher.java`).
- DummyJSON has no size, color or fabric data. Color and fabric are read from the product title or description when mentioned, otherwise a category default is used. Sizes are added for clothing and shoe categories. These values are for demo purposes.
- Admin → Dashboard → **Import from DummyJSON** re-downloads the catalogue. This overwrites manual edits made to imported products.
- If DummyJSON cannot be reached, a small built-in demo catalogue is used instead.
- To start again with a clean database, run `DROP DATABASE shopease_db;` in MySQL and restart the app.

---

## 🗂️ Project Structure

```
shopease
├── database/                     Optional SQL script
├── src/main/java/com/shopease
│   ├── config/                   SecurityConfig, DataSeeder
│   ├── controller/               Auth, Shop, Cart, Order, Admin controllers
│   ├── model/                    User, Product, CartItem, Order, OrderItem, forms
│   ├── repository/               JPA repositories + dynamic product filters
│   └── service/                  Cart, Order, Product, User, DummyJSON import
├── src/main/resources
│   ├── templates/                Thymeleaf pages (+ admin/)
│   ├── static/                   CSS and JavaScript
│   └── application.properties
└── pom.xml
```

---

## 🔐 Security Notes

- Passwords are hashed with BCrypt.
- Forms are protected with CSRF tokens.
- `/admin/**` is restricted to users with the `ADMIN` role; cart, checkout and orders require login.
- Each user can only view and cancel their own orders.

---

## 🛣️ Roadmap

- Admin screen to update order status (Shipped / Delivered)
- Online payment gateway (UPI / cards)
- Product reviews and wishlist
- Pagination for product listings
- Email notifications for orders

---

## 🤝 Contributing

1. Fork the repository
2. Create a branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m "Add my feature"`
4. Push the branch: `git push origin feature/my-feature`
5. Open a Pull Request

---

## 🙏 Acknowledgements

- [DummyJSON](https://dummyjson.com) for the free product API
- [Spring Boot](https://spring.io/projects/spring-boot) and the Spring community
