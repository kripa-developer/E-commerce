# 🛒 NovaCart

A full-stack e-commerce web application built with **Spring Boot** and **Angular 18**.

🔗 **Live Demo:** [novacart-nu.vercel.app](https://novacart-nu.vercel.app)

---

## 📁 Project Structure

```
E-commerce/
├── novaCart/          # Spring Boot backend
│   └── src/main/java/com/novacart/
│       ├── auth/      # Authentication & JWT
│       ├── product/   # Products & Categories
│       ├── order/     # Orders
│       ├── cart/      # Shopping cart
│       ├── review/    # Product reviews
│       ├── user/      # User management
│       └── common/    # Security, config
└── src/               # Angular 18 frontend
    └── app/
        ├── core/      # Auth, services, models
        ├── pages/     # Feature pages
        └── shared/    # Reusable components
```

---

## Tech Stack

### Backend
- **Java 17** + **Spring Boot 3.3**
- **Spring Security** with JWT authentication (access + refresh tokens)
- **Spring Data JPA** + **Hibernate**
- **MySQL** (Aiven cloud database)
- **Resend** for transactional emails
- **Maven** for build management

### Frontend
- **Angular 18** (standalone components)
- **TypeScript**
- **RxJS** + Angular Signals
- **Angular Router** with lazy loading

### Infrastructure
- **Backend:** Render
- **Frontend:** Vercel
- **Database:** Aiven MySQL

---

## Features

### Customer
- JWT-based authentication (login, register, refresh tokens)
- Forgot password with email reset link
- Browse products with search and category filter
- Product detail page with reviews and ratings
- Shopping cart (add, update, remove)
- Wishlist
- Order history
- Profile management (addresses, reviews, password change)
- Address book with India state/country dropdowns

### Admin
- Admin dashboard
- Add / edit / delete products
- Add / edit / delete categories
- Product image management

---

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL

### Backend Setup

```bash
cd novaCart
# Configure src/main/resources/application-dev.yml with your local DB
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend runs on `http://localhost:8811`

### Frontend Setup

```bash
# From root directory
npm install
ng serve
```

Frontend runs on `http://localhost:4200`

---

## Environment Variables

### Backend (Render)

| Variable | Description |
|----------|-------------|
| `DATABASE_URL` | Aiven MySQL JDBC URL |
| `DATABASE_USERNAME` | DB username |
| `DATABASE_PASSWORD` | DB password |
| `JWT_SECRET` | JWT signing secret |
| `APP_CORS_ALLOWED_ORIGINS` | Frontend URL |
| `RESEND_API_KEY` | Resend API key for emails |
| `APP_MAIL_FROM` | From email address |
| `APP_FRONTEND_URL` | Frontend base URL |

### Frontend (Vercel)

| Variable | Description |
|----------|-------------|
| `NG_APP_API_URL` | Backend API base URL |

---

## Database

The project uses **MySQL** with Hibernate schema validation. Tables must be created manually before first run.

Key tables: `app_users`, `products`, `categories`, `orders`, `order_items`, `cart`, `cart_items`, `reviews`, `refresh_tokens`, `password_reset_tokens`, `addresses`, `wishlist_items`

---

## API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register |
| POST | `/api/v1/auth/login` | Login |
| POST | `/api/v1/auth/refresh` | Refresh token |
| POST | `/api/v1/auth/logout` | Logout |
| POST | `/api/v1/auth/forgot-password` | Send reset link |
| POST | `/api/v1/auth/reset-password` | Reset password |

### Products
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/products` | Search / list products |
| GET | `/api/v1/products/:slug` | Product detail |
| GET | `/api/v1/categories` | List categories |

### Cart & Orders
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/cart` | Get cart |
| POST | `/api/v1/cart/items` | Add to cart |
| GET | `/api/v1/orders` | My orders |

---

## Architecture

```
Angular Frontend  ──►  Spring Boot REST API  ──►  MySQL (Aiven)
     │                        │
   Vercel                   Render
                              │
                           Resend (Email)
```

---

<p align="center">Built with love by <a href="https://github.com/kripa-developer">kripa-developer</a></p>
