const main = document.getElementById('main-content');

// ========== Auth UI ==========
function updateAuthUI() {
    const loggedIn = Auth.isLoggedIn();
    document.getElementById('btn-login').classList.toggle('hidden', loggedIn);
    document.getElementById('user-menu').classList.toggle('hidden', !loggedIn);
    document.querySelectorAll('.nav-auth').forEach(el => {
        el.style.display = loggedIn ? '' : 'none';
    });
    if (loggedIn) {
        const name = Auth.username();
        document.getElementById('user-name').textContent = name;
        document.getElementById('user-avatar').textContent = name.charAt(0).toUpperCase();
        document.getElementById('link-profile').href = `#/profile/${Auth.userId()}`;
    }
}

let authTab = 'login';
function openAuthModal(tab = 'login') {
    authTab = tab;
    document.getElementById('auth-modal').classList.remove('hidden');
    document.getElementById('auth-title').textContent = tab === 'login' ? '登录' : '注册';
    document.getElementById('auth-submit').textContent = tab === 'login' ? '登录' : '注册';
    document.querySelectorAll('.tab').forEach(t => {
        t.classList.toggle('active', t.dataset.tab === tab);
    });
}

function closeAuthModal() {
    document.getElementById('auth-modal').classList.add('hidden');
    document.getElementById('auth-form').reset();
}

document.getElementById('btn-login').onclick = () => openAuthModal('login');
document.getElementById('auth-close').onclick = closeAuthModal;
document.querySelector('#auth-modal .modal-backdrop').onclick = closeAuthModal;

document.querySelectorAll('.tab').forEach(tab => {
    tab.onclick = () => openAuthModal(tab.dataset.tab);
});

document.getElementById('auth-form').onsubmit = async (e) => {
    e.preventDefault();
    const username = document.getElementById('auth-username').value.trim();
    const password = document.getElementById('auth-password').value;
    try {
        if (authTab === 'register') {
            await API.register(username, password);
            toast('注册成功，请登录', 'success');
            openAuthModal('login');
        } else {
            const res = await API.login(username, password);
            Auth.set({
                token: res.data.token,
                userId: res.data.userId,
                username: res.data.username
            });
            updateAuthUI();
            closeAuthModal();
            toast('登录成功', 'success');
            router();
        }
    } catch (err) {
        toast(err.message, 'error');
    }
};

document.getElementById('btn-logout').onclick = () => {
    Auth.clear();
    updateAuthUI();
    toast('已退出登录');
    location.hash = '#/';
};

document.getElementById('user-trigger').onclick = (e) => {
    e.stopPropagation();
    document.getElementById('user-dropdown').classList.toggle('open');
};

document.addEventListener('click', () => {
    document.getElementById('user-dropdown')?.classList.remove('open');
});

// ========== Router ==========
function getRoute() {
    const hash = location.hash.slice(1) || '/';
    const parts = hash.split('/').filter(Boolean);
    return { path: parts[0] || 'home', params: parts.slice(1) };
}

function setActiveNav() {
    const { path } = getRoute();
    const map = { home: '/', hot: '/hot', write: '/write', favorites: '/favorites' };
    document.querySelectorAll('.nav-link').forEach(link => {
        const route = link.dataset.route;
        link.classList.toggle('active', route === map[path] || (path === 'home' && route === '/'));
    });
}

async function router() {
    setActiveNav();
    const { path, params } = getRoute();
    main.innerHTML = '<div class="loading"><div class="spinner"></div>加载中…</div>';

    try {
        switch (path) {
            case 'home':
            case '':
                await renderHome();
                break;
            case 'hot':
                await renderHot();
                break;
            case 'article':
                await renderArticleDetail(params[0]);
                break;
            case 'write':
                if (!requireAuth()) { main.innerHTML = ''; return; }
                await renderEditor();
                break;
            case 'edit':
                if (!requireAuth()) { main.innerHTML = ''; return; }
                await renderEditor(params[0]);
                break;
            case 'favorites':
                if (!requireAuth()) { main.innerHTML = ''; return; }
                await renderFavorites();
                break;
            case 'profile':
                await renderProfile(params[0]);
                break;
            default:
                main.innerHTML = '<div class="empty"><div class="empty-icon">404</div><p>页面不存在</p></div>';
        }
    } catch (err) {
        main.innerHTML = `<div class="empty"><div class="empty-icon">!</div><p>${escapeHtml(err.message)}</p></div>`;
    }
}

