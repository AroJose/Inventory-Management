const state = {
    products: [],
    categories: [],
    suppliers: [],
    invoices: [],
    users: [],
    currentUser: null,
    pieSegments: [],
    gridView: true,
    confirmAction: null,
    stream: null
};

const $ = (id) => document.getElementById(id);

document.addEventListener("DOMContentLoaded", async () => {
    wireNav();
    wireTheme();
    wireProductModal();
    wireCategoryForm();
    wireSupplierForm();
    wireStockForm();
    wireInvoiceForm();
    wireUserForm();
    wireProductFilters();
    wireBarcodeModal();
    wireReports();
    wireMobileMenu();
    await loadAll();
});

async function loadAll() {
    await safeLoad(loadCurrentUser, "Failed to load user");
    await safeLoad(loadCategories, "Failed to load categories");
    await safeLoad(loadSuppliers, "Failed to load suppliers");
    await safeLoad(loadProducts, "Failed to load products");
    await safeLoad(loadDashboard, "Failed to load dashboard");
    await safeLoad(loadInvoices, "Failed to load invoices");
    await safeLoad(loadStockTransactions, "Failed to load stock transactions");
    if (state.currentUser?.role === "ADMIN") {
        await safeLoad(loadUsers, "Failed to load users");
    }
    populateCommonSelects();
    renderProducts();
    applyRoleAccess();
}

async function safeLoad(fn, errMsg) {
    try {
        await fn();
    } catch (e) {
        toast(e.message || errMsg, "error");
    }
}

function wireNav() {
    document.querySelectorAll("nav a[data-view]").forEach((a) => {
        a.addEventListener("click", (e) => {
            e.preventDefault();
            if (isAnyModalOpen()) return;
            document.querySelectorAll("nav a[data-view]").forEach((x) => x.classList.remove("active"));
            a.classList.add("active");
            const target = a.dataset.view;
            document.querySelectorAll(".view").forEach((v) => v.classList.remove("active-view"));
            $(target).classList.add("active-view");
            if (target === "products") renderProducts();
            if (target === "invoices") loadInvoices();
            if (target === "users" && state.currentUser?.role === "ADMIN") loadUsers();
            closeMobileNav();
        });
    });
}

function isAnyModalOpen() {
    return ["productModal", "confirmModal", "barcodeModal"].some((id) => !$(id).classList.contains("hidden"));
}

function wireTheme() {
    const pref = localStorage.getItem("theme");
    if (pref === "dark") document.body.classList.add("dark");
    setThemeIcon();
    $("themeToggle").addEventListener("click", () => {
        document.body.classList.toggle("dark");
        localStorage.setItem("theme", document.body.classList.contains("dark") ? "dark" : "light");
        setThemeIcon();
    });
}

function setThemeIcon() {
    const dark = document.body.classList.contains("dark");
    $("themeIcon").innerHTML = dark
        ? '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 1 0 9.8 9.8Z"/></svg>'
        : '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41"/></svg>';
}

function wireMobileMenu() {
    $("menuBtn").addEventListener("click", () => {
        $("mainNav").classList.toggle("open");
        $("navOverlay").classList.toggle("hidden");
    });
    $("navOverlay").addEventListener("click", closeMobileNav);
}

function closeMobileNav() {
    $("mainNav").classList.remove("open");
    $("navOverlay").classList.add("hidden");
}

function toast(msg, type = "success") {
    const div = document.createElement("div");
    div.className = `toast ${type}`;
    div.textContent = msg;
    $("toastWrap").appendChild(div);
    setTimeout(() => div.remove(), 3000);
}

async function api(url, options = {}) {
    const res = await fetch(url, options);
    if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        throw new Error(data.message || "Request failed");
    }
    const contentType = res.headers.get("content-type") || "";
    return contentType.includes("application/json") ? res.json() : res.blob();
}

async function loadDashboard() {
    const data = await api("/api/dashboard/summary");
    $("stats").innerHTML = [
        stat("Total Products", data.totalProducts),
        stat("Total Categories", data.totalCategories),
        stat("Total Suppliers", data.totalSuppliers),
        stat("Stock Value", Number(data.totalStockValue).toFixed(2)),
        stat("Low Stock", data.lowStockCount),
        stat("Expiring Soon", data.expiringSoonCount)
    ].join("");
    listMini("recentProducts", data.recentProducts);
    listMini("lowStockItems", data.lowStockItems);
    listMini("expiringItems", data.expiringSoonItems);
    renderSalesChart(data.monthlySales);
    renderCategoryPie(data.categorySales || []);
    renderReportStats(data);
}

