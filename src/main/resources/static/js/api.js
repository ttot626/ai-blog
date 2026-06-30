const Auth = {
    get() {
        const raw = localStorage.getItem('ai_blog_user');
        return raw ? JSON.parse(raw) : null;
    },
    set(user) {
        localStorage.setItem('ai_blog_user', JSON.stringify(user));
    },
    clear() {
        localStorage.removeItem('ai_blog_user');
    },
    isLoggedIn() {
        return !!this.get()?.token;
    },
    token() {
        return this.get()?.token || '';
    },
    userId() {
        return this.get()?.userId || null;
    },
    username() {
        return this.get()?.username || '';
    }
};

const API = {
    async request(path, options = {}) {
        const headers = { ...(options.headers || {}) };
        if (!(options.body instanceof FormData)) {
            headers['Content-Type'] = headers['Content-Type'] || 'application/json';
        }
        const token = Auth.token();
        if (token) headers['Authorization'] = `Bearer ${token}`;

        const res = await fetch(path, { ...options, headers });
        let json;
        try {
            json = await res.json();
        } catch {
            throw new Error('服务器无响应，请确认程序已启动');
        }
        if (json.code !== 200) {
            throw new Error(json.message || '请求失败');
        }
        return json;
    },

    register(username, password) {
        return this.request('/user/register', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
    },

    login(username, password) {
        return this.request('/user/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
    },

    getArticles() {
        return this.request('/article/list');
    },

    getHotArticles(limit = 5) {
        return this.request(`/article/hot?limit=${limit}`);
    },

    getArticle(id) {
        return this.request(`/article/detail?id=${id}`);
    },

    addArticle(title, content) {
        return this.request('/article/add', {
            method: 'POST',
            body: JSON.stringify({ title, content })
        });
    },

    editArticle(id, title, content) {
        return this.request('/article/edit', {
            method: 'POST',
            body: JSON.stringify({ id, title, content })
        });
    },

    deleteArticle(id) {
        return this.request(`/article/delete?id=${id}`, { method: 'POST' });
    },

    likeArticle(id) {
        return this.request(`/article/like?articleId=${id}`, { method: 'POST' });
    },

    unlikeArticle(id) {
        return this.request(`/article/unlike?articleId=${id}`, { method: 'POST' });
    },

    getComments(articleId) {
        return this.request(`/comment/list?articleId=${articleId}`);
    },

    addComment(articleId, content, parentId = null) {
        const body = { articleId, content };
        if (parentId) body.parentId = parentId;
        return this.request('/comment/add', {
            method: 'POST',
            body: JSON.stringify(body)
        });
    },

    deleteComment(id) {
        return this.request(`/comment/delete?id=${id}`, { method: 'POST' });
    },

    addFavorite(articleId) {
        return this.request(`/favorite/add?articleId=${articleId}`, { method: 'POST' });
    },

    removeFavorite(articleId) {
        return this.request(`/favorite/remove?articleId=${articleId}`, { method: 'POST' });
    },

    getFavorites() {
        return this.request('/favorite/list');
    },

    getUserHome(userId) {
        return this.request(`/user/home?userId=${userId}`);
    },

    aiSummary(content) {
        return this.request('/ai/summary', {
            method: 'POST',
            body: JSON.stringify({ content })
        });
    },

    aiTitle(content) {
        return this.request('/ai/title', {
            method: 'POST',
            body: JSON.stringify({ content })
        });
    },

    aiKeywords(content) {
        return this.request('/ai/keywords', {
            method: 'POST',
            body: JSON.stringify({ content })
        });
    },

    aiTags(content) {
        return this.request('/ai/tags', {
            method: 'POST',
            body: JSON.stringify({ content })
        });
    }
};

function formatDate(time) {
    if (!time) return '';
    let d;
    if (Array.isArray(time)) {
        const [y, mo, day, h = 0, mi = 0] = time;
        d = new Date(y, mo - 1, day, h, mi);
    } else {
        d = new Date(time);
    }
    if (isNaN(d.getTime())) return '';
    const pad = n => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function truncate(str, len = 120) {
    if (!str) return '';
    return str.length > len ? str.slice(0, len) + '…' : str;
}

function toast(msg, type = 'info') {
    const el = document.createElement('div');
    el.className = `toast ${type === 'error' ? 'error' : type === 'success' ? 'success' : ''}`;
    el.textContent = msg;
    document.getElementById('toast-container').appendChild(el);
    setTimeout(() => el.remove(), 3000);
}

function requireAuth() {
    if (!Auth.isLoggedIn()) {
        toast('请先登录', 'error');
        openAuthModal('login');
        return false;
    }
    return true;
}

function renderArticleCard(article, options = {}) {
    const { showActions = false, linkPrefix = '#/article/' } = options;
    const liked = article.liked ? 'active-like' : '';
    const fav = article.favorited ? 'active-fav' : '';
    return `
        <article class="card card-clickable" data-id="${article.id}">
            <h3 class="card-title">${escapeHtml(article.title)}</h3>
            <div class="card-meta">
                <span>@${escapeHtml(article.username || '未知')}</span>
                <span>${formatDate(article.createTime)}</span>
                <span>❤ ${article.likeCount || 0}</span>
            </div>
            <p class="card-excerpt">${escapeHtml(truncate(article.content))}</p>
            ${showActions ? `
            <div class="card-actions" onclick="event.stopPropagation()">
                <button class="btn btn-sm btn-outline ${liked}" data-like="${article.id}">${article.liked ? '已赞' : '点赞'}</button>
                <button class="btn btn-sm btn-outline ${fav}" data-fav="${article.id}">${article.favorited ? '已收藏' : '收藏'}</button>
            </div>` : ''}
        </article>`;
}