window.addEventListener('hashchange', router);

// ========== Pages ==========
async function renderHome() {
    const [listRes, hotRes] = await Promise.all([
        API.getArticles(),
        API.getHotArticles(5)
    ]);
    const articles = listRes.data || [];
    const hot = hotRes.data || [];

    main.innerHTML = `
        <div class="layout-2col">
            <div>
                <h1 class="page-title">最新文章</h1>
                <p class="page-desc">探索社区中的最新博客内容</p>
                <div id="article-list">
                    ${articles.length ? articles.map(a => renderArticleCard(a)).join('') :
                        '<div class="empty"><div class="empty-icon">📝</div><p>还没有文章，<a href="#/write">写一篇</a>吧</p></div>'}
                </div>
            </div>
            <aside>
                <div class="card sidebar-card">
                    <h3>🔥 热门文章</h3>
                    ${hot.length ? hot.map((a, i) => `
                        <div class="sidebar-item">
                            <span class="badge">${i + 1}</span>
                            <a href="#/article/${a.id}">${escapeHtml(a.title)}</a>
                            <div style="color:var(--text-muted);font-size:0.75rem;margin-top:4px">❤ ${a.likeCount || 0}</div>
                        </div>`).join('') : '<p style="color:var(--text-muted);font-size:0.875rem">暂无热门</p>'}
                </div>
                ${!Auth.isLoggedIn() ? `
                <div class="card sidebar-card" style="margin-top:16px">
                    <h3>开始使用</h3>
                    <p style="font-size:0.875rem;color:var(--text-muted);margin-bottom:12px">登录后可以发布文章、评论、点赞和收藏</p>
                    <button class="btn btn-primary btn-block" onclick="openAuthModal('login')">立即登录</button>
                </div>` : ''}
            </aside>
        </div>`;

    bindArticleCards();
}

async function renderHot() {
    const res = await API.getHotArticles(20);
    const articles = res.data || [];
    main.innerHTML = `
        <h1 class="page-title">热门文章</h1>
        <p class="page-desc">按点赞数排序的热门内容</p>
        <div id="article-list">
            ${articles.length ? articles.map(a => renderArticleCard(a)).join('') :
                '<div class="empty"><div class="empty-icon">🔥</div><p>还没有热门文章</p></div>'}
        </div>`;
    bindArticleCards();
}

