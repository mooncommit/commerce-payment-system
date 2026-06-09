const STORAGE = {
  token: "ec2ShopAccessToken",
  refreshToken: "ec2ShopRefreshToken",
  user: "ec2ShopUser",
};

const CATEGORY_LABELS = {
  DIGITAL: "전자기기",
  ACCESSORY: "잡화",
  FASHION: "의류",
  BOOK: "도서",
};

const STATUS_LABELS = {
  ON_SALE: "재고 있음",
  SOLD_OUT: "품절",
};

const PRODUCT_IMAGES = {
  1: "assets/products/images/clean_code_book.png", // 클린 코드
  2: "assets/products/images/wireless_mouse.png", // 마우스
  3: "assets/products/images/lion_keyring.png", // 사자 키링
  4: "assets/products/images/leather_keyring.png", // 가죽 키링
  5: "assets/products/images/bluetooth_headphones.png", // 헤드폰
  6: "assets/products/images/casual_hoodie.png", // 후드티
  7: "assets/products/images/windbreaker_jacket.png", // 자켓
  8: "assets/products/images/mechanical_keyboard.png", // 기계식 키보드
  9: "assets/products/images/prod_headphone.png", // 프리미엄 헤드폰
  10: "assets/products/images/prod_hoodie.png", // 블랙 기모 후드티
  11: "assets/products/images/prod_jacket.png", // 아웃도어 자켓
  12: "assets/products/images/prod_keyboard.png", // 게이밍 키보드
  13: "assets/products/images/keyring.png", // 기본 로고 키링
  14: "assets/products/images/pig_keyring.png", // 핑크 돼지 키링
  15: "assets/products/images/lg_monitor.png", // LG 스마트 모니터
  16: "assets/products/images/book_java.png", // 혼자 공부하는 자바
  17: "assets/products/images/book_spring.png", // 스프링 교과서
  18: "assets/products/images/book_algorithm.png", // 알고리즘 입문
};

const LOCAL_PRODUCTS = [
  {
    productId: 1,
    name: "클린 코드",
    price: 30000,
    stock: 10,
    description: "개발자 필독서",
    status: "ON_SALE",
    category: "BOOK",
  },
  {
    productId: 2,
    name: "로지텍 무선 마우스",
    price: 25000,
    stock: 20,
    description: "로지텍 무선 마우스",
    status: "ON_SALE",
    category: "DIGITAL",
  },
  {
    productId: 3,
    name: "멋쟁이 사자 키링",
    price: 15000,
    stock: 50,
    description: "귀여운 사자 인형 키링",
    status: "ON_SALE",
    category: "ACCESSORY",
  },
  {
    productId: 4,
    name: "가죽 키링",
    price: 1000,
    stock: 0,
    description: "고급스러운 가죽 키링",
    status: "SOLD_OUT",
    category: "ACCESSORY",
  },
  {
    productId: 5,
    name: "블루투스 헤드폰",
    price: 150000,
    stock: 15,
    description: "노이즈 캔슬링 무선 헤드폰",
    status: "ON_SALE",
    category: "DIGITAL",
  },
  {
    productId: 6,
    name: "캐주얼 후드티",
    price: 45000,
    stock: 40,
    description: "편안한 오버핏 후드티",
    status: "ON_SALE",
    category: "FASHION",
  },
  {
    productId: 7,
    name: "바람막이 자켓",
    price: 60000,
    stock: 25,
    description: "생활 방수 바람막이",
    status: "ON_SALE",
    category: "FASHION",
  },
  {
    productId: 8,
    name: "기계식 키보드",
    price: 85000,
    stock: 10,
    description: "청축 기계식 키보드",
    status: "ON_SALE",
    category: "DIGITAL",
  },
];

const state = {
  token: localStorage.getItem(STORAGE.token),
  refreshToken: localStorage.getItem(STORAGE.refreshToken),
  user: readStoredUser(),
  products: [],
  productPage: null,
  cart: null,
  pointBalance: 0,
  currentPage: 0,
  loading: false,
};

const els = {};

document.addEventListener("DOMContentLoaded", () => {
  cacheElements();
  bindEvents();
  renderSession();
  loadProducts(0);
  if (state.token) {
    refreshCart(false);
    refreshAccount(false);
  }
});

function cacheElements() {
  [
    "productGrid",
    "productTitle",
    "productMeta",
    "pageMeta",
    "prevPageBtn",
    "nextPageBtn",
    "categoryFilter",
    "statusFilter",
    "sortFilter",
    "searchInput",
    "productDrawer",
    "productDetail",
    "cartDrawer",
    "cartItems",
    "cartCount",
    "cartSubtotal",
    "usePointInput",
    "pointHint",
    "checkoutTotal",
    "authDialog",
    "loginEmail",
    "loginPassword",
    "signupName",
    "signupEmail",
    "signupPassword",
    "signupPhone",
    "authTabs",
    "loginPane",
    "signupPane",
    "authAccountPane",
    "authUserName",
    "authUserEmail",
    "authUserId",
    "authPointBalance",
    "logoutButton",
    "toast",
  ].forEach((id) => {
    els[id] = document.getElementById(id);
  });
}

