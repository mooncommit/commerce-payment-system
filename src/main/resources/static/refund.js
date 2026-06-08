document.addEventListener('DOMContentLoaded', () => {
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

    document.getElementById('orderId').value = orderId;

    document.getElementById('btnRefund').addEventListener('click', async () => {
        const reason = document.getElementById('reason').value.trim();
        if (!reason) {
            alert('취소 사유를 입력해주세요.');
            return;
        }

        const btn = document.getElementById('btnRefund');
        btn.disabled = true;
        btn.innerText = '취소 처리 중...';

        try {
            await fetchApi(`/payments/${orderId}/cancel`, {
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