function stat(label, value) {
    return `<div class="stat-card"><p>${label}</p><h2>${value}</h2></div>`;
}

function listMini(id, items) {
    $(id).innerHTML = (items || []).map((p) => `
        <div class="mini-item">
            <div style="display:flex; gap:8px; align-items:center;">
                <img src="${p.imagePath || "/img/placeholder.svg"}" alt="">
                <div><strong>${p.name}</strong><br><small>${p.quantity ?? ""}</small></div>
            </div>
        </div>
    `).join("") || "<small>No data</small>";
}

function renderSalesChart(series) {
    const canvas = $("salesChart");
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;
    const points = Array.isArray(series) ? series : [];
    const labels = points.map((p) => p.label);
    const values = points.map((p) => Number(p.total || 0));
    if (!labels.length) {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        return;
    }
    const w = canvas.width = canvas.clientWidth * devicePixelRatio;
    const h = canvas.height = 130 * devicePixelRatio;
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.scale(devicePixelRatio, devicePixelRatio);
    ctx.clearRect(0, 0, w, h);
    const max = Math.max(...values, 10);
    const step = canvas.clientWidth / Math.max(values.length, 1);
    ctx.strokeStyle = "#2E7D32";
    ctx.lineWidth = 2;
    ctx.beginPath();
    values.forEach((v, i) => {
        const x = i * step + step / 2;
        const y = 110 - (v / max) * 90;
        if (i === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y);
    });
    ctx.stroke();
    ctx.fillStyle = "#5f6d76";
    ctx.font = "11px Segoe UI";
    const skip = labels.length > 10 ? 2 : 1;
    labels.forEach((l, i) => {
        if (i % skip === 0 || i === labels.length - 1) {
            ctx.fillText(l, i * step + 8, 146);
        }
    });
}

function renderCategoryPie(categorySales) {
    const canvas = $("categoryPieChart");
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;
    const items = Array.isArray(categorySales) ? categorySales : [];
    const w = canvas.width = canvas.clientWidth * devicePixelRatio;
    const h = canvas.height = 150 * devicePixelRatio;
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.scale(devicePixelRatio, devicePixelRatio);
    ctx.clearRect(0, 0, w, h);
    state.pieSegments = [];

    if (!items.length) {
        ctx.fillStyle = "#7b8790";
        ctx.font = "13px Segoe UI";
        ctx.fillText("No sales data yet", 12, 30);
        return;
    }

    const total = items.reduce((s, x) => s + Number(x.soldQuantity || 0), 0);
    const colors = ["#2E7D32", "#43A047", "#66BB6A", "#FB8C00", "#FFB74D", "#8BC34A", "#26A69A", "#EC407A"];
    const cx = canvas.clientWidth / 2;
    const cy = 75;
    const radius = 58;
    let start = -Math.PI / 2;

    items.forEach((item, idx) => {
        const value = Number(item.soldQuantity || 0);
        const angle = (value / total) * Math.PI * 2;
        const end = start + angle;
        const color = colors[idx % colors.length];

        ctx.beginPath();
        ctx.moveTo(cx, cy);
        ctx.arc(cx, cy, radius, start, end);
        ctx.closePath();
        ctx.fillStyle = color;
        ctx.fill();

        state.pieSegments.push({
            start,
            end,
            categoryId: item.categoryId,
            categoryName: item.categoryName,
            color
        });
        start = end;
    });

    ctx.fillStyle = "#5f6d76";
    ctx.font = "11px Segoe UI";
    items.slice(0, 4).forEach((item, i) => {
        const color = colors[i % colors.length];
        ctx.fillStyle = color;
        ctx.fillRect(10, 12 + i * 18, 10, 10);
        ctx.fillStyle = "#5f6d76";
        ctx.fillText(`${item.categoryName} (${item.soldQuantity})`, 26, 21 + i * 18);
    });

    canvas.onclick = (e) => {
        const rect = canvas.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        const dx = x - cx;
        const dy = y - cy;
        const dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > radius) return;
        let angle = Math.atan2(dy, dx);
        if (angle < -Math.PI / 2) angle += Math.PI * 2;
        const found = state.pieSegments.find((s) => angle >= s.start && angle <= s.end);
        if (found) {
            loadTopSoldProducts(found.categoryId, found.categoryName);
        }
    };
}

