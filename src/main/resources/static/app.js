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

// 1. 공통 API Fetch 로직
async function fetchApi(url, options = {}) {
    let token = localStorage.getItem('token');
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    let response = await fetch(`/api${url}`, {
        ...options,
        headers,
    });

    // Token expiration handling
    if (response.status === 401 && !url.includes('/auth/reissue') && !url.includes('/auth/login')) {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
            try {
                const reissueRes = await fetch('/api/auth/reissue', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ refreshToken })
                });

                if (reissueRes.ok) {
                    const reissueData = await reissueRes.json();
                    const newAccessToken = reissueData.data.accessToken;
                    localStorage.setItem('token', newAccessToken);

                    headers['Authorization'] = `Bearer ${newAccessToken}`;
                    response = await fetch(`/api${url}`, {
                        ...options,
                        headers,
                    });
                } else {
                    handleLogout();
                }
            } catch (e) {
                handleLogout();
            }
        } else {
            handleLogout();
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

    updateCartBadge();
}

async function updateCartBadge() {
    const token = localStorage.getItem('token');
    if (!token) return;
    try {
        const cart = await fetchApi('/carts');
        const count = cart.items ? cart.items.reduce((sum, item) => sum + item.quantity, 0) : 0;
        const badge = document.getElementById('cartBadge');
        if (count > 0) {
            badge.innerText = count;
            badge.style.display = 'flex';
        } else {
            badge.style.display = 'none';
        }
    } catch (e) {
        console.warn('Failed to load cart count', e);
    }
}

// 4. 전역 초기화
document.addEventListener('DOMContentLoaded', () => {
    // 모든 페이지에 네비게이션 바 추가 (로그인 페이지 제외)
    if (!window.location.pathname.includes('/login.html')) {
        renderNavbar();
    }
});
