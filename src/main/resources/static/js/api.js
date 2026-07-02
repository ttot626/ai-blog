const Auth = {
    get() {
        const raw = localStorage.getItem('ai_blog_user');
        return raw ? JSON.parse(raw) : null;
    },
    set(user) {
        localStorage.setItem('ai_blog_user', JSON.stringify({
            token: user.token,
            userId: Number(user.userId),
            username: user.username
        }));
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
        const id = this.get()?.userId;
        return id == null ? null : Number(id);
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

        let res;
        try {
            res = await fetch(path, { ...options, headers });
        } catch {
            throw new Error('网络连接失败，请检查服务器是否运行');
        }

        let json;
        try {
            json = await res.json();
        } catch {
            throw new Error('服务器响应异常');
        }

        if (json.code === 401) {
            Auth.clear();
            updateAuthUI?.();
            throw new Error(json.message || '登录已过期，请重新登录');
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

    getArticles(page = 1, size = 10) {
        return this.request(`/article/list?page=${page}&size=${size}`);
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
            body: JSON.stringify({ id: Number(id), title, content })
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
        const body = { articleId: Number(articleId), content };
        if (parentId) body.parentId = Number(parentId);
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

function sameUserId(a, b) {
    if (a == null || b == null) return false;
    return Number(a) === Number(b);
}

function formatDate(time) {
    if (!time) return '';
    let d;
    if (Array.isArray(time)) {
        const [y, mo, day, h = 0, mi = 0, s = 0] = time;
        d = new Date(y, mo - 1, day, h, mi, s);
    } else if (typeof time === 'string' && !time.includes('T')) {
        d = new Date(time.replace(' ', 'T'));
    } else {
        d = new Date(time);
    }
    if (isNaN(d.getTime())) return String(time);
    const pad = n => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function escapeHtml(str) {
    if (str == null) return '';
    return String(str).replace(/&/g, '&amp;')
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
    setTimeout(() => el.remove(), 3200);
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
    const { showActions = false, showOwnerActions = false } = options;
    const liked = article.liked ? 'active-like' : '';
    const fav = article.favorited ? 'active-fav' : '';
    const isOwner = showOwnerActions && sameUserId(Auth.userId(), article.userId);
    return `
        <article class="card card-clickable" data-id="${article.id}">
            <h3 class="card-title">${escapeHtml(article.title)}</h3>
            <div class="card-meta">
                <a href="#/profile/${article.userId}" class="author-link" onclick="event.stopPropagation()">@${escapeHtml(article.username || '未知')}</a>
                <span>${formatDate(article.createTime)}</span>
                <span>❤ ${article.likeCount || 0}</span>
            </div>
            <p class="card-excerpt">${escapeHtml(truncate(article.content))}</p>
            ${showActions || isOwner ? `
            <div class="card-actions" onclick="event.stopPropagation()">
                ${showActions ? `
                <button class="btn btn-sm btn-outline ${liked}" data-like="${article.id}">${article.liked ? '已赞' : '👍 点赞'}</button>
                <button class="btn btn-sm btn-outline ${fav}" data-fav="${article.id}">${article.favorited ? '已收藏' : '⭐ 收藏'}</button>` : ''}
                ${isOwner ? `
                <a href="#/edit/${article.id}" class="btn btn-sm btn-outline">编辑</a>
                <button class="btn btn-sm btn-danger" data-del-article="${article.id}">删除</button>` : ''}
            </div>` : ''}
        </article>`;
}

function bindArticleCards() {
    document.querySelectorAll('.card-clickable').forEach(card => {
        card.onclick = (e) => {
            if (e.target.closest('.card-actions') || e.target.closest('.author-link')) return;
            location.hash = `#/article/${card.dataset.id}`;
        };
    });
}

function bindCardActions() {
    document.querySelectorAll('[data-like]').forEach(btn => {
        btn.onclick = async (e) => {
            e.stopPropagation();
            if (!requireAuth()) return;
            const id = btn.dataset.like;
            try {
                if (btn.classList.contains('active-like')) {
                    await API.unlikeArticle(id);
                    toast('已取消点赞');
                } else {
                    await API.likeArticle(id);
                    toast('点赞成功', 'success');
                }
                router();
            } catch (err) { toast(err.message, 'error'); }
        };
    });
    document.querySelectorAll('[data-fav]').forEach(btn => {
        btn.onclick = async (e) => {
            e.stopPropagation();
            if (!requireAuth()) return;
            const id = btn.dataset.fav;
            try {
                if (btn.classList.contains('active-fav')) {
                    await API.removeFavorite(id);
                    toast('已取消收藏');
                } else {
                    await API.addFavorite(id);
                    toast('收藏成功', 'success');
                }
                router();
            } catch (err) { toast(err.message, 'error'); }
        };
    });
    document.querySelectorAll('[data-del-article]').forEach(btn => {
        btn.onclick = async (e) => {
            e.stopPropagation();
            if (!confirm('确定删除这篇文章？')) return;
            try {
                await API.deleteArticle(btn.dataset.delArticle);
                toast('删除成功', 'success');
                router();
            } catch (err) { toast(err.message, 'error'); }
        };
    });
}

function cardOptionsForList(extra = {}) {
    return {
        showActions: Auth.isLoggedIn(),
        ...extra
    };
}

function bindListPage() {
    bindArticleCards();
    bindCardActions();
}

function renderPagination(pageData) {
    if (!pageData || pageData.pages <= 1) return '';
    const page = Number(pageData.page);
    const pages = Number(pageData.pages);
    let buttons = `<button type="button" class="btn btn-sm btn-outline" data-page="${page - 1}" ${page <= 1 ? 'disabled' : ''}>上一页</button>`;
    for (let i = 1; i <= pages; i++) {
        if (pages > 7 && i > 2 && i < pages - 1 && Math.abs(i - page) > 1) {
            if (i === 3 || i === pages - 2) buttons += '<span class="pagination-ellipsis">…</span>';
            continue;
        }
        buttons += `<button type="button" class="btn btn-sm ${i === page ? 'btn-primary' : 'btn-outline'}" data-page="${i}">${i}</button>`;
    }
    buttons += `<button type="button" class="btn btn-sm btn-outline" data-page="${page + 1}" ${page >= pages ? 'disabled' : ''}>下一页</button>`;
    return `
        <nav class="pagination" aria-label="文章分页">
            ${buttons}
            <span class="pagination-info">第 ${page} / ${pages} 页，共 ${pageData.total} 篇</span>
        </nav>`;
}

function bindPagination(onPage) {
    document.querySelectorAll('.pagination [data-page]').forEach(btn => {
        btn.onclick = () => {
            const target = Number(btn.dataset.page);
            if (!btn.disabled && target >= 1) onPage(target);
        };
    });
}