function bindEvents() {
  document.addEventListener("click", handleClick);
  document.addEventListener("input", handleInput);
  document.addEventListener("change", handleChange);

  els.categoryFilter.addEventListener("change", () => {
    updateCategoryNav();
    loadProducts(0);
  });

  [els.statusFilter, els.sortFilter].forEach((el) => {
    el.addEventListener("change", () => loadProducts(0));
  });

  els.searchInput.addEventListener("input", () => renderProducts());
  els.prevPageBtn.addEventListener("click", () => loadProducts(state.currentPage - 1));
  els.nextPageBtn.addEventListener("click", () => loadProducts(state.currentPage + 1));

  [els.productDrawer, els.cartDrawer].forEach((drawer) => {
    drawer.addEventListener("click", (event) => {
      if (event.target === drawer) {
        closeDrawers();
      }
    });
  });
}

async function handleClick(event) {
  const actionTarget = event.target.closest("[data-action]");
  if (actionTarget) {
    const action = actionTarget.dataset.action;
    if (action === "scroll-products") return scrollToSection("products");
    if (action === "focus-search") return focusSearch();
    if (action === "open-auth") return openAuthDialog();
    if (action === "open-cart") return openCart();
    if (action === "close-cart") return closeDrawer(els.cartDrawer);
    if (action === "close-product") return closeDrawer(els.productDrawer);
    if (action === "refresh-account") return refreshAccount();
    if (action === "login") return login();
    if (action === "signup") return signup();
    if (action === "logout") return logout();
    if (action === "detail-qty-minus") return adjustDetailQuantity(-1);
    if (action === "detail-qty-plus") return adjustDetailQuantity(1);
    if (action === "add-detail-cart") return addDetailProductToCart();
    if (action === "direct-buy") return createDirectOrder();
    if (action === "remove-cart-item") return deleteCartItem(Number(actionTarget.dataset.cartItemId));
    if (action === "cart-qty") return updateCartQuantity(actionTarget);
    if (action === "use-all-points") return useAllPoints();
    if (action === "use-all-direct-points") return useAllDirectPoints();
    if (action === "create-order") return createOrder();
    if (action === "clear-cart") return clearCart();
  }

  const categoryTarget = event.target.closest("[data-category-nav]");
  if (categoryTarget) {
    els.categoryFilter.value = categoryTarget.dataset.categoryNav;
    updateCategoryNav();
    scrollToSection("products");
    return loadProducts(0);
  }

  const productTarget = event.target.closest(".product-card");
  if (productTarget) {
    return openProductDetail(Number(productTarget.dataset.productId));
  }

  const authTab = event.target.closest("[data-auth-tab]");
  if (authTab) {
    return switchAuthTab(authTab.dataset.authTab);
  }
}

function handleInput(event) {
  if (event.target === els.usePointInput) {
    updateCheckoutTotal();
  } else if (event.target.id === "directPointInput") {
    const directPointInput = event.target;
    const qtyInput = document.getElementById("detailQuantity");
    const localProduct = state.currentProduct;
    if (qtyInput && localProduct) {
      let value = Number(directPointInput.value || 0);
      const quantity = Number(qtyInput.value || 1);
      const subtotal = localProduct.price * quantity;
      const maxPoints = Math.min(state.pointBalance, subtotal);
      if (value > maxPoints) directPointInput.value = String(maxPoints);
      if (value < 0) directPointInput.value = "0";
    }
  }
}

function handleChange(event) {
  const input = event.target.closest("[data-cart-qty-input]");
  if (!input) return;
  const cartItemId = Number(input.dataset.cartItemId);
  const quantity = Math.max(1, Number(input.value || 1));
  updateCartItem(cartItemId, quantity);
}

