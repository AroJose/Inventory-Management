# CJ ICE SHOPZ (React + Node.js + Postgres)

## Quick Start

1) Server
```
cd server
npm install
npm run dev
```

2) Client (new terminal)
```
cd client
npm install
npm run dev
```

Open the Vite URL printed in the client terminal.

## Default Admin

- Email: admin@example.com
- Password: admin123

## Notes

- Postgres is used for persistence (auto-creates tables on server start).
- Use `DATABASE_URL` for deployment (Render Postgres provides this).
- Local fallback config is available via `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`.
- Configure via `server/.env` (copy from `.env.example`).
- CORS is enabled for local dev.
- Checkout generates a bill and creates an order in Postgres.
- Categories and products can be added from the Admin page.
- Image uploads are stored locally in `server/uploads` and served at `/uploads/*`.
- Admin can edit/delete products, categories, ads, and quotes, and download invoices.

---

# Processed Food Inventory (Spring Boot + MySQL)

This repo now also includes a full Spring Boot inventory system in the root `src/` with:

- Product, category, supplier, stock, invoice modules
- Product image upload (`/static/uploads`, JPG/JPEG/PNG, 2MB max)
- Barcode scan/search support (camera + manual)
- Invoice generation + PDF export
- Products PDF export
- Dark mode toggle (stored in `localStorage`)
- Spring Security login

## Run

1. Ensure MySQL is running and credentials match:
   - username: `root`
   - password: `root123`
   - database: `processed_food_inventory`
2. Run Spring Boot from repo root:
   - IntelliJ: run `ProcessedFoodInventoryApplication`
   - VS Code terminal (with Maven installed): `mvn spring-boot:run`
3. Open `http://localhost:8080`

## Default Admin

- Username: `admin`
- Password: `admin123`
