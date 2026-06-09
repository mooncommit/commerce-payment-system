const STORAGE = {
  token: "ec2ShopAccessToken",
  user: "ec2ShopUser",
};

const PRODUCT_IMAGES = {
  1: "assets/products/images/clean_code_book.png",
  2: "assets/products/images/wireless_mouse.png",
  3: "assets/products/images/lion_keyring.png",
  4: "assets/products/images/leather_keyring.png",
  5: "assets/products/images/bluetooth_headphones.png",
  6: "assets/products/images/casual_hoodie.png",
  7: "assets/products/images/windbreaker_jacket.png",
  8: "assets/products/images/mechanical_keyboard.png",
  9: "assets/products/images/prod_headphone.png",
  10: "assets/products/images/prod_hoodie.png",
  11: "assets/products/images/prod_jacket.png",
  12: "assets/products/images/prod_keyboard.png",
  13: "assets/products/images/keyring.png",
  14: "assets/products/images/pig_keyring.png",
  15: "assets/products/images/lg_monitor.png",
  16: "assets/products/images/book_java.png",
  17: "assets/products/images/book_spring.png",
  18: "assets/products/images/book_algorithm.png",
};

const ORDER_STATUS_LABELS = {
  PAYMENT_PENDING: "결제 대기",
  COMPLETED: "주문 완료",
  PARTIAL_CANCELED: "부분 취소",
  CANCELED: "취소 완료",
};

const PAYMENT_STATUS_LABELS = {
  PENDING: "결제 대기",
  COMPLETED: "결제 완료",
  FAILED: "결제 실패",
  PARTIAL_REFUNDED: "부분 환불",
  FULL_REFUNDED: "전체 환불",
};

const state = {
  token: localStorage.getItem(STORAGE.token),
};

const els = {};

document.addEventListener("DOMContentLoaded", () => {
  els.ordersList = document.getElementById("ordersList");
  els.ordersMeta = document.getElementById("ordersMeta");
  els.refreshOrdersButton = document.getElementById("refreshOrdersButton");
  els.toast = document.getElementById("toast");
  els.pagination = document.getElementById("pagination");

  els.refreshOrdersButton.addEventListener("click", () => loadOrders());
  loadOrders();
});

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

  if (!response.ok || payload?.code) {
    const error = new Error(payload?.message || `요청 실패 (${response.status})`);
    error.code = payload?.code;
    error.status = response.status;
    throw error;
  }

  return payload?.data;
}

async function loadOrders(page = 1) {
  state.token = localStorage.getItem(STORAGE.token);

  if (!state.token) {
    renderLoggedOut();
    return;
  }

  els.ordersMeta.textContent = "주문 정보를 불러오는 중입니다.";
  els.ordersList.innerHTML = `<div class="empty-state">주문 정보를 불러오는 중입니다.</div>`;

  try {
    const params = new URLSearchParams({ page: String(page), size: "10" });
    const data = await api(`/api/orders?${params.toString()}`);
    const orders = data?.content || [];

    if (!orders.length) {
      els.ordersMeta.textContent = "주문 내역이 없습니다.";
      els.ordersList.innerHTML = `<div class="empty-state">아직 주문한 상품이 없습니다.</div>`;
      if (els.pagination) els.pagination.innerHTML = "";
      return;
    }

    const detailResults = await Promise.allSettled(
      orders.map((order) => api(`/api/orders/${order.orderId || order.id}`))
    );

    els.ordersMeta.textContent = `전체 ${data.totalElements || orders.length}개의 주문`;
    els.ordersList.innerHTML = detailResults
      .map((result, index) => {
        if (result.status === "fulfilled") {
          return renderOrderCard(result.value);
        }
        return renderOrderErrorCard(orders[index], result.reason);
      })
      .join("");

    renderOrdersPagination(data);
  } catch (error) {
    if (error.status === 401) {
      clearSession();
      renderLoggedOut();
      return;
    }
    els.ordersMeta.textContent = "주문 내역을 불러오지 못했습니다.";
    els.ordersList.innerHTML = `<div class="error-state">${escapeHtml(formatError(error))}</div>`;
    showToast(formatError(error), "error");
  }
}

function renderLoggedOut() {
  els.ordersMeta.textContent = "로그인이 필요합니다.";
  els.ordersList.innerHTML = `
    <div class="empty-state">
      로그인 후 주문 내역과 환불 가능 상태를 확인할 수 있습니다.
      <div class="empty-actions">
        <a class="primary-button link-button" href="/">쇼핑몰에서 로그인하기</a>
      </div>
    </div>
  `;
}