async function api(path, options = {}) {
  const headers = {
    Accept: "application/json",
    ...(options.headers || {}),
  };
  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
  }
  if (options.auth !== false && state.token) {
    headers.Authorization = `Bearer ${state.token}`;
  }

  const response = await fetch(path, {
    method: options.method || "GET",
    headers,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  });

  let payload = null;
  try {
    payload = await response.json();
  } catch (_) {
    payload = null;
  }

  if (!response.ok || payload?.code || payload?.error) {
    if (response.status === 401 && state.refreshToken && !path.includes("/auth/reissue") && !path.includes("/auth/login")) {
      try {
        const reissueData = await api("/api/auth/reissue", {
          method: "POST",
          auth: false,
          body: { refreshToken: state.refreshToken }
        });
        state.token = reissueData.accessToken;
        localStorage.setItem(STORAGE.token, state.token);

        // Retry original request
        headers.Authorization = `Bearer ${state.token}`;
        const retryResponse = await fetch(path, {
          method: options.method || "GET",
          headers,
          body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
        });

        let retryPayload = null;
        try { retryPayload = await retryResponse.json(); } catch (_) { retryPayload = null; }

        if (retryResponse.ok && !retryPayload?.code && !retryPayload?.error) {
          return retryPayload?.data;
        }
      } catch (e) {
        clearSession();
        renderSession();
        throw e;
      }
    }

    const backendMessage = payload?.error?.message || payload?.message;
    const backendCode = payload?.error?.code || payload?.code;
    const error = new Error(backendMessage || `요청 실패 (${response.status})`);
    error.code = backendCode;
    error.status = response.status;
    throw error;
  }

  return payload?.data;
}

async function loadProducts(page = 0) {
  if (state.loading || page < 0) return;
  state.loading = true;
  state.currentPage = page;
  els.productGrid.innerHTML = `<div class="empty-state">상품을 불러오는 중입니다.</div>`;

  const params = new URLSearchParams({
    page: String(page),
    size: "12",
    sort: els.sortFilter.value || "createdAt,desc",
  });

  appendParam(params, "category", els.categoryFilter.value);
  appendParam(params, "status", els.statusFilter.value);

  try {
    const data = await api(`/api/products?${params.toString()}`, { auth: false });
    
    // Normalize backend fields to match frontend expectations
    if (data.content) {
      data.content.forEach((p) => {
        p.productId = p.productId || p.id;
        p.stock = p.stock !== undefined ? p.stock : p.stockQuantity;
        p.category = p.category || p.categoryCode || "TOP";
        p.status = p.status || p.saleStatus || "ON_SALE";
      });
    }

    state.productPage = data.totalElements === 0 ? localProductPage(page) : data;
    state.products = data.content || [];
    if (data.totalElements === 0) {
      state.products = state.productPage.content;
    }
    renderProducts();
  } catch (error) {
    els.productMeta.textContent = "상품 목록을 불러오지 못했습니다.";
    els.productGrid.innerHTML = `<div class="error-state">${escapeHtml(formatError(error))}</div>`;
    showToast(formatError(error), "error");
  } finally {
    state.loading = false;
  }
}

function renderProducts() {
  updateProductHeading();

  const keyword = els.searchInput.value.trim().toLowerCase();
  const products = keyword
    ? state.products.filter((product) => product.name.toLowerCase().includes(keyword))
    : state.products;

  if (!products.length) {
    els.productGrid.innerHTML = `<div class="empty-state">조건에 맞는 상품이 없습니다.</div>`;
  } else {
    els.productGrid.innerHTML = products.map(renderProductCard).join("");
  }

  const page = state.productPage;
  if (page) {
    els.productMeta.textContent = `${page.totalElements}개의 상품`;
    els.pageMeta.textContent = `${page.page} / ${Math.max(page.totalPages, 1)}`;
    els.prevPageBtn.disabled = page.page <= 1;
    els.nextPageBtn.disabled = page.page >= page.totalPages;
  }
}

function renderProductCard(product) {
  const soldOut = product.status === "SOLD_OUT" || product.stock <= 0;
  const categoryLabel = CATEGORY_LABELS[product.category] || product.category;
  return `
    <button class="product-card" type="button" data-product-id="${product.productId}">
      <span class="product-image-frame">
        <img src="${productImage(product.productId)}" alt="${escapeHtml(product.name)}" />
      </span>
      <span class="product-card-body">
        <span class="product-badge${soldOut ? " is-sold-out" : ""}">${soldOut ? "품절" : categoryLabel}</span>
        <h3 class="product-name">${escapeHtml(product.name)}</h3>
        <p class="product-price">${formatCurrency(product.price)}</p>
      </span>
    </button>
  `;
}

async function openProductDetail(productId) {
  els.productDetail.innerHTML = `<div class="empty-state">상품 상세를 불러오는 중입니다.</div>`;
  openDrawer(els.productDrawer);
  if (state.productPage?.localFallback) {
    const localProduct = LOCAL_PRODUCTS.find((product) => product.productId === productId);
    if (localProduct) {
      renderProductDetail(localProduct);
      return;
    }
  }
  try {
    const product = await api(`/api/products/${productId}`, { auth: false });
    
    // Normalize backend fields
    product.productId = product.productId || product.id;
    product.stock = product.stock !== undefined ? product.stock : product.stockQuantity;
    product.category = product.category || product.categoryCode || "TOP";
    product.status = product.status || product.saleStatus || "ON_SALE";

    state.currentProduct = product;
    renderProductDetail(product);
  } catch (error) {
    const localProduct = LOCAL_PRODUCTS.find((product) => product.productId === productId);
    if (localProduct) {
      renderProductDetail(localProduct);
      return;
    }
    els.productDetail.innerHTML = `<div class="error-state">${escapeHtml(formatError(error))}</div>`;
  }
}