async function loadTopSoldProducts(categoryId, categoryName) {
    if (!$("topSoldTitle") || !$("pieCategoryProducts")) return;
    const items = await api(`/api/dashboard/top-category-products/${categoryId}`);
    $("topSoldTitle").textContent = `Top Sold Products - ${categoryName}`;
    $("pieCategoryProducts").innerHTML = items.map((p) => `
        <div class="list-item">
            <div style="display:flex;align-items:center;gap:8px;">
                <img src="${p.imagePath || "/img/placeholder.svg"}" alt="${p.productName}">
                <span>${p.productName}</span>
            </div>
            <small>Sold: ${p.soldQuantity}</small>
        </div>
    `).join("") || "<small>No sold products found for this category.</small>";
}

async function loadCategories() {
    state.categories = await api("/api/categories");
    $("categoryList").innerHTML = state.categories.map((c) => rowItem(c.name, () => delCategory(c.id))).join("");
}

async function loadSuppliers() {
    state.suppliers = await api("/api/suppliers");
    $("supplierList").innerHTML = state.suppliers.map((s) => rowItem(s.name, () => delSupplier(s.id))).join("");
}

async function loadProducts() {
    state.products = await api("/api/products");
}

async function loadInvoices() {
    state.invoices = await api("/api/invoices");
    const isAdmin = state.currentUser?.role === "ADMIN";
    $("invoiceList").innerHTML = state.invoices.map((i) => `
        <div class="list-item">
            <span>${i.invoiceNumber} - ${i.customerName} (${Number(i.totalAmount).toFixed(2)})</span>
            <div>
                <button class="btn" onclick="downloadInvoicePdf(${i.id})">PDF</button>
                ${isAdmin ? `<button class="btn" onclick="deleteInvoice(${i.id})">Delete</button>` : ""}
            </div>
        </div>
    `).join("") || "<small>No invoices yet</small>";
    renderReportInvoices();
}

async function loadUsers() {
    state.users = await api("/api/users");
    $("userList").innerHTML = state.users.map((u) => `
        <div class="list-item">
            <span>${u.username}</span>
            <div>
                <small>${u.role}</small>
                <button class="btn" onclick="deleteUser(${u.id})">Delete</button>
            </div>
        </div>
    `).join("") || "<small>No users</small>";
}

async function loadCurrentUser() {
    state.currentUser = await api("/api/auth/me");
}

function applyRoleAccess() {
    const role = state.currentUser?.role || "VIEWER";
    const isAdmin = role === "ADMIN";
    const isViewer = role === "VIEWER";
    $("usersNavLink").classList.toggle("hidden", !isAdmin);

    const viewerHidden = [
        "categoryCreatePanel",
        "supplierCreatePanel",
        "stockCreatePanel",
        "invoiceCreatePanel",
        "newProductBtn"
    ];
    viewerHidden.forEach((id) => {
        const el = $(id);
        if (!el) return;
        el.classList.toggle("hidden", isViewer);
    });

    // Keep admin user creation panel hidden for non-admin roles.
    const userForm = $("userForm");
    if (userForm) {
        userForm.classList.toggle("hidden", !isAdmin);
    }
}

async function loadStockTransactions() {
    const txs = await api("/api/stock/transactions");
    const isAdmin = state.currentUser?.role === "ADMIN";
    $("stockTxList").innerHTML = txs.map((t) => `
        <div class="list-item">
            <span>${t.product?.name || ""}</span>
            <div>
                <small>${t.type} ${t.quantity} (${new Date(t.date).toLocaleString()})</small>
                ${isAdmin ? `<button class="btn" onclick="deleteStockTx(${t.id})">Delete</button>` : ""}
            </div>
        </div>
    `).join("") || "<small>No transactions</small>";
}

function rowItem(label, onDelete) {
    const canDelete = state.currentUser?.role !== "VIEWER";
    if (!canDelete) {
        return `<div class="list-item"><span>${label}</span></div>`;
    }
    const id = Math.random().toString(36).slice(2);
    setTimeout(() => {
        const el = document.querySelector(`[data-del="${id}"]`);
        if (el) el.onclick = onDelete;
    }, 0);
    return `<div class="list-item"><span>${label}</span><button class="btn" data-del="${id}">Delete</button></div>`;
}

