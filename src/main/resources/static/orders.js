document.addEventListener('DOMContentLoaded', async () => {
    if (!localStorage.getItem('token')) {
        window.location.href = '/login.html';
        return;
    }

    try {
        const response = await fetchApi('/orders?page=1&size=50');
        const orders = response.content || [];

        document.getElementById('loading').style.display = 'none';

        if (orders.length === 0) {
            document.getElementById('noOrders').style.display = 'block';
            return;
        }

        const container = document.getElementById('ordersContainer');
        container.style.display = 'flex';

        container.innerHTML = orders.map(order => {
            const date = new Date(order.createdAt).toLocaleDateString();
            const statusColor = order.orderStatus === 'PAYMENT_COMPLETED' ? '#1e8e3e' : 
                                order.orderStatus === 'PENDING' ? '#5f6368' : '#d93025';
            const statusBg = order.orderStatus === 'PAYMENT_COMPLETED' ? '#e6f4ea' : 
                             order.orderStatus === 'PENDING' ? '#f1f3f4' : '#fce8e6';
            const statusText = order.orderStatus === 'PAYMENT_COMPLETED' ? '결제 완료' :
                               order.orderStatus === 'PENDING' ? '결제 대기' : 
                               order.orderStatus === 'CANCELLED' ? '주문 취소' : order.orderStatus;

            return `
                <div class="card" style="padding: 24px;">
                    <div style="display: flex; justify-content: space-between; border-bottom: 1px solid var(--color-border); padding-bottom: 16px; margin-bottom: 16px;">
                        <div>
                            <span style="font-size: 14px; color: var(--color-text-muted); display: block; margin-bottom: 4px;">${date} 주문</span>
                            <span style="font-size: 16px; font-weight: 600;">주문번호: ${order.orderNumber}</span>
                        </div>
                        <div>
                            <span style="padding: 4px 8px; border-radius: 4px; font-size: 13px; font-weight: 600; background-color: ${statusBg}; color: ${statusColor};">
                                ${statusText}
                            </span>
                        </div>
                    </div>
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <div style="flex: 1;">
                            <div style="font-size: 15px; color: var(--color-text-main); margin-bottom: 8px;">
                                총 결제 금액: <span style="font-weight: 700;">${order.totalAmount.toLocaleString()}원</span>
                            </div>
                            <div style="font-size: 14px; color: var(--color-text-muted);">
                                (포인트 사용: ${order.usedPointAmount.toLocaleString()}P / 실 결제액: ${order.pgAmount.toLocaleString()}원)
                            </div>
                        </div>
                        ${order.orderStatus === 'PAYMENT_COMPLETED' && order.pgAmount > 0 ? `
                            <button class="btn-outline" style="padding: 8px 16px; font-size: 14px;" onclick="location.href='/refund.html?orderId=${order.orderId}'">
                                결제 취소
                            </button>
                        ` : ''}
                    </div>
                </div>
            `;
        }).join('');
    } catch (e) {
        console.warn('Failed to load orders', e);
        document.getElementById('loading').innerText = '주문 내역을 불러오는데 실패했습니다.';
    }
});