function renderProductDetail(product) {
  const soldOut = product.status === "SOLD_OUT" || product.stock <= 0;
  els.productDetail.dataset.productId = product.productId;
  els.productDetail.innerHTML = `
    <div class="detail-layout">
      <div class="detail-gallery">
        <div class="detail-main-image">
          <img src="${productImage(product.productId)}" alt="${escapeHtml(product.name)}" />
        </div>
      </div>
      <div class="detail-info">
        <p class="breadcrumb">컬렉션 / ${CATEGORY_LABELS[product.category] || product.category}</p>
        <h2>${escapeHtml(product.name)}</h2>
        <p class="detail-price">${formatCurrency(product.price)}</p>
        <p class="detail-stock" style="font-size: 13px; color: var(--ink-muted); margin-bottom: 24px;">남은 재고: ${formatNumber(product.stock)}개</p>
        <p class="detail-description">${escapeHtml(product.description || "")}</p>
        <div class="quantity-row">
          <span>수량</span>
          <div class="quantity-control">
            <button type="button" data-action="detail-qty-minus">-</button>
            <input id="detailQuantity" type="number" min="1" max="${Math.max(product.stock, 1)}" value="1" />
            <button type="button" data-action="detail-qty-plus">+</button>
          </div>
        </div>
        <div class="point-row" style="margin-top: 16px; margin-bottom: 24px;">
          <label for="directPointInput" style="font-size: 14px; font-weight: 600;">포인트 사용</label>
          <div class="point-control">
            <input id="directPointInput" type="number" min="0" step="1" value="0" />
            <button class="ghost-button small" type="button" data-action="use-all-direct-points">전액</button>
          </div>
          <span id="directPointHint" style="color: var(--ink-muted); font-size: 12px;">보유 포인트 ${state.pointBalance || 0}P</span>
        </div>
        <div class="detail-actions" style="display: flex; gap: 8px;">
          <button class="ghost-button full" type="button" data-action="add-detail-cart" ${soldOut ? "disabled" : ""}>장바구니 담기</button>
          <button class="primary-button full" type="button" data-action="direct-buy" ${soldOut ? "disabled" : ""}>바로 구매하기</button>
        </div>
      </div>
    </div>
  `;
}

async function addDetailProductToCart() {
  const productId = Number(els.productDetail.dataset.productId);
  const quantityInput = document.getElementById("detailQuantity");
  const quantity = Math.max(1, Number(quantityInput?.value || 1));
  await addCartItem(productId, quantity);
}

function adjustDetailQuantity(delta) {
  const input = document.getElementById("detailQuantity");
  if (!input) return;
  const min = Number(input.min || 1);
  const max = Number(input.max || 99);
  const next = Math.max(min, Math.min(max, Number(input.value || min) + delta));
  input.value = String(next);
}

async function addCartItem(productId, quantity, pendingMessage) {
  if (!requireLogin()) return;
  if (pendingMessage) showToast(pendingMessage);
  try {
    const result = await api("/api/carts/items", {
      method: "POST",
      body: { productId, quantity },
    });
    showToast(`장바구니에 상품이 성공적으로 담겼습니다.`);
    await refreshCart(false);
  } catch (error) {
    showToast(formatError(error), "error");
  }
}

async function refreshCart(showErrors = true) {
  if (!state.token) {
    state.cart = null;
    renderCart();
    return;
  }

  try {
    const response = await api("/api/carts");
    const actualItems = response?.content || response?.items || response || [];
    const items = Array.isArray(actualItems) ? actualItems : [];
    
    const totalQuantity = items.reduce((sum, item) => sum + item.quantity, 0);
    const totalAmount = items.reduce((sum, item) => sum + (item.quantity * item.unitPrice), 0);
    
    state.cart = {
      items,
      totalQuantity,
      totalAmount
    };
    renderCart();
  } catch (error) {
    if (showErrors) showToast(formatError(error), "error");
    if (error.status === 401) {
      clearSession();
      renderSession();
    }
    renderCart();
  }
}

