let currentPage = 0;

async function loadOrders(page = 1) {
    if (!App.token) {
        App.showToast('로그인이 필요합니다.', 'error');
        window.location.href = '/login.html';
        return;
    }

    try {
        const res = await App.fetch(`/orders?page=${page}&size=10`);
        if (res && res.data) {
            renderOrders(res.data.content);
            renderPagination(res.data);
            currentPage = page;
        }
    } catch (error) {
        document.getElementById('orders-list').innerHTML = `
            <div class="empty-state text-danger">주문 내역을 불러오는 데 실패했습니다.</div>
        `;
    }
}

function getStatusBadge(status) {
    switch(status) {
        case 'PENDING_PAYMENT': return '<span class="badge badge-warning">결제 대기</span>';
        case 'COMPLETED': return '<span class="badge badge-success">결제 완료</span>';
        case 'CANCELED': return '<span class="badge badge-danger">결제 취소</span>';
        case 'REFUND_COMPLETED': return '<span class="badge badge-danger">환불 완료</span>';
        default: return `<span class="badge">${status}</span>`;
    }
}

function renderOrders(orders) {
    const container = document.getElementById('orders-list');
    
    if (!orders || orders.length === 0) {
        container.innerHTML = `
            <div class="empty-state card">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path><polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline><line x1="12" y1="22.08" x2="12" y2="12"></line></svg>
                <h3>주문 내역이 없습니다.</h3>
                <a href="/index.html" class="btn btn-primary mt-4">상품 구경가기</a>
            </div>
        `;
        return;
    }

    container.innerHTML = orders.map(order => {
        const isCancelable = order.orderStatus === 'COMPLETED';
        const isPending = order.orderStatus === 'PENDING_PAYMENT';
        
        let actionBtn = '';
        if (isCancelable) {
            actionBtn = `<button onclick="refundOrder(${order.paymentId})" class="btn btn-danger">결제 취소(환불)</button>`;
        } else if (isPending) {
            actionBtn = `
                <button onclick="window.location.href='/payment.html?orderId=${order.orderId}'" class="btn btn-primary">결제 계속하기</button>
                <button onclick="cancelPendingOrder(${order.orderId})" class="btn btn-secondary ml-2">주문 취소</button>
            `;
        }

        return `
            <div class="card p-4">
                <div class="flex justify-between items-center mb-4">
                    <div>
                        <span class="text-muted" style="font-size: 0.875rem;">주문번호: ${order.orderNumber}</span>
                        <div class="mt-1">${getStatusBadge(order.orderStatus)}</div>
                    </div>
                    <div class="text-right">
                        <div style="font-weight: 600; font-size: 1.125rem;">${App.formatCurrency(order.totalAmount)}</div>
                        <div class="text-muted" style="font-size: 0.875rem;">${App.formatDate(order.createdAt)}</div>
                    </div>
                </div>
                <div class="flex justify-between items-center" style="border-top: 1px solid var(--border-color); padding-top: var(--spacing-md);">
                    <div style="font-weight: 500;">
                        ${order.items && order.items.length > 0 ? order.items[0].productName + (order.items.length > 1 ? ` 외 ${order.items.length - 1}건` : '') : '상품 정보 없음'}
                    </div>
                    <div class="flex gap-4">
                        ${actionBtn}
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function renderPagination(pageData) {
    const container = document.getElementById('pagination');
    if (pageData.totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    let html = '';
    if (!pageData.first) {
        html += `<button class="btn btn-secondary" onclick="loadOrders(${pageData.number})">이전</button>`;
    }
    html += `<span>${pageData.number + 1} / ${pageData.totalPages}</span>`;
    if (!pageData.last) {
        html += `<button class="btn btn-secondary" onclick="loadOrders(${pageData.number + 2})">다음</button>`;
    }
    container.innerHTML = html;
}

async function refundOrder(paymentId) {
    if (!paymentId) return;
    const reason = prompt('환불 사유를 입력해주세요.');
    if (!reason) return;

    try {
        App.showToast('환불을 요청하고 있습니다...', 'success');
        const res = await App.fetch(`/payments/${paymentId}/refunds`, {
            method: 'POST',
            body: JSON.stringify({ reason })
        });
        
        App.showToast('환불 처리가 완료되었습니다.', 'success');
        loadOrders(currentPage); // 새로고침
    } catch (error) {
        App.showToast('환불 실패: ' + error.message, 'error');
    }
}

async function cancelPendingOrder(orderId) {
    if (!confirm('이 주문을 취소하시겠습니까?')) return;
    try {
        await App.fetch(`/orders/${orderId}/status`, { method: 'PATCH' });
        App.showToast('주문이 취소되었습니다.');
        loadOrders(currentPage);
    } catch (error) {
        App.showToast('취소 실패: ' + error.message, 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => loadOrders(1));
