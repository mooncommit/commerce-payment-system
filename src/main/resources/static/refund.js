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

const REFUND_STATUS_LABELS = {
  PROCESSING: "처리 중",
  COMPLETED: "환불 완료",
  FAILED: "환불 실패",
  PG_RESULT_UNKNOWN: "PG 결과 확인 필요",
};

const state = {
  token: localStorage.getItem(STORAGE.token),
  orderId: Number(new URLSearchParams(window.location.search).get("orderId")),
  detail: null,
  lastRefund: null,
  submitting: false,
};

const els = {};

document.addEventListener("DOMContentLoaded", () => {
  els.refundMeta = document.getElementById("refundMeta");
  els.refundOrderSummary = document.getElementById("refundOrderSummary");
  els.refundFormArea = document.getElementById("refundFormArea");
  els.refreshRefundButton = document.getElementById("refreshRefundButton");
  els.toast = document.getElementById("toast");

  els.refreshRefundButton.addEventListener("click", () => loadOrderDetail());
  document.addEventListener("input", handleInput);
  document.addEventListener("click", handleClick);

  loadOrderDetail();
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

async function loadOrderDetail() {
  state.token = localStorage.getItem(STORAGE.token);

  if (!state.token) {
    renderLoggedOut();
    return;
  }

  if (!Number.isFinite(state.orderId) || state.orderId <= 0) {
    renderPageError("주문 ID가 올바르지 않습니다.");
    return;
  }

  els.refundMeta.textContent = "주문 정보를 불러오는 중입니다.";
  els.refundOrderSummary.innerHTML = `<div class="empty-state">주문 정보를 불러오는 중입니다.</div>`;
  els.refundFormArea.innerHTML = `<div class="empty-state">환불 가능 여부를 확인하는 중입니다.</div>`;

  try {
    const rawDetail = await api(`/api/orders/${state.orderId}`);
    state.detail = {
      ...rawDetail,
      order: {
        orderNumber: rawDetail.orderNumber,
        status: rawDetail.orderStatus,
        totalAmount: rawDetail.totalAmount,
        usedPointAmount: rawDetail.usedPointAmount,
        pgAmount: rawDetail.pgAmount,
        earnedPointAmount: rawDetail.earnedPointAmount,
        createdAt: rawDetail.createdAt,
        paidAt: rawDetail.paidAt,
        canceledAt: rawDetail.canceledAt,
      },
      payment: {
        paymentId: rawDetail.paymentId,
        status: rawDetail.paymentStatus,
      }
    };
    renderRefundPage();
  } catch (error) {
    if (error.status === 401) {
      clearSession();
      renderLoggedOut();
      return;
    }
    renderPageError(formatError(error));
    showToast(formatError(error), "error");
  }
}

function renderLoggedOut() {
  els.refundMeta.textContent = "로그인이 필요합니다.";
  els.refundOrderSummary.innerHTML = `
    <div class="empty-state">
      로그인 후 환불을 요청할 수 있습니다.
      <div class="empty-actions">
        <a class="primary-button link-button" href="/">쇼핑몰에서 로그인하기</a>
      </div>
    </div>
  `;
  els.refundFormArea.innerHTML = `<div class="empty-state">로그인이 필요합니다.</div>`;
}

function renderPageError(message) {
  els.refundMeta.textContent = "환불 요청을 진행할 수 없습니다.";
  els.refundOrderSummary.innerHTML = `<div class="error-state">${escapeHtml(message)}</div>`;
  els.refundFormArea.innerHTML = `
    <div class="empty-state">
      <div class="empty-actions">
        <a class="ghost-button link-button" href="/orders.html">주문 내역으로 돌아가기</a>
      </div>
    </div>
  `;
}

function renderRefundPage() {
  const detail = state.detail;
  const order = detail.order;
  const payment = detail.payment;
  const refundable = isRefundablePaymentStatus(payment.status) && sumRemainingRefundableQuantity(detail.items) > 0;

  els.refundMeta.textContent = `${order.orderNumber} / ${statusLabel(PAYMENT_STATUS_LABELS, payment.status)}`;
  els.refundOrderSummary.innerHTML = renderOrderSummary(detail);
  els.refundFormArea.innerHTML = refundable
    ? renderRefundForm(detail)
    : renderRefundUnavailable(detail);

  updateSelectedRefundSummary();
  if (state.lastRefund) {
    renderRefundResult(state.lastRefund);
  }
}

function renderOrderSummary(detail) {
  const order = detail.order;
  const payment = detail.payment;

  return `
    <div class="refund-summary-head">
      <span class="label">주문번호</span>
      <h2>${escapeHtml(order.orderNumber)}</h2>
      <p>${formatDate(order.orderedAt || detail.createdAt)}</p>
      <div class="order-status-group">
        <span class="status-pill">${escapeHtml(statusLabel(ORDER_STATUS_LABELS, order.status || detail.orderStatus))}</span>
        <span class="status-pill payment">${escapeHtml(statusLabel(PAYMENT_STATUS_LABELS, payment.status || detail.paymentStatus))}</span>
      </div>
    </div>
    <div class="order-summary-grid compact">
      <div>
        <span class="label">주문 금액</span>
        <strong>${formatCurrency(order.totalAmount || detail.totalAmount)}</strong>
      </div>
      <div>
        <span class="label">사용 포인트</span>
        <strong>${formatNumber(order.usedPointAmount || detail.usedPointAmount)} P</strong>
      </div>
      <div>
        <span class="label">PG 결제</span>
        <strong>${formatCurrency(detail.pgAmount)}</strong>
      </div>
      <div>
        <span class="label">적립 포인트</span>
        <strong>${formatNumber(detail.earnedPointAmount)} P</strong>
      </div>
    </div>
    <div class="refund-order-items">
      ${(detail.items || []).map(renderOrderItemSummary).join("")}
    </div>
  `;
}

function renderOrderItemSummary(item) {
  const remainingQuantity = remainingRefundableQuantity(item);

  return `
    <div class="order-item-preview">
      <img src="${productImage(item.productId)}" alt="${escapeHtml(item.productName)}" />
      <div>
        <strong>${escapeHtml(item.productName)}</strong>
        <span>${formatCurrency(item.unitPrice)} / 주문 ${formatNumber(item.quantity)}개</span>
        <span>환불 완료 ${formatNumber(item.refundedQuantity)}개 / 환불 가능 ${formatNumber(remainingQuantity)}개</span>
      </div>
    </div>
  `;
}

function renderRefundForm(detail) {
  return `
    <form class="refund-form" id="refundForm">
      <label class="form-field">
        환불 사유
        <textarea id="refundReason" maxlength="255" rows="4" placeholder="환불 사유를 입력하세요."></textarea>
      </label>
      <div class="refund-actions">
        <button class="ghost-button" id="fullRefundButton" type="button" style="width:100%;">환불 요청</button>
      </div>
      <div id="refundResult" class="refund-result-slot"></div>
    </form>
  `;
}

function renderRefundItemInput(item) {
  return "";
}

function renderRefundUnavailable(detail) {
  const remainingQuantity = sumRemainingRefundableQuantity(detail.items);
  const paymentStatus = detail.payment?.status;
  const message = remainingQuantity <= 0
    ? "이미 환불 가능한 수량이 없습니다."
    : `${statusLabel(PAYMENT_STATUS_LABELS, paymentStatus)} 상태에서는 환불을 요청할 수 없습니다.`;

  return `
    <div class="empty-state">
      ${escapeHtml(message)}
      <div class="empty-actions">
        <a class="ghost-button link-button" href="/orders.html">주문 내역으로 돌아가기</a>
      </div>
    </div>
  `;
}

function handleInput(event) {
  // Partial refund UI removed
}

function handleClick(event) {

  if (event.target.id === "fullRefundButton") {
    requestFullRefund();
  }
}

function adjustRefundQuantity(button) {
  const input = document.querySelector(`[data-refund-quantity][data-order-item-id="${button.dataset.orderItemId}"]`);
  if (!input) return;

  const delta = Number(button.dataset.delta || 0);
  input.value = String(Number(input.value || 0) + delta);
  normalizeQuantityInput(input);
  updateSelectedRefundSummary();
}

function normalizeQuantityInput(input) {
  const min = Number(input.min || 0);
  const max = Number(input.max || 0);
  let value = Number(input.value || 0);
  if (!Number.isFinite(value)) value = min;
  value = Math.max(min, Math.min(max, Math.floor(value)));
  input.value = String(value);
}

function updateSelectedRefundSummary() {
  const target = document.getElementById("selectedRefundAmount");
  if (!target) return;

  const amount = collectPartialRefundItems().reduce((sum, item) => {
    const input = document.querySelector(`[data-refund-quantity][data-order-item-id="${item.orderItemId}"]`);
    return sum + Number(input?.dataset.unitPrice || 0) * item.quantity;
  }, 0);

  target.textContent = formatCurrency(amount);
}

async function requestPartialRefund() {
  // Partial refund UI removed
}

async function requestFullRefund() {
  if (state.submitting) return;

  const reason = readReason();
  if (!reason) {
    showToast("환불 사유를 입력하세요.", "error");
    return;
  }

  await submitRefund(() => api(`/api/payments/${state.detail.payment.paymentId}/refunds`, {
    method: "POST",
    headers: {
      "Idempotency-Key": createIdempotencyKey("refund-full"),
    },
    body: { reason },
  }));
}

async function submitRefund(requestFn) {
  state.submitting = true;
  setSubmitDisabled(true);

  try {
    state.lastRefund = await requestFn();
    renderRefundResult(state.lastRefund);
    showToast("환불 요청이 접수되었습니다.");
    await loadOrderDetail();
  } catch (error) {
    renderRefundError(error);
    showToast(formatError(error), "error");
  } finally {
    state.submitting = false;
    setSubmitDisabled(false);
  }
}

function setSubmitDisabled(disabled) {
  document.getElementById("partialRefundButton")?.toggleAttribute("disabled", disabled);
  document.getElementById("fullRefundButton")?.toggleAttribute("disabled", disabled);
}

function readReason() {
  return document.getElementById("refundReason")?.value.trim() || "";
}

function collectPartialRefundItems() {
  return Array.from(document.querySelectorAll("[data-refund-quantity]"))
    .map((input) => ({
      orderItemId: Number(input.dataset.orderItemId),
      quantity: Number(input.value || 0),
    }))
    .filter((item) => item.orderItemId && item.quantity > 0);
}

function renderRefundResult(refund) {
  const target = document.getElementById("refundResult");
  if (!target) return;

  target.innerHTML = `
    <div class="refund-result is-success">
      <div>
        <span class="label">환불 요청 결과</span>
        <strong>${escapeHtml(statusLabel(REFUND_STATUS_LABELS, refund.refundStatus))}</strong>
      </div>
      <div class="order-summary-grid compact">
        <div>
          <span class="label">실제 환불액</span>
          <strong>${formatCurrency(refund.actualRefundAmount)}</strong>
        </div>
        <div>
          <span class="label">포인트 반환</span>
          <strong>${formatNumber(refund.pointRefundAmount)} P</strong>
        </div>
        <div>
          <span class="label">PG 환불</span>
          <strong>${formatCurrency(refund.pgRefundAmount)}</strong>
        </div>
        <div>
          <span class="label">환불 ID</span>
          <strong>#${escapeHtml(refund.refundId)}</strong>
        </div>
      </div>
      <p>PG 환불은 서버 outbox에서 처리되며, 처리 결과는 주문 내역을 새로고침해 확인할 수 있습니다.</p>
    </div>
  `;
}

function renderRefundError(error) {
  const target = document.getElementById("refundResult");
  if (!target) return;

  target.innerHTML = `
    <div class="refund-result is-error">
      <strong>환불 요청 실패</strong>
      <p>${escapeHtml(formatError(error))}</p>
    </div>
  `;
}

function isRefundablePaymentStatus(status) {
  return status === "COMPLETED" || status === "PARTIAL_REFUNDED";
}

function sumRemainingRefundableQuantity(items = []) {
  return items.reduce((sum, item) => sum + remainingRefundableQuantity(item), 0);
}

function remainingRefundableQuantity(item) {
  return Math.max(0, Number(item.quantity || 0) - Number(item.refundedQuantity || 0));
}

function statusLabel(labels, status) {
  return labels[status] || status || "-";
}

function createIdempotencyKey(prefix) {
  if (window.crypto?.randomUUID) {
    return `${prefix}-${window.crypto.randomUUID()}`;
  }
  return `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2)}`;
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