function renderCart() {
  const cart = state.cart;
  const count = cart?.totalQuantity || 0;
  els.cartCount.textContent = String(count);
  els.cartSubtotal.textContent = formatCurrency(cart?.totalAmount || 0);

  if (!state.token) {
    els.cartItems.innerHTML = `<div class="empty-state">로그인이 필요합니다.</div>`;
  } else if (!cart?.items?.length) {
    els.cartItems.innerHTML = `<div class="empty-state">장바구니가 비어 있습니다.</div>`;
  } else {
    els.cartItems.innerHTML = cart.items.map(renderCartItem).join("");
  }

  updateCheckoutTotal();
}

function renderCartItem(item) {
  return `
    <article class="cart-item">
      <img src="${productImage(item.productId)}" alt="${escapeHtml(item.productName)}" />
      <div>
        <h3>${escapeHtml(item.productName)}</h3>
        <p>${formatCurrency(item.unitPrice)}</p>
        <div class="quantity-control">
          <button type="button" data-action="cart-qty" data-cart-item-id="${item.cartItemId}" data-next-quantity="${item.quantity - 1}">-</button>
          <input type="number" min="1" max="${item.stock}" value="${item.quantity}" data-cart-qty-input data-cart-item-id="${item.cartItemId}" />
          <button type="button" data-action="cart-qty" data-cart-item-id="${item.cartItemId}" data-next-quantity="${item.quantity + 1}">+</button>
        </div>
      </div>
      <button class="icon-button" type="button" data-action="remove-cart-item" data-cart-item-id="${item.cartItemId}" aria-label="삭제">
        <span class="material-symbols-rounded">close</span>
      </button>
    </article>
  `;
}

function updateCartQuantity(button) {
  const cartItemId = Number(button.dataset.cartItemId);
  const nextQuantity = Number(button.dataset.nextQuantity);
  if (nextQuantity < 1) {
    return deleteCartItem(cartItemId);
  }
  return updateCartItem(cartItemId, nextQuantity);
}

async function updateCartItem(cartItemId, quantity) {
  try {
    await api(`/api/carts/items/${cartItemId}`, {
      method: "PATCH",
      body: { quantity },
    });
    await refreshCart(false);
  } catch (error) {
    showToast(formatError(error), "error");
    await refreshCart(false);
  }
}

async function deleteCartItem(cartItemId) {
  try {
    await api(`/api/carts/items/${cartItemId}`, { method: "DELETE" });
    await refreshCart(false);
    showToast("상품을 삭제했습니다.");
  } catch (error) {
    showToast(formatError(error), "error");
  }
}

async function clearCart() {
  if (!requireLogin()) return;
  try {
    await api("/api/carts", { method: "DELETE" });
    await refreshCart(false);
    showToast("장바구니를 비웠습니다.");
  } catch (error) {
    showToast(formatError(error), "error");
  }
}

async function createOrder() {
  if (!requireLogin()) return;
  if (!state.cart?.items?.length) {
    showToast("장바구니에 상품이 없습니다.", "error");
    return;
  }
  if (state.loading) return;
  state.loading = true;

  const usedPointAmount = normalizeUsePoint();
  const cartItemIds = state.cart.items.map((item) => item.cartItemId);

  const outOfStockItems = state.cart.items.filter(item => item.quantity > item.stock);
  if (outOfStockItems.length > 0) {
    showToast(`'${outOfStockItems[0].productName}' 상품의 재고가 부족합니다.`, "error");
    return;
  }

  try {
    const orderResult = await api("/api/orders/cart", {
      method: "POST",
      body: { cartItemIds, usePointAmount: usedPointAmount },
    });

    if (orderResult.pgAmount === 0) {
      const confirmed = await confirmPayment(
        orderResult.paymentId,
        orderResult.portonePaymentId
      );
      showToast(`주문 ${confirmed.orderNumber} 결제가 완료되었습니다.`);
      await refreshCart(false);
      await refreshAccount(false);
      closeDrawer(els.cartDrawer);
      return;
    }

    const params = new URLSearchParams({
      orderId: String(orderResult.orderId),
      internalPaymentId: String(orderResult.paymentId),
      portonePaymentId: orderResult.portonePaymentId,
      amount: String(orderResult.pgAmount),
      orderName: buildOrderName(orderResult.items),
      payerName: state.user?.name || "홍길동",
      payerEmail: state.user?.email || "test@example.com",
    });

    sessionStorage.setItem("ec2ShopPendingPayment", JSON.stringify(orderResult));
    window.location.href = `/payment.html?${params.toString()}`;
  } catch (error) {
    showToast(formatError(error), "error");
    await refreshCart(false);
    await refreshAccount(false);
  } finally {
    state.loading = false;
  }
}

