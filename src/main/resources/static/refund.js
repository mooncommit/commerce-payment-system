document.addEventListener('DOMContentLoaded', async () => {
    if (!localStorage.getItem('token')) {
        window.location.href = '/login.html';
        return;
    }

    const orderId = getUrlParameter('orderId');
    if (!orderId) {
        alert('잘못된 접근입니다.');
        window.location.href = '/orders.html';
        return;
    }

    let paymentId = null;

    try {
        // 주문 상세 내역을 조회하여 paymentId를 획득합니다.
        const orderDetail = await fetchApi(`/orders/${orderId}`);
        if (orderDetail) {
            document.getElementById('orderId').value = orderDetail.orderNumber || orderId;
            paymentId = orderDetail.paymentId;
        }
    } catch (e) {
        alert('주문 정보를 불러오지 못했습니다.');
        window.location.href = '/orders.html';
        return;
    }

    document.getElementById('btnRefund').addEventListener('click', async () => {
        if (!paymentId) {
            alert('결제 정보가 존재하지 않아 취소할 수 없습니다.');
            return;
        }

        const reason = document.getElementById('reason').value.trim();
        if (!reason) {
            alert('취소 사유를 입력해주세요.');
            return;
        }

        const btn = document.getElementById('btnRefund');
        btn.disabled = true;
        btn.innerText = '취소 처리 중...';

        try {
            await fetchApi(`/payments/${paymentId}/refunds`, {
                method: 'POST',
                body: JSON.stringify({ reason })
            });

            alert('결제가 성공적으로 취소되었습니다.');
            window.location.href = '/orders.html';
        } catch (err) {
            alert(`결제 취소 실패: ${err.message}`);
            btn.disabled = false;
            btn.innerText = '결제 취소 요청';
        }
    });
});
