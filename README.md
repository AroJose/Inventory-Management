# Processed Food Inventory Management System

Spring Boot + MySQL + HTML/CSS/JavaScript inventory platform for finished processed food products.

## Features
- Product, Category, Supplier management
- Stock In / Stock Out with transaction history
- Expiry tracking and low-stock alerts
- Barcode scan (camera/manual) + barcode search
- Invoice creation with stock deduction
- PDF export for invoices and product list
- Dashboard analytics:
  - Monthly sales (from January 2026)
  - Top-selling category pie chart
  - Click pie segment to see top sold products with images
- Product image upload (JPG/JPEG/PNG, max 2MB)
- Role-based access: `ADMIN`, `STAFF`, `VIEWER`
- Dark mode + mobile responsive sidebar UI

## Tech Stack
- Java 17+ (tested with Java 21)
- Spring Boot 3
- Spring Security
- Spring Data JPA (Hibernate)
- MySQL 8
- Thymeleaf templates + vanilla JS/CSS
- OpenPDF for PDF generation

## Project Structure
```
src/main/java/com/processedfood/inventory/
  config/
  controller/
  dto/
  exception/
  model/
  repository/
  service/
src/main/resources/
  templates/
  static/
```

## Prerequisites
1. Java JDK installed (`java -version`)
2. Maven installed (`mvn -version`)
3. MySQL running locally

If you download the repo as a ZIP on another computer, install Java + Maven and start MySQL before running.

## Database Configuration
Configured in `src/main/resources/application.properties`:
- URL: `${DB_URL}` (default: local `processed_food_inventory`)
- Username: `${DB_USER}` (default: `root`)
- Password: `${DB_PASSWORD}` (default: `root123`)

You can override at runtime:
- PowerShell:
  - `$env:DB_USER="root"`
  - `$env:DB_PASSWORD="root123"`
  - `$env:DB_URL="jdbc:mysql://localhost:3306/processed_food_inventory?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"`

Create the database once (optional if `createDatabaseIfNotExist=true` in URL):
```sql
CREATE DATABASE processed_food_inventory;
```

## Run Locally
1. Open terminal in project folder:
   - `Y:\Inventory Management`
2. Run:
   - `mvn spring-boot:run`
3. Open:
   - `http://localhost:8080`

## Default Users
- Admin: `admin / admin123`
- Staff: `staff / staff123`
- Viewer: `viewer / viewer123`

## Role Permissions
- `ADMIN`
  - Full create/update/delete access
  - Can manage users
  - Can delete stock transactions, invoices, users
- `STAFF`
  - Operational create/update actions (products, stock, invoices, etc.)
  - No user management
- `VIEWER`
  - Read-only access to inventory, reports, dashboard

## Image Upload Behavior
- Upload folder: `uploads/` (project root, persistent across restarts)
- Allowed formats: JPG, JPEG, PNG
- Max file size: 2MB
- If no image is uploaded, placeholder image is used

## Important API Endpoints
- Auth:
  - `GET /api/auth/me`
- Dashboard:
  - `GET /api/dashboard/summary`
  - `GET /api/dashboard/top-category-products/{categoryId}`
- Products:
  - `GET /api/products`
  - `POST /api/products` (multipart)
  - `PUT /api/products/{id}` (multipart)
  - `DELETE /api/products/{id}`
  - `GET /api/products/barcode/{barcode}`
  - `GET /api/products/export/pdf`
- Stock:
  - `POST /api/stock/adjust`
  - `GET /api/stock/transactions`
  - `DELETE /api/stock/transactions/{id}` (admin)
- Invoices:
  - `GET /api/invoices`
  - `POST /api/invoices`
  - `GET /api/invoices/{id}/pdf`
  - `DELETE /api/invoices/{id}` (admin)
- Users (admin):
  - `GET /api/users`
  - `POST /api/users`
  - `DELETE /api/users/{id}`

## .gitignore / Safe Upload Notes
- `target/` is ignored
- Runtime uploads are ignored:
  - `uploads/*`
  - except `.gitkeep`
- IDE/system artifacts are ignored

Before pushing:
1. Ensure no sensitive credentials are committed
2. Ensure only source code + docs are tracked
3. Run `git status` and review staged files (you should see deletions from `target/` and uploaded runtime images)

## Build Check
- Compile:
  - `mvn -q -DskipTests compile`
