// app.js - Global App Logic

const API_BASE_URL = '/api';

const App = {
    // Auth State
    token: localStorage.getItem('token') || null,
    memberInfo: null,

    init() {
        this.renderNavbar();
        this.setupToastContainer();
        if (this.token) {
            this.fetchMemberInfo();
        }
    },

    // Set Token
    setToken(token) {
        this.token = token;
        if (token) {
            localStorage.setItem('token', token);
            this.fetchMemberInfo();
        } else {
            localStorage.removeItem('token');
            this.memberInfo = null;
            this.renderNavbar();
        }
    },

    // API Call Wrapper with JWT
    async fetch(endpoint, options = {}) {
        const headers = {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        };

        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                ...options,
                headers
            });

            const data = await response.json().catch(() => null);

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    this.showToast('로그인이 필요하거나 권한이 없습니다.', 'error');
                    this.setToken(null);
                    window.location.href = '/login.html';
                }
                const errorMsg = data?.message || '요청 처리 중 오류가 발생했습니다.';
                throw new Error(errorMsg);
            }

            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },

    async fetchMemberInfo() {
        try {
            const res = await this.fetch('/members/me');
            if (res && res.data) {
                this.memberInfo = res.data;
                this.renderNavbar();
            }
        } catch (error) {
            this.setToken(null);
        }
    },

    logout() {
        this.setToken(null);
        this.showToast('로그아웃 되었습니다.', 'success');
        window.location.href = '/index.html';
    },

    renderNavbar() {
        const navRight = document.getElementById('nav-right');
        if (!navRight) return;

        if (this.token) {
            // Logged in
            navRight.innerHTML = `
                <a href="/cart.html" class="nav-link" style="display: flex; align-items: center; gap: 4px;">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="9" cy="21" r="1"></circle><circle cx="20" cy="21" r="1"></circle><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path></svg>
                    장바구니
                </a>
                <a href="/orders.html" class="nav-link">주문내역</a>
                <span class="badge badge-success" style="margin-left: 8px;">${this.memberInfo ? this.memberInfo.name + '님' : '회원님'}</span>
                <button onclick="App.logout()" class="btn btn-secondary" style="padding: 0.25rem 0.75rem; margin-left: 8px; font-size: 0.875rem;">로그아웃</button>
            `;
        } else {
            // Not logged in
            navRight.innerHTML = `
                <a href="/login.html" class="btn btn-primary" style="padding: 0.25rem 1rem;">로그인</a>
            `;
        }
    },

    setupToastContainer() {
        if (!document.getElementById('toast-container')) {
            const container = document.createElement('div');
            container.id = 'toast-container';
            document.body.appendChild(container);
        }
    },

    showToast(message, type = 'success') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        
        let icon = type === 'success' 
            ? '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>'
            : '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>';

        toast.innerHTML = `
            <div>${icon}</div>
            <div>
                <div style="font-weight: 600; margin-bottom: 0.25rem;">${type === 'success' ? '성공' : '오류'}</div>
                <div style="font-size: 0.875rem; color: var(--secondary);">${message}</div>
            </div>
        `;

        container.appendChild(toast);

        setTimeout(() => {
            toast.style.animation = 'slideOut 0.3s ease-in forwards';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    },

    formatCurrency(amount) {
        return amount.toLocaleString('ko-KR') + '원';
    },

    formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }
};

document.addEventListener('DOMContentLoaded', () => {
    App.init();
});