async function createDirectOrder() {
  if (!requireLogin()) return;
  if (state.loading) return;
  state.loading = true;
  const productId = Number(els.productDetail.dataset.productId);
  const qtyInput = document.getElementById("detailQuantity");
  const quantity = Number(qtyInput?.value || 1);
  const pointInput = document.getElementById("directPointInput");
  let usePointAmount = Number(pointInput?.value || 0);

  if (!productId || quantity < 1) return;

  const localProduct = state.currentProduct || {};
  if (quantity > (localProduct.stock || 0)) {
    showToast("주문 수량이 현재 재고보다 많습니다.", "error");
    return;
  }

  const maxPoints = Math.min(state.pointBalance, (localProduct.price || 0) * quantity);
  usePointAmount = Math.min(usePointAmount, maxPoints);

  try {
    const orderResult = await api("/api/orders", {
      method: "POST",
      body: { productId, quantity, usePointAmount },
    });

    if (orderResult.pgAmount === 0) {
      const confirmed = await confirmPayment(
        orderResult.paymentId,
        orderResult.portonePaymentId
      );
      showToast(`주문 ${confirmed.orderNumber} 결제가 완료되었습니다.`);
      await refreshAccount(false);
      closeDrawer(els.productDrawer);
      return;
    }

    const localProduct = state.products.find(p => p.productId === productId) || {};
    const params = new URLSearchParams({
      orderId: String(orderResult.orderId),
      internalPaymentId: String(orderResult.paymentId),
      portonePaymentId: orderResult.portonePaymentId,
      amount: String(orderResult.pgAmount),
      orderName: localProduct.name ? `${localProduct.name} ${quantity}개` : `단일 상품 주문`,
      payerName: state.user?.name || "홍길동",
      payerEmail: state.user?.email || "test@example.com",
    });

    sessionStorage.setItem("ec2ShopPendingPayment", JSON.stringify(orderResult));
    window.location.href = `/payment.html?${params.toString()}`;
  } catch (error) {
    showToast(formatError(error), "error");
  } finally {
    state.loading = false;
  }
}

async function confirmPayment(paymentId, portonePaymentId) {
  return api("/api/payments/confirm", {
    method: "POST",
    body: { paymentId, portonePaymentId },
  });
}

async function login() {
  try {
    const data = await api("/api/auth/login", {
      method: "POST",
      auth: false,
      body: {
        email: els.loginEmail.value.trim(),
        password: els.loginPassword.value,
      },
    });
    state.token = data.accessToken;
    state.refreshToken = data.refreshToken;
    state.user = {
      userId: data.id,
      email: data.email,
      name: data.name
    };
    localStorage.setItem(STORAGE.token, state.token);
    localStorage.setItem(STORAGE.refreshToken, state.refreshToken);
    localStorage.setItem(STORAGE.user, JSON.stringify(state.user));
    renderSession();
    closeAuthDialog();
    await refreshCart(false);
    await refreshAccount(false);
    showToast(`${state.user.name}님, 로그인되었습니다.`);
  } catch (error) {
    showToast(formatError(error), "error");
  }
}

async function signup() {
  try {
    const email = els.signupEmail.value.trim();
    const password = els.signupPassword.value;
    const name = els.signupName.value.trim();

    if (!email) {
      showToast("이메일을 입력해주세요.", "error");
      return;
    }
    if (!email.includes("@")) {
      showToast("올바른 이메일 형식이 아닙니다.", "error");
      return;
    }
    if (!password) {
      showToast("비밀번호를 입력해주세요.", "error");
      return;
    }
    if (password.length < 8) {
      showToast("비밀번호는 8자 이상이어야 합니다.", "error");
      return;
    }
    if (!name) {
      showToast("이름을 입력해주세요.", "error");
      return;
    }

    let rawPhone = els.signupPhone.value.trim().replace(/[^0-9]/g, "");
    if (rawPhone.length < 10 || rawPhone.length > 11) {
      showToast("올바른 전화번호를 입력해주세요.", "error");
      return;
    }
    if (rawPhone.length === 11) {
      rawPhone = rawPhone.replace(/(\d{3})(\d{4})(\d{4})/, "$1-$2-$3");
    } else if (rawPhone.length === 10) {
      rawPhone = rawPhone.replace(/(\d{3})(\d{3})(\d{4})/, "$1-$2-$3");
    }

    const request = {
      email,
      password,
      name,
      phoneNumber: rawPhone,
    };
    await api("/api/auth/signup", {
      method: "POST",
      auth: false,
      body: request,
    });
    els.loginEmail.value = request.email;
    els.loginPassword.value = request.password;
    switchAuthTab("login");
    await login();
  } catch (error) {
    showToast(formatError(error), "error");
  }
}

async function logout() {
  if (state.token) {
    try {
      await api("/api/auth/logout", { method: "POST" });
    } catch (_) {
      // Local logout should still clear the browser session.
    }
  }
  clearSession();
  renderSession();
  renderCart();
  refreshAccount(false);
  closeAuthDialog();
  showToast("로그아웃되었습니다.");
}