async function renderArticleDetail(id) {
    if (!id) { location.hash = '#/'; return; }
    const [articleRes, commentRes] = await Promise.all([
        API.getArticle(id),
        API.getComments(id)
    ]);
    const article = articleRes.data;
    const comments = commentRes.data || [];
    const isOwner = Auth.isLoggedIn() && Auth.userId() === article.userId;
    const topComments = comments.filter(c => !c.parentId);
    const replies = comments.filter(c => c.parentId);

    main.innerHTML = `
        <div class="article-header">
            <h1>${escapeHtml(article.title)}</h1>
            <div class="card-meta">
                <a href="#/profile/${article.userId}">@${escapeHtml(article.username)}</a>
                <span>${formatDate(article.createTime)}</span>
                <span>❤ ${article.likeCount || 0}</span>
            </div>
        </div>
        <div class="action-bar">
            ${Auth.isLoggedIn() ? `
                <button class="btn btn-outline ${article.liked ? 'active-like' : ''}" id="btn-like">${article.liked ? '已赞' : '👍 点赞'}</button>
                <button class="btn btn-outline ${article.favorited ? 'active-fav' : ''}" id="btn-fav">${article.favorited ? '已收藏' : '⭐ 收藏'}</button>
            ` : '<button class="btn btn-outline" onclick="openAuthModal(\'login\')">登录后点赞</button>'}
            ${isOwner ? `
                <a href="#/edit/${article.id}" class="btn btn-outline">编辑</a>
                <button class="btn btn-danger" id="btn-delete">删除</button>
            ` : ''}
        </div>
        <div class="article-body">${escapeHtml(article.content)}</div>

        <section class="comments-section">
            <h3>评论 (${comments.length})</h3>
            ${Auth.isLoggedIn() ? `
            <div class="comment-form">
                <textarea id="comment-input" placeholder="写下你的评论…"></textarea>
                <button class="btn btn-primary btn-sm" id="btn-comment">发表评论</button>
            </div>` : '<p style="color:var(--text-muted);margin-bottom:16px"><a href="#" onclick="openAuthModal(\'login\');return false">登录</a> 后参与评论</p>'}
            <div id="comment-list">
                ${topComments.length ? topComments.map(c => renderComment(c, replies)).join('') :
                    '<div class="empty" style="padding:30px"><p>暂无评论，来抢沙发吧</p></div>'}
            </div>
        </section>`;

    document.getElementById('btn-like')?.addEventListener('click', async () => {
        try {
            if (article.liked) {
                await API.unlikeArticle(id);
                toast('已取消点赞');
            } else {
                await API.likeArticle(id);
                toast('点赞成功', 'success');
            }
            renderArticleDetail(id);
        } catch (e) { toast(e.message, 'error'); }
    });

    document.getElementById('btn-fav')?.addEventListener('click', async () => {
        try {
            if (article.favorited) {
                await API.removeFavorite(id);
                toast('已取消收藏');
            } else {
                await API.addFavorite(id);
                toast('收藏成功', 'success');
            }
            renderArticleDetail(id);
        } catch (e) { toast(e.message, 'error'); }
    });

    document.getElementById('btn-delete')?.addEventListener('click', async () => {
        if (!confirm('确定删除这篇文章？')) return;
        try {
            await API.deleteArticle(id);
            toast('删除成功', 'success');
            location.hash = '#/';
        } catch (e) { toast(e.message, 'error'); }
    });

    document.getElementById('btn-comment')?.addEventListener('click', async () => {
        const content = document.getElementById('comment-input').value.trim();
        if (!content) return toast('请输入评论内容', 'error');
        try {
            await API.addComment(id, content);
            toast('评论成功', 'success');
            renderArticleDetail(id);
        } catch (e) { toast(e.message, 'error'); }
    });

    bindReplyButtons(id);
    bindDeleteCommentButtons();
}

function renderComment(comment, allReplies) {
    const replies = allReplies.filter(r => r.parentId === comment.id);
    const canDelete = Auth.isLoggedIn() && Auth.userId() === comment.userId;
    return `
        <div class="comment-item" data-id="${comment.id}">
            <div class="comment-header">
                <span class="comment-author">${escapeHtml(comment.username)}</span>
                <span class="comment-time">${formatDate(comment.createTime)}</span>
            </div>
            <div class="comment-content">${escapeHtml(comment.content)}</div>
            <div class="comment-actions">
                ${Auth.isLoggedIn() ? `<button class="btn btn-sm btn-outline btn-reply" data-id="${comment.id}">回复</button>` : ''}
                ${canDelete ? `<button class="btn btn-sm btn-danger btn-del-comment" data-id="${comment.id}">删除</button>` : ''}
            </div>
            <div class="reply-form hidden" id="reply-form-${comment.id}">
                <textarea placeholder="回复 @${escapeHtml(comment.username)}" rows="2" style="width:100%;margin:8px 0;padding:8px;border:1px solid var(--border);border-radius:8px;font-family:inherit"></textarea>
                <button class="btn btn-sm btn-primary btn-submit-reply" data-id="${comment.id}">发送回复</button>
            </div>
        </div>
        ${replies.map(r => `
            <div class="comment-item reply" data-id="${r.id}">
                <div class="comment-header">
                    <span class="comment-author">${escapeHtml(r.username)}</span>
                    <span class="comment-time">${formatDate(r.createTime)}</span>
                </div>
                <div class="comment-content">${escapeHtml(r.content)}</div>
                ${Auth.isLoggedIn() && Auth.userId() === r.userId ? `
                <div class="comment-actions">
                    <button class="btn btn-sm btn-danger btn-del-comment" data-id="${r.id}">删除</button>
                </div>` : ''}
            </div>`).join('')}`;
}