function renderOrderCard(detail) {
  const order = detail.order || detail;
  const payment = detail.payment || detail;
  const orderStatus = order.orderStatus || order.status;
  const paymentStatus = payment.paymentStatus || payment.status;
  const orderedAt = order.createdAt || order.orderedAt;
  const rewardPointAmount = payment.earnedPointAmount || payment.rewardPointAmount;
  
  const refundable = canRequestRefund(detail);
  const remainingQuantity = sumRemainingRefundableQuantity(detail.items);
  const items = detail.items || [];

  return `
    <article class="order-card">
      <div class="order-card-head">
        <div>
          <span class="label">주문번호</span>
          <h2>${escapeHtml(order.orderNumber)}</h2>
          <p>${formatDate(orderedAt)}</p>
        </div>
        <div class="order-status-group">
          <span class="status-pill">${escapeHtml(statusLabel(ORDER_STATUS_LABELS, orderStatus))}</span>
          <span class="status-pill payment">${escapeHtml(statusLabel(PAYMENT_STATUS_LABELS, paymentStatus))}</span>
        </div>
      </div>
      <div class="order-items-preview">
        ${items.map(renderOrderItemPreview).join("")}
      </div>
      <div class="order-summary-grid">
        <div>
          <span class="label">주문 금액</span>
          <strong>${formatCurrency(order.totalAmount)}</strong>
        </div>
        <div>
          <span class="label">사용 포인트</span>
          <strong>${formatNumber(order.usedPointAmount)} P</strong>
        </div>
        <div>
          <span class="label">PG 결제</span>
          <strong>${formatCurrency(order.pgAmount || payment.pgAmount)}</strong>
        </div>
        <div>
          <span class="label">적립 포인트</span>
          <strong>${formatNumber(rewardPointAmount)} P</strong>
        </div>
        <div>
          <span class="label">환불 가능 수량</span>
          <strong>${formatNumber(remainingQuantity)}개</strong>
        </div>
      </div>
      <div class="order-actions">
        <a class="ghost-button link-button" href="/refund.html?orderId=${encodeURIComponent(order.orderId)}">상세 보기</a>
        ${
          refundable
            ? `<a class="primary-button link-button" href="/refund.html?orderId=${encodeURIComponent(order.orderId)}">환불하기</a>`
            : `<button class="primary-button" type="button" disabled>${escapeHtml(refundUnavailableLabel(paymentStatus, remainingQuantity))}</button>`
        }
      </div>
    </article>
  `;
}

function renderOrderErrorCard(order, error) {
  const orderNumber = order.orderNumber;
  const orderedAt = order.createdAt || order.orderedAt;
  return `
    <article class="order-card">
      <div class="order-card-head">
        <div>
          <span class="label">주문번호</span>
          <h2>${escapeHtml(orderNumber)}</h2>
          <p>${formatDate(orderedAt)}</p>
        </div>
        <span class="status-pill is-error">상세 조회 실패</span>
      </div>
      <div class="error-state">${escapeHtml(formatError(error))}</div>
    </article>
  `;
}

function renderOrderItemPreview(item) {
  const remainingQuantity = Math.max(0, Number(item.quantity || 0) - Number(item.refundedQuantity || 0));

  return `
    <div class="order-item-preview">
      <img src="${productImage(item.productId)}" alt="${escapeHtml(item.productName)}" />
      <div>
        <strong>${escapeHtml(item.productName)}</strong>
        <span>${formatCurrency(item.unitPrice)} / 주문 ${formatNumber(item.quantity)}개</span>
        <span>환불 완료 ${formatNumber(item.refundedQuantity)}개 / 남은 수량 ${formatNumber(remainingQuantity)}개</span>
      </div>
    </div>
  `;
}

function canRequestRefund(detail) {
  const paymentStatus = detail.payment?.status || detail.paymentStatus;
  return isRefundablePaymentStatus(paymentStatus)
    && sumRemainingRefundableQuantity(detail.items) > 0;
}

function isRefundablePaymentStatus(status) {
  return status === "COMPLETED" || status === "PARTIAL_REFUNDED";
}

function sumRemainingRefundableQuantity(items = []) {
  return items.reduce((sum, item) => {
    return sum + Math.max(0, Number(item.quantity || 0) - Number(item.refundedQuantity || 0));
  }, 0);
}

function refundUnavailableLabel(paymentStatus, remainingQuantity) {
  if (remainingQuantity <= 0) return "환불 완료";
  if (paymentStatus === "PENDING") return "결제 대기";
  if (paymentStatus === "FAILED") return "결제 실패";
  if (paymentStatus === "FULL_REFUNDED") return "환불 완료";
  return "환불 불가";
}

function statusLabel(labels, status) {
  return labels[status] || status || "-";
}

function renderOrdersPagination(pageData) {
  if (!els.pagination) return;
  if (!pageData || pageData.totalPages <= 1) {
    els.pagination.innerHTML = "";
    return;
  }

  const prevDisabled = pageData.page <= 1 ? "disabled" : "";
  const nextDisabled = pageData.page >= pageData.totalPages ? "disabled" : "";

  els.pagination.innerHTML = `
    <div style="display: flex; gap: 8px; align-items: center; justify-content: center; margin-top: 24px;">
      <button class="ghost-button" type="button" ${prevDisabled} onclick="loadOrders(${pageData.page - 1})">이전</button>
      <span style="font-size: 14px; font-weight: 500;">${pageData.page} / ${pageData.totalPages}</span>
      <button class="ghost-button" type="button" ${nextDisabled} onclick="loadOrders(${pageData.page + 1})">다음</button>
    </div>
  `;
}


function productImage(productId) {
  return PRODUCT_IMAGES[productId] || "assets/products/product-sheet.png";
}

function clearSession() {
  localStorage.removeItem(STORAGE.token);
  localStorage.removeItem(STORAGE.user);
  state.token = null;
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
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

function formatError(error) {
  return error?.code ? `${error.code}: ${error.message}` : error?.message || "요청 처리 중 오류가 발생했습니다.";
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