async function refreshAccount(showErrors = true) {
  if (!state.token) {
    renderLoggedOutAccount();
    return;
  }

  try {
    const balance = await api("/api/points/balance");
    state.pointBalance = Number(balance.pointBalance || 0);
    renderAuthAccount(balance.memberId);
    if (els.pointTransactions) {
      const transactions = await api("/api/points/history?page=0&size=8");
      renderTransactions(transactions.content || []);
    }
    updateCheckoutTotal();
  } catch (error) {
    if (showErrors) showToast(formatError(error), "error");
    if (error.status === 401) {
      clearSession();
      renderSession();
      renderCart();
      return;
    }
    renderSession();
  }
}

function renderTransactions(transactions) {
  if (!els.pointTransactions) return;

  if (!transactions.length) {
    els.pointTransactions.innerHTML = `<div class="empty-state">거래 내역이 없습니다.</div>`;
    return;
  }

  els.pointTransactions.innerHTML = transactions.map((tx) => `
    <div class="transaction-row">
      <strong>${escapeHtml(pointTypeLabel(tx.type))}</strong>
      <span>${formatNumber(tx.amount)} P</span>
      <span>${formatDate(tx.createdAt)}</span>
    </div>
  `).join("");
}

function updateCheckoutTotal() {
  const subtotal = Number(state.cart?.totalAmount || 0);
  const usablePoint = Math.min(state.pointBalance, subtotal);
  const usePointAmount = normalizeUsePoint(true);
  els.pointHint.textContent = `보유 포인트 ${formatNumber(state.pointBalance)}P / 최대 ${formatNumber(usablePoint)}P`;
  els.checkoutTotal.textContent = formatCurrency(Math.max(subtotal - usePointAmount, 0));
}

function normalizeUsePoint(writeBack = true) {
  const subtotal = Number(state.cart?.totalAmount || 0);
  const usablePoint = Math.min(state.pointBalance, subtotal);
  let value = Number(els.usePointInput.value || 0);
  if (!Number.isFinite(value) || value < 0) value = 0;
  value = Math.floor(Math.min(value, usablePoint));
  if (writeBack) els.usePointInput.value = String(value);
  return value;
}

function useAllPoints() {
  const subtotal = Number(state.cart?.totalAmount || 0);
  els.usePointInput.value = String(Math.min(state.pointBalance, subtotal));
  updateCheckoutTotal();
}

function useAllDirectPoints() {
  const directPointInput = document.getElementById("directPointInput");
  const qtyInput = document.getElementById("detailQuantity");
  if (!directPointInput || !qtyInput) return;
  const productId = Number(els.productDetail.dataset.productId);
  const localProduct = state.currentProduct;
  if (!localProduct) return;
  
  const quantity = Number(qtyInput.value || 1);
  const subtotal = localProduct.price * quantity;
  directPointInput.value = String(Math.min(state.pointBalance, subtotal));
}

function renderSession() {
  const loggedIn = Boolean(state.token);
  els.authTabs.classList.toggle("hidden", loggedIn);
  els.loginPane.classList.toggle("hidden", loggedIn);
  els.signupPane.classList.toggle("hidden", loggedIn);
  els.authAccountPane.classList.toggle("hidden", !loggedIn);
  els.logoutButton.classList.toggle("hidden", !loggedIn);

  if (loggedIn) {
    renderAuthAccount();
    return;
  }

  switchAuthTab("login");
  renderLoggedOutAccount();
}

function renderAuthAccount(fallbackUserId) {
  if (!state.token) return;
  const userId = state.user?.userId || fallbackUserId;
  els.authUserName.textContent = state.user?.name || "사용자";
  els.authUserEmail.textContent = state.user?.email || "-";
  els.authUserId.textContent = userId ? `#${userId}` : "-";
  els.authPointBalance.textContent = `${formatNumber(state.pointBalance)} P`;
}

function renderLoggedOutAccount() {
  state.pointBalance = 0;
  if (els.pointTransactions) {
    els.pointTransactions.innerHTML = `<div class="empty-state">로그인 후 거래 내역을 확인할 수 있습니다.</div>`;
  }
  els.authUserName.textContent = "비로그인";
  els.authUserEmail.textContent = "-";
  els.authUserId.textContent = "-";
  els.authPointBalance.textContent = "-";
  updateCheckoutTotal();
}

function clearSession() {
  state.token = null;
  state.refreshToken = null;
  state.user = null;
  state.cart = null;
  state.pointBalance = 0;
  localStorage.removeItem(STORAGE.token);
  localStorage.removeItem(STORAGE.refreshToken);
  localStorage.removeItem(STORAGE.user);
}