function populateCommonSelects() {
    const cOpts = state.categories.map((c) => `<option value="${c.id}">${c.name}</option>`).join("");
    const sOpts = state.suppliers.map((s) => `<option value="${s.id}">${s.name}</option>`).join("");
    $("categoryFilter").innerHTML = `<option value="">All categories</option>${cOpts}`;
    $("productCategorySelect").innerHTML = cOpts;
    $("productSupplierSelect").innerHTML = sOpts;
    $("stockProductSelect").innerHTML = state.products.map((p) => `<option value="${p.id}">${p.name}</option>`).join("");
    document.querySelectorAll(".invoice-product").forEach((sel) => sel.innerHTML = state.products.map((p) => `<option value="${p.id}" data-price="${p.sellingPrice}">${p.name}</option>`).join(""));
}

function renderProducts() {
    const filtered = state.products.filter((p) => {
        const q = $("searchInput").value.trim().toLowerCase();
        const cat = $("categoryFilter").value;
        const low = $("lowStockFilter").checked;
        const exp = $("expiryFilter").checked;
        const matchText = !q || p.name.toLowerCase().includes(q) || (p.barcode || "").toLowerCase().includes(q);
        const matchCat = !cat || String(p.category?.id) === cat;
        const isLow = p.quantity < 10;
        const isExp = new Date(p.expiryDate) <= new Date(Date.now() + 7 * 86400000);
        return matchText && matchCat && (!low || isLow) && (!exp || isExp);
    });

    $("productsGrid").innerHTML = filtered.map(cardHtml).join("") || "<small>No products found</small>";
    $("productsTableBody").innerHTML = filtered.map(tableRowHtml).join("");

    const canWrite = state.currentUser?.role !== "VIEWER";
    document.querySelectorAll("[data-edit]").forEach((btn) => {
        if (!canWrite) btn.classList.add("hidden");
        btn.onclick = () => openProductModal(btn.dataset.edit);
    });
    document.querySelectorAll("[data-del-product]").forEach((btn) => {
        if (!canWrite) btn.classList.add("hidden");
        btn.onclick = () => deleteProduct(btn.dataset.delProduct);
    });
}

function cardHtml(p) {
    const low = p.quantity < 10;
    const exp = new Date(p.expiryDate) <= new Date(Date.now() + 7 * 86400000);
    return `
    <article class="product-card">
        <img src="${p.imagePath || "/img/placeholder.svg"}" alt="${p.name}">
        <div class="card-actions">
            <button data-edit="${p.id}">Edit</button>
            <button data-del-product="${p.id}">Delete</button>
        </div>
        <div class="product-body">
            <strong>${p.name}</strong><br>
            <small>${p.category?.name || ""}</small>
            <div>Qty: ${p.quantity}</div>
            <div>Price: ${Number(p.sellingPrice).toFixed(2)}</div>
            <div>Expiry: ${p.expiryDate}</div>
            <div class="badges">
                ${low ? '<span class="badge low">Low Stock</span>' : ''}
                ${exp ? '<span class="badge exp">Expiring</span>' : ''}
            </div>
        </div>
    </article>`;
}

function tableRowHtml(p) {
    const low = p.quantity < 10 ? '<span class="badge low">Low</span>' : '';
    const exp = new Date(p.expiryDate) <= new Date(Date.now() + 7 * 86400000) ? '<span class="badge exp">Exp</span>' : '';
    return `<tr>
        <td><img class="thumb" src="${p.imagePath || "/img/placeholder.svg"}" alt=""></td>
        <td>${p.name}</td><td>${p.category?.name || ""}</td><td>${p.quantity}</td>
        <td>${Number(p.sellingPrice).toFixed(2)}</td><td>${p.expiryDate}</td><td>${low}${exp}</td>
        <td><button class="btn" data-edit="${p.id}">Edit</button> <button class="btn" data-del-product="${p.id}">Delete</button></td>
    </tr>`;
}