function bindReplyButtons(articleId) {
    document.querySelectorAll('.btn-reply').forEach(btn => {
        btn.onclick = () => {
            const id = btn.dataset.id;
            document.getElementById(`reply-form-${id}`)?.classList.toggle('hidden');
        };
    });
    document.querySelectorAll('.btn-submit-reply').forEach(btn => {
        btn.onclick = async () => {
            const parentId = btn.dataset.id;
            const form = document.getElementById(`reply-form-${parentId}`);
            const textarea = form.querySelector('textarea');
            const content = textarea.value.trim();
            if (!content) return toast('请输入回复内容', 'error');
            try {
                await API.addComment(articleId, content, Number(parentId));
                toast('回复成功', 'success');
                renderArticleDetail(articleId);
            } catch (e) { toast(e.message, 'error'); }
        };
    });
}

function bindDeleteCommentButtons() {
    document.querySelectorAll('.btn-del-comment').forEach(btn => {
        btn.onclick = async () => {
            if (!confirm('确定删除这条评论？')) return;
            const articleId = getRoute().params[0];
            try {
                await API.deleteComment(btn.dataset.id);
                toast('删除成功', 'success');
                renderArticleDetail(articleId);
            } catch (e) { toast(e.message, 'error'); }
        };
    });
}

async function renderEditor(editId) {
    let title = '', content = '';
    const isEdit = !!editId;
    if (isEdit) {
        const res = await API.getArticle(editId);
        if (res.data.userId !== Auth.userId()) {
            toast('只能编辑自己的文章', 'error');
            location.hash = '#/';
            return;
        }
        title = res.data.title;
        content = res.data.content;
    }

    main.innerHTML = `
        <h1 class="page-title">${isEdit ? '编辑文章' : '写文章'}</h1>
        <p class="page-desc">${isEdit ? '修改你的文章内容' : '分享你的想法与见解'}</p>
        <div class="card">
            <div class="form-group">
                <label>标题</label>
                <input type="text" id="editor-title" placeholder="输入文章标题">
            </div>
            <div class="form-group">
                <label>正文</label>
                <textarea id="editor-content" class="editor-area" placeholder="开始写作…"></textarea>
            </div>
            <div style="display:flex;gap:10px">
                <button class="btn btn-primary" id="btn-publish">${isEdit ? '保存修改' : '发布文章'}</button>
                <a href="${isEdit ? '#/article/' + editId : '#/'}" class="btn btn-outline">取消</a>
            </div>
            <div class="ai-panel">
                <h3>✨ AI 写作助手（需配置 DeepSeek API Key）</h3>
                <div class="ai-buttons">
                    <button class="btn btn-sm btn-outline" data-ai="title">优化标题</button>
                    <button class="btn btn-sm btn-outline" data-ai="summary">生成摘要</button>
                    <button class="btn btn-sm btn-outline" data-ai="keywords">提取关键词</button>
                    <button class="btn btn-sm btn-outline" data-ai="tags">推荐标签</button>
                </div>
                <div class="ai-result" id="ai-result">点击上方按钮，AI 将根据正文内容生成建议</div>
            </div>
        </div>`;

    document.getElementById('editor-title').value = title;
    document.getElementById('editor-content').value = content;

    document.getElementById('btn-publish').onclick = async () => {
        const t = document.getElementById('editor-title').value.trim();
        const c = document.getElementById('editor-content').value.trim();
        if (!t || !c) return toast('标题和正文不能为空', 'error');
        try {
            if (isEdit) {
                await API.editArticle(Number(editId), t, c);
                toast('保存成功', 'success');
                location.hash = `#/article/${editId}`;
            } else {
                const res = await API.addArticle(t, c);
                toast('发布成功', 'success');
                location.hash = `#/article/${res.data.id}`;
            }
        } catch (e) { toast(e.message, 'error'); }
    };

    document.querySelectorAll('[data-ai]').forEach(btn => {
        btn.onclick = async () => {
            const c = document.getElementById('editor-content').value.trim();
            if (!c) return toast('请先输入正文内容', 'error');
            const resultEl = document.getElementById('ai-result');
            resultEl.textContent = 'AI 思考中…';
            try {
                const type = btn.dataset.ai;
                let res;
                if (type === 'title') res = await API.aiTitle(c);
                else if (type === 'summary') res = await API.aiSummary(c);
                else if (type === 'keywords') res = await API.aiKeywords(c);
                else res = await API.aiTags(c);
                const text = res.data.result;
                resultEl.textContent = text;
                if (type === 'title') {
                    document.getElementById('editor-title').value = text;
                    toast('标题已填入', 'success');
                }
            } catch (e) {
                resultEl.textContent = e.message;
                toast(e.message, 'error');
            }
        };
    });
}