function requireLogin() {
  if (state.token) return true;
  openAuthDialog();
  showToast("로그인이 필요합니다.", "error");
  return false;
}

function openAuthDialog() {
  renderSession();
  if (state.token) {
    refreshAccount(false);
  }
  if (els.authDialog.open) return;
  if (els.authDialog.showModal) {
    els.authDialog.showModal();
  } else {
    els.authDialog.setAttribute("open", "");
  }
}

function closeAuthDialog() {
  if (els.authDialog.open) els.authDialog.close();
}

function switchAuthTab(tab) {
  document.querySelectorAll("[data-auth-tab]").forEach((button) => {
    button.classList.toggle("is-active", button.dataset.authTab === tab);
  });
  els.loginPane.classList.toggle("is-active", tab === "login");
  els.signupPane.classList.toggle("is-active", tab === "signup");
}

function openCart() {
  if (!state.token) {
    openAuthDialog();
    return;
  }
  refreshCart(false);
  refreshAccount(false);
  openDrawer(els.cartDrawer);
}

function openDrawer(drawer) {
  closeDrawers();
  drawer.classList.add("is-open");
  drawer.setAttribute("aria-hidden", "false");
  document.body.style.overflow = "hidden";
}

function closeDrawer(drawer) {
  drawer.classList.remove("is-open");
  drawer.setAttribute("aria-hidden", "true");
  if (!document.querySelector(".drawer.is-open")) {
    document.body.style.overflow = "";
  }
}

function closeDrawers() {
  document.querySelectorAll(".drawer.is-open").forEach(closeDrawer);
}

function scrollToSection(id) {
  document.getElementById(id)?.scrollIntoView({ block: "start" });
}

function updateCategoryNav() {
  const selectedCategory = els.categoryFilter.value;
  document.querySelectorAll("[data-category-nav]").forEach((button) => {
    button.classList.toggle("is-active", button.dataset.categoryNav === selectedCategory);
  });
  updateProductHeading();
}

function updateProductHeading() {
  const selectedCategory = els.categoryFilter.value;
  els.productTitle.textContent = selectedCategory
    ? CATEGORY_LABELS[selectedCategory] || selectedCategory
    : "전체 상품";
}

function focusSearch() {
  scrollToSection("products");
  els.searchInput.focus();
}

function appendParam(params, key, value) {
  if (value !== undefined && value !== null && String(value).trim() !== "") {
    params.set(key, String(value).trim());
  }
}

function localProductPage(page = 0) {
  const size = 12;
  const category = els.categoryFilter.value;
  const status = els.statusFilter.value;
  const sort = els.sortFilter.value || "createdAt,desc";

  let content = LOCAL_PRODUCTS.filter((product) => {
    if (category && product.category !== category) return false;
    if (status && product.status !== status) return false;
    return true;
  });

  content = content.sort((left, right) => {
    if (sort === "price,asc") return left.price - right.price;
    if (sort === "price,desc") return right.price - left.price;
    return right.productId - left.productId;
  });

  const totalElements = content.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / size));
  const safePage = Math.min(Math.max(page, 0), totalPages - 1);
  const start = safePage * size;

  return {
    content: content.slice(start, start + size),
    page: safePage,
    size,
    totalElements,
    totalPages,
    hasNext: safePage + 1 < totalPages,
    localFallback: true,
  };
}

function buildOrderName(items = []) {
  if (!items.length) return "Follow Commerce 주문";
  const first = items[0].productName;
  return items.length === 1 ? first : `${first} 외 ${items.length - 1}건`;
}

function productImage(productId) {
  return PRODUCT_IMAGES[productId] || "assets/products/product-sheet.png";
}

function readStoredUser() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE.user));
  } catch (_) {
    return null;
  }
}

function formatCurrency(value) {
  return `${formatNumber(Number(value || 0))}원`;
}

function formatNumber(value) {
  return new Intl.NumberFormat("ko-KR").format(value || 0);
}

function formatDate(value) {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value).slice(0, 10);
  return new Intl.DateTimeFormat("ko-KR", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

function pointTypeLabel(type) {
  return {
    USE: "사용",
    EARN: "적립",
    USE_CANCEL: "사용 취소",
    USE_RESTORE: "사용 복구",
    EARN_CANCEL: "적립 취소",
  }[type] || type;
}

function formatError(error) {
  return error?.message || "요청 처리 중 오류가 발생했습니다.";
}

function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

let toastTimer = null;

function showToast(message, type = "info") {
  clearTimeout(toastTimer);
  els.toast.textContent = message;
  els.toast.classList.toggle("is-error", type === "error");
  els.toast.classList.add("is-visible");
  toastTimer = setTimeout(() => {
    els.toast.classList.remove("is-visible");
  }, 3200);
}
