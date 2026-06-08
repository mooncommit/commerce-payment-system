// app.js

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

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.error?.message || 'API 요청 실패');
    }

    return data.data;
}

// 2. 인증 및 유틸 로직
function handleLogout() {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    window.location.href = '/login.html';
}

function getUrlParameter(name) {
    name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
    var regex = new RegExp('[\\?&]' + name + '=([^&#]*)');
    var results = regex.exec(location.search);
    return results === null ? '' : decodeURIComponent(results[1].replace(/\+/g, ' '));
}

// 3. 네비게이션 헤더 렌더링
function renderNavbar() {
    const token = localStorage.getItem('token');
    const navHtml = `
        <nav class="nav-bar">
            <div class="nav-links">
                <a href="/index.html" style="font-weight: 700; font-family: var(--font-serif); font-size: 20px;">
                    Follow <span style="color: var(--color-accent);">Commerce</span>
                </a>
            </div>
            <div class="nav-links" style="gap: 20px; align-items: center;">
                <form id="searchForm" style="display: flex; align-items: center;">
                    <input type="text" id="searchInput" placeholder="검색어를 입력하세요..." style="padding: 4px 8px; border-radius: 4px; border: 1px solid #ccc; outline: none;">
                </form>
                <a href="/orders.html" class="icon-btn" title="Orders">📋</a>
                ${token 
                    ? `<button class="icon-btn" title="Logout" onclick="handleLogout()">🚪</button>` 
                    : `<a href="/login.html" class="icon-btn" title="Login">👤</a>`
                }
                <div class="cart-icon-wrapper" style="position: relative;">
                    <a href="/cart.html" class="icon-btn" title="Cart">🛒</a>
                    <span id="cartBadge" class="cart-badge" style="display: none;">0</span>
                </div>
            </div>
        </nav>
    `;

    document.body.insertAdjacentHTML('afterbegin', navHtml);

    // 검색 폼 이벤트 리스너
    document.getElementById('searchForm').addEventListener('submit', (e) => {
        e.preventDefault();
        const keyword = document.getElementById('searchInput').value.trim();
        if (keyword) {
            window.location.href = `/index.html?keyword=${encodeURIComponent(keyword)}`;
        }
    });

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