async function renderFavorites() {
    const res = await API.getFavorites();
    const articles = res.data || [];
    main.innerHTML = `
        <h1 class="page-title">我的收藏</h1>
        <p class="page-desc">你收藏的所有文章</p>
        <div id="article-list">
            ${articles.length ? articles.map(a => renderArticleCard(a, { showActions: true })).join('') :
                '<div class="empty"><div class="empty-icon">⭐</div><p>还没有收藏，去<a href="#/">首页</a>逛逛吧</p></div>'}
        </div>`;
    bindArticleCards();
    bindCardActions();
}

async function renderProfile(userId) {
    if (!userId) { location.hash = '#/'; return; }
    const res = await API.getUserHome(userId);
    const home = res.data;
    const isSelf = Auth.isLoggedIn() && Auth.userId() === Number(userId);

    main.innerHTML = `
        <h1 class="page-title">${escapeHtml(home.username)} 的主页</h1>
        <p class="page-desc">${isSelf ? '这是你的个人主页' : '查看 TA 发布的文章'}</p>
        <div class="stats-row">
            <div class="stat-card"><div class="stat-num">${home.articleCount || 0}</div><div class="stat-label">文章</div></div>
            <div class="stat-card"><div class="stat-num">${home.likeCount || 0}</div><div class="stat-label">获赞</div></div>
            <div class="stat-card"><div class="stat-num">${(home.articles || []).length}</div><div class="stat-label">展示</div></div>
        </div>
        <h3 style="margin-bottom:16px">TA 的文章</h3>
        <div id="article-list">
            ${(home.articles || []).length ? home.articles.map(a => renderArticleCard(a)).join('') :
                '<div class="empty"><p>还没有发布文章</p></div>'}
        </div>`;
    bindArticleCards();
}

function bindArticleCards() {
    document.querySelectorAll('.card-clickable').forEach(card => {
        card.onclick = (e) => {
            if (e.target.closest('.card-actions')) return;
            location.hash = `#/article/${card.dataset.id}`;
        };
    });
}

function bindCardActions() {
    document.querySelectorAll('[data-like]').forEach(btn => {
        btn.onclick = async (e) => {
            e.stopPropagation();
            const id = btn.dataset.like;
            try {
                if (btn.classList.contains('active-like')) {
                    await API.unlikeArticle(id);
                } else {
                    await API.likeArticle(id);
                }
                router();
            } catch (err) { toast(err.message, 'error'); }
        };
    });
    document.querySelectorAll('[data-fav]').forEach(btn => {
        btn.onclick = async (e) => {
            e.stopPropagation();
            const id = btn.dataset.fav;
            try {
                if (btn.classList.contains('active-fav')) {
                    await API.removeFavorite(id);
                } else {
                    await API.addFavorite(id);
                }
                router();
            } catch (err) { toast(err.message, 'error'); }
        };
    });
}

// ========== Init ==========
updateAuthUI();
if (!location.hash) location.hash = '#/';
else router();