function wireProductFilters() {
    ["searchInput", "categoryFilter", "lowStockFilter", "expiryFilter"].forEach((id) => {
        $(id).addEventListener("input", renderProducts);
        $(id).addEventListener("change", renderProducts);
    });

    $("toggleViewBtn").addEventListener("click", () => {
        state.gridView = !state.gridView;
        $("productsGrid").classList.toggle("hidden", !state.gridView);
        $("productsTableWrap").classList.toggle("hidden", state.gridView);
        $("toggleViewBtn").textContent = state.gridView ? "Table View" : "Grid View";
    });

    $("pdfProductsBtn").addEventListener("click", async () => {
        const blob = await api("/api/products/export/pdf");
        downloadBlob(blob, "products.pdf");
    });
}

function wireProductModal() {
    $("newProductBtn").addEventListener("click", () => openProductModal());
    $("closeProductModal").addEventListener("click", () => closeProductModal());

    const input = $("imageFileInput");
    const zone = $("uploadZone");
    input.addEventListener("change", () => previewImage(input.files[0]));
    zone.addEventListener("dragover", (e) => e.preventDefault());
    zone.addEventListener("drop", (e) => {
        e.preventDefault();
        input.files = e.dataTransfer.files;
        previewImage(input.files[0]);
    });

    $("productForm").addEventListener("submit", async (e) => {
        e.preventDefault();
        const form = e.target;
        const fd = new FormData(form);
        const id = form.id.value;
        const pp = Number(fd.get("purchasePrice"));
        const sp = Number(fd.get("sellingPrice"));
        if (sp < pp) return toast("Selling price cannot be less than purchase price", "error");
        if (Number(fd.get("quantity")) < 0) return toast("Quantity cannot be negative", "error");
        if (new Date(fd.get("expiryDate")) < new Date(new Date().toDateString())) return toast("Expiry date cannot be in past", "error");

        try {
            await api(id ? `/api/products/${id}` : "/api/products", {method: id ? "PUT" : "POST", body: fd});
            form.reset();
            $("imagePreview").src = "/img/placeholder.svg";
            closeProductModal();
            await loadProducts();
            populateCommonSelects();
            renderProducts();
            loadDashboard();
            toast("Product saved");
        } catch (err) {
            toast(err.message, "error");
        }
    });
}

function closeProductModal() {
    $("productModal").classList.add("hidden");
}

function openProductModal(id = null) {
    const form = $("productForm");
    form.reset();
    form.id.value = "";
    $("imagePreview").src = "/img/placeholder.svg";
    $("productModalTitle").textContent = id ? "Edit Product" : "Add Product";
    if (id) {
        const p = state.products.find((x) => String(x.id) === String(id));
        if (!p) return;
        form.id.value = p.id;
        form.name.value = p.name;
        form.description.value = p.description || "";
        form.categoryId.value = p.category?.id;
        form.brand.value = p.brand || "";
        form.batchNumber.value = p.batchNumber || "";
        form.barcode.value = p.barcode || "";
        form.expiryDate.value = p.expiryDate;
        form.purchasePrice.value = p.purchasePrice;
        form.sellingPrice.value = p.sellingPrice;
        form.quantity.value = p.quantity;
        form.supplierId.value = p.supplier?.id;
        $("imagePreview").src = p.imagePath || "/img/placeholder.svg";
    }
    $("productModal").classList.remove("hidden");
}

async function deleteProduct(id) {
    if (!confirm("Delete product?")) return;
    await api(`/api/products/${id}`, {method: "DELETE"});
    toast("Product deleted", "warning");
    await loadProducts();
    populateCommonSelects();
    renderProducts();
    loadDashboard();
}

function previewImage(file) {
    if (!file) return;
    if (!["image/png", "image/jpeg"].includes(file.type)) return toast("Only JPG/PNG allowed", "error");
    if (file.size > 2 * 1024 * 1024) return toast("Image must be <= 2MB", "error");
    const reader = new FileReader();
    reader.onload = (e) => $("imagePreview").src = e.target.result;
    reader.readAsDataURL(file);
}

function wireCategoryForm() {
    $("categoryForm").addEventListener("submit", async (e) => {
        e.preventDefault();
        const fd = new FormData(e.target);
        await api("/api/categories", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(Object.fromEntries(fd.entries()))
        });
        e.target.reset();
        await loadCategories();
        toast("Category saved");
        populateCommonSelects();
    });
}

async function delCategory(id) {
    await api(`/api/categories/${id}`, {method: "DELETE"});
    await loadCategories();
    toast("Category removed", "warning");
    populateCommonSelects();
}

