async function loadCart() {
    if (!App.token) {
        App.showToast('로그인이 필요합니다.', 'error');
        window.location.href = '/login.html';
        return;
    }

    try {
        const res = await App.fetch('/carts');
        if (res && res.data) {
            renderCart(res.data);
        }
    } catch (error) {
        document.getElementById('cart-items').innerHTML = `
            <div class="empty-state text-danger">장바구니를 불러오는 데 실패했습니다.</div>
        `;
    }
}

function renderCart(cartData) {
    const container = document.getElementById('cart-items');
    
    if (!cartData.items || cartData.items.length === 0) {
        container.innerHTML = `
            <div class="empty-state card" style="padding: 3rem;">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="9" cy="21" r="1"></circle><circle cx="20" cy="21" r="1"></circle><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path></svg>
                <h3>장바구니가 비어있습니다.</h3>
                <a href="/index.html" class="btn btn-primary mt-4">쇼핑 계속하기</a>
            </div>
        `;
        document.getElementById('cart-total').innerText = '0원';
        return;
    }

    container.innerHTML = cartData.items.map(item => {
        let imageUrl = '/images/prod_hoodie.png';
        if (item.productName.includes('키보드')) imageUrl = '/images/prod_keyboard.png';
        if (item.productName.includes('마우스')) imageUrl = '/images/banner_digital.png';
        if (item.productName.includes('자켓')) imageUrl = '/images/prod_jacket.png';
        if (item.productName.includes('사자')) imageUrl = '/images/lion_keyring.png';
        else if (item.productName.includes('키링')) imageUrl = '/images/keyring.png';
        if (item.productName.includes('헤드폰')) imageUrl = '/images/prod_headphone.png';

        return `
            <div class="card p-4 flex gap-4 items-center">
                <img src="${imageUrl}" style="width: 80px; height: 80px; object-fit: cover; border-radius: var(--radius-md); border: 1px solid var(--border-color);">
                <div style="flex: 1;">
                    <a href="/product.html?id=${item.productId}" style="color: var(--text-main); font-weight: 600;">${item.productName}</a>
                    <div class="text-muted" style="font-size: 0.875rem; margin-top: 4px;">수량: ${item.quantity}개</div>
                    <div style="font-weight: 700; margin-top: 8px; color: var(--primary);">${App.formatCurrency(item.totalPrice)}</div>
                </div>
                <div>
                    <button onclick="removeCartItem(${item.cartItemId})" class="btn btn-secondary" style="padding: 0.5rem;">삭제</button>
                </div>
            </div>
        `;
    }).join('');

    document.getElementById('cart-total').innerText = App.formatCurrency(cartData.totalAmount);
}

async function removeCartItem(cartItemId) {
    if (!confirm('장바구니에서 삭제하시겠습니까?')) return;
    
    try {
        await App.fetch(`/carts/${cartItemId}`, { method: 'DELETE' });
        App.showToast('삭제되었습니다.');
        loadCart();
    } catch (error) {
        App.showToast('삭제 실패: ' + error.message, 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => loadCart());