function wireSupplierForm() {
    $("supplierForm").addEventListener("submit", async (e) => {
        e.preventDefault();
        const fd = new FormData(e.target);
        await api("/api/suppliers", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(Object.fromEntries(fd.entries()))
        });
        e.target.reset();
        await loadSuppliers();
        toast("Supplier saved");
        populateCommonSelects();
    });
}

async function delSupplier(id) {
    await api(`/api/suppliers/${id}`, {method: "DELETE"});
    await loadSuppliers();
    toast("Supplier removed", "warning");
    populateCommonSelects();
}

function wireStockForm() {
    $("stockForm").addEventListener("submit", async (e) => {
        e.preventDefault();
        const fd = new FormData(e.target);
        const payload = {
            productId: Number(fd.get("productId")),
            type: fd.get("type"),
            quantity: Number(fd.get("quantity")),
            referenceNote: fd.get("referenceNote")
        };
        state.confirmAction = async () => {
            await api("/api/stock/adjust", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(payload)
            });
            e.target.reset();
            toast("Stock updated");
            await Promise.all([loadProducts(), loadStockTransactions(), loadDashboard()]);
            populateCommonSelects();
            renderProducts();
        };
        $("confirmModal").classList.remove("hidden");
    });

    $("confirmNo").onclick = () => $("confirmModal").classList.add("hidden");
    $("confirmYes").onclick = async () => {
        $("confirmModal").classList.add("hidden");
        if (state.confirmAction) {
            try { await state.confirmAction(); }
            catch (e) { toast(e.message, "error"); }
            state.confirmAction = null;
        }
    };
}

function wireInvoiceForm() {
    if (!$("invoiceForm")) return;
    $("addInvoiceItemBtn").addEventListener("click", addInvoiceItemRow);
    addInvoiceItemRow();
    $("invoiceForm").addEventListener("submit", async (e) => {
        e.preventDefault();
        const name = e.target.customerName.value.trim();
        if (!name) return;
        const items = [...document.querySelectorAll(".invoice-item-row")].map((row) => ({
            productId: Number(row.querySelector(".invoice-product").value),
            quantity: Number(row.querySelector(".invoice-qty").value)
        })).filter((x) => x.quantity > 0);
        if (!items.length) return toast("Add at least one item", "warning");
        const invoice = await api("/api/invoices", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({customerName: name, items})
        });
        toast("Invoice created");
        e.target.reset();
        $("invoiceItems").innerHTML = "";
        addInvoiceItemRow();
        await Promise.all([loadInvoices(), loadProducts(), loadDashboard(), loadStockTransactions()]);
        populateCommonSelects();
        renderProducts();
        downloadInvoicePdf(invoice.id);
    });
}

function addInvoiceItemRow() {
    const row = document.createElement("div");
    row.className = "invoice-item-row";
    row.style.display = "grid";
    row.style.gridTemplateColumns = "2fr 1fr auto";
    row.style.gap = "8px";
    row.innerHTML = `
        <select class="invoice-product"></select>
        <input type="number" min="1" class="invoice-qty" value="1">
        <button type="button" class="btn">X</button>`;
    row.querySelector("button").onclick = () => {
        row.remove();
        recalcInvoiceTotal();
    };
    row.querySelector(".invoice-product").onchange = recalcInvoiceTotal;
    row.querySelector(".invoice-qty").oninput = recalcInvoiceTotal;
    $("invoiceItems").appendChild(row);
    populateCommonSelects();
    recalcInvoiceTotal();
}

function recalcInvoiceTotal() {
    let total = 0;
    document.querySelectorAll(".invoice-item-row").forEach((row) => {
        const productId = Number(row.querySelector(".invoice-product").value);
        const qty = Number(row.querySelector(".invoice-qty").value || 0);
        const product = state.products.find((p) => p.id === productId);
        if (product) total += Number(product.sellingPrice) * qty;
    });
    $("invoiceTotal").textContent = `Total: ${total.toFixed(2)}`;
}

window.downloadInvoicePdf = async (id) => {
    const blob = await api(`/api/invoices/${id}/pdf`);
    downloadBlob(blob, `invoice-${id}.pdf`);
};

window.deleteInvoice = async (id) => {
    if (!confirm("Delete this invoice? Stock will be restored.")) return;
    await api(`/api/invoices/${id}`, {method: "DELETE"});
    toast("Invoice deleted", "warning");
    await Promise.all([loadInvoices(), loadProducts(), loadDashboard(), loadStockTransactions()]);
    renderProducts();
};

window.deleteStockTx = async (id) => {
    if (!confirm("Delete this stock transaction?")) return;
    await api(`/api/stock/transactions/${id}`, {method: "DELETE"});
    toast("Stock transaction deleted", "warning");
    await Promise.all([loadStockTransactions(), loadProducts(), loadDashboard()]);
    renderProducts();
};

window.deleteUser = async (id) => {
    if (!confirm("Delete this user?")) return;
    await api(`/api/users/${id}`, {method: "DELETE"});
    toast("User deleted", "warning");
    await loadUsers();
};

function wireReports() {
    $("reportProductsPdfBtn").addEventListener("click", async () => {
        const blob = await api("/api/products/export/pdf");
        downloadBlob(blob, "products-report.pdf");
    });
    $("reportLatestInvoicePdfBtn").addEventListener("click", async () => {
        if (!state.invoices.length) return toast("No invoices available", "warning");
        await window.downloadInvoicePdf(state.invoices[0].id);
    });
}

function wireUserForm() {
    if (!$("userForm")) return;
    $("userForm").addEventListener("submit", async (e) => {
        e.preventDefault();
        const fd = new FormData(e.target);
        await api("/api/users", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(Object.fromEntries(fd.entries()))
        });
        e.target.reset();
        await loadUsers();
        toast("User created");
    });
}

function renderReportStats(data) {
    if (!$("reportStats")) return;
    $("reportStats").innerHTML = [
        stat("Products", data.totalProducts),
        stat("Categories", data.totalCategories),
        stat("Suppliers", data.totalSuppliers),
        stat("Low Stock", data.lowStockCount),
        stat("Expiring Soon", data.expiringSoonCount),
        stat("Stock Value", Number(data.totalStockValue).toFixed(2))
    ].join("");
}

function renderReportInvoices() {
    if (!$("reportInvoiceList")) return;
    $("reportInvoiceList").innerHTML = state.invoices.slice(0, 8).map((i) => `
        <div class="list-item">
            <span>${i.invoiceNumber} - ${i.customerName}</span>
            <button class="btn" onclick="downloadInvoicePdf(${i.id})">PDF</button>
        </div>
    `).join("") || "<small>No invoices available</small>";
}

function downloadBlob(blob, fileName) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = fileName;
    a.click();
    URL.revokeObjectURL(url);
}

function wireBarcodeModal() {
    $("barcodeScanBtn").onclick = openBarcodeModal;
    $("closeBarcodeModal").onclick = closeBarcodeModal;
    $("barcodeSearchBtn").onclick = () => searchByBarcode($("barcodeManualInput").value.trim());
    $("barcodeManualInput").addEventListener("change", () => searchByBarcode($("barcodeManualInput").value.trim()));
}

async function openBarcodeModal() {
    $("barcodeModal").classList.remove("hidden");
    if (!navigator.mediaDevices?.getUserMedia) return;
    try {
        state.stream = await navigator.mediaDevices.getUserMedia({video: {facingMode: "environment"}});
        $("barcodeVideo").srcObject = state.stream;
        scanLoop();
    } catch (e) {
        toast("Camera access denied", "warning");
    }
}

function closeBarcodeModal() {
    $("barcodeModal").classList.add("hidden");
    if (state.stream) {
        state.stream.getTracks().forEach((t) => t.stop());
        state.stream = null;
    }
}

async function scanLoop() {
    if (!("BarcodeDetector" in window) || !state.stream) return;
    const detector = new BarcodeDetector({formats: ["ean_13", "ean_8", "code_128", "upc_a", "upc_e"]});
    const video = $("barcodeVideo");
    while (state.stream) {
        try {
            const codes = await detector.detect(video);
            if (codes.length) {
                $("barcodeManualInput").value = codes[0].rawValue;
                await searchByBarcode(codes[0].rawValue);
                break;
            }
        } catch (_) {}
        await new Promise((r) => setTimeout(r, 350));
    }
}

async function searchByBarcode(barcode) {
    if (!barcode) return;
    try {
        const product = await api(`/api/products/barcode/${barcode}`);
        $("searchInput").value = product.name;
        renderProducts();
        closeBarcodeModal();
        toast(`Found ${product.name}`);
    } catch (e) {
        toast("Barcode not found", "warning");
    }
}
