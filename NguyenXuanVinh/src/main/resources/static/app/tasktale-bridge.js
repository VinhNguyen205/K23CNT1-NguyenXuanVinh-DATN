/**
 * TASKTALE / LIFERPG - LIVE BACKEND API BRIDGE (v2.3 - FIXED ROUTING & TABS)
 */
(function () {
  const API_BASE = '/api/v1';

  // Bang dinh tuyenchuan giua cac man hinh Stitch
  const ROUTE_MAP = {
    'dashboard-tasks-kanban': '/app/dashboard.html',
    'dashboard': '/app/dashboard.html',
    'epic-quests-university-gpa': '/app/gpa.html',
    'epic-quests': '/app/gpa.html',
    'skill-tree-classes-nodes': '/app/skills.html',
    'skill-tree': '/app/skills.html',
    'inventory-market-gear-gacha': '/app/inventory.html',
    'inventory-market-gacha': '/app/inventory.html',
    'pet-system-familiars-chat': '/app/pets.html',
    'pet-system-familiar-incubator': '/app/pets.html',
    'social-party-co-op-leaderboard': '/app/party.html',
    'party-guild': '/app/party.html',
    'admin-dashboard-system-config': '/app/admin.html',
    'system-config': '/app/admin.html'
  };

  function getToken() {
    return localStorage.getItem('tasktale_token');
  }

  function setToken(token) {
    if (token) localStorage.setItem('tasktale_token', token);
  }

  async function apiCall(path, method = 'GET', body = null) {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) headers['Authorization'] = 'Bearer ' + token;

    const opts = { method, headers };
    if (body !== null) opts.body = JSON.stringify(body);

    const res = await fetch(API_BASE + path, opts);
    const json = await res.json().catch(() => ({}));
    if (!res.ok || (json.code && json.code !== 200)) {
      if (json.code === 50012 || json.code === 50200) {
        showRetroToast('NHÂN VẬT ĐÃ TỬ TRẬN (HP = 0)! Đang chuyển tới cổng Hồi Sinh VietQR...', 'error');
        setTimeout(() => { window.location.href = '/app/revival.html'; }, 1200);
      }
      throw new Error(json.message || 'Lỗi kết nối máy chủ (' + res.status + ')');
    }
    return json.data;
  }

  function showRetroToast(msg, type = 'info') {
    let box = document.getElementById('nxv-live-toast');
    if (!box) {
      box = document.createElement('div');
      box.id = 'nxv-live-toast';
      box.className = 'fixed bottom-5 right-5 z-[9999] max-w-md border-[3px] border-black p-4 font-mono text-xs font-bold uppercase shadow-[6px_6px_0px_0px_#000000] transition-all';
      document.body.appendChild(box);
    }
    box.style.backgroundColor = type === 'error' ? '#ffdad6' : type === 'success' ? '#ffddb8' : '#ffffff';
    box.style.color = '#1e1b19';
    box.innerHTML = `<div class="flex items-center justify-between gap-3">
      <span>⚡ [SQL SERVER LIVE]: ${msg}</span>
      <button onclick="this.parentElement.parentElement.style.display='none'" class="px-1.5 py-0.5 bg-black text-white">✕</button>
    </div>`;
    box.style.display = 'block';
    clearTimeout(window.__nxvToastTimer);
    window.__nxvToastTimer = setTimeout(() => { box.style.display = 'none'; }, 4500);
  }

  async function ensureAuthenticatedSession() {
    if (getToken()) {
      try {
        return await apiCall('/characters/me/dashboard');
      } catch (e) {
        localStorage.removeItem('tasktale_token');
      }
    }
    const demoUser = 'thorin_archmage';
    const demoPass = 'Password123!';
    try {
      const loginData = await apiCall('/auth/login', 'POST', {
        usernameOrEmail: demoUser,
        password: demoPass,
        deviceName: 'Stitch 16-Bit Neobrutalist UI'
      });
      setToken(loginData.accessToken);
    } catch (err) {
      const rand = Math.floor(1000 + Math.random() * 9000);
      const regData = await apiCall('/auth/register', 'POST', {
        username: demoUser + '_' + rand,
        email: 'thorin_' + rand + '@tasktale.vn',
        password: demoPass,
        gender: 0,
        characterName: 'THORIN',
        deviceName: 'Stitch 16-Bit Neobrutalist UI'
      });
      setToken(regData.accessToken);
    }
    return await apiCall('/characters/me/dashboard');
  }

  function wireGlobalNavigation() {
    // 1. Xu ly tat ca the <a> trong Sidebar ben trai
    document.querySelectorAll('aside nav a').forEach(a => {
      const dp = a.getAttribute('data-path') || '';
      // Tim trong ROUTE_MAP hoac theo text content
      let targetUrl = ROUTE_MAP[dp];
      const text = a.textContent.trim().toUpperCase();

      if (!targetUrl) {
        if (text.includes('BẢNG') || text.includes('TỔNG QUAN')) targetUrl = '/app/dashboard.html';
        else if (text.includes('SỬ THI') || text.includes('GPA')) targetUrl = '/app/gpa.html';
        else if (text.includes('KỸ NĂNG')) targetUrl = '/app/skills.html';
        else if (text.includes('KHO ĐỒ') || text.includes('CỬA HÀNG')) targetUrl = '/app/inventory.html';
        else if (text.includes('LINH THÚ')) targetUrl = '/app/pets.html';
        else if (text.includes('TỔ ĐỘI') || text.includes('BANG HỘI')) targetUrl = '/app/party.html';
        else if (text.includes('CẤU HÌNH') || text.includes('HỆ THỐNG')) targetUrl = '/app/admin.html';
      }

      if (targetUrl) {
        a.setAttribute('href', targetUrl);
        a.addEventListener('click', (e) => {
          e.preventDefault();
          window.location.href = targetUrl;
        });
      }
    });

    // 2. Nut [HOI SINH] va [Avatar] tren Header
    document.querySelectorAll('header button, header div').forEach(el => {
      if (el.innerText && el.innerText.trim() === 'HỒI SINH' && el.tagName === 'BUTTON') {
        el.addEventListener('click', () => {
          window.location.href = '/app/revival.html';
        });
      }
    });

    // 3. Thanh dieu huong nhanh 10 man hinh o goc duoi ben trai
    let navBar = document.getElementById('nxv-quick-navbar');
    if (!navBar) {
      navBar = document.createElement('div');
      navBar.id = 'nxv-quick-navbar';
      navBar.className = 'fixed bottom-3 left-3 z-[9990] flex flex-wrap items-center gap-1 bg-white border-2 border-black p-1.5 shadow-[4px_4px_0px_0px_#000000] font-mono text-[10px] font-bold uppercase';
      navBar.innerHTML = `
        <span class="px-1.5 py-0.5 bg-[#b61722] text-white border border-black">10 MÀN:</span>
        <a href="/app/login.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">1.AUTH</a>
        <a href="/app/dashboard.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">2.KANBAN</a>
        <a href="/app/levelup.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">3.LEVEL</a>
        <a href="/app/gpa.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">4.GPA</a>
        <a href="/app/skills.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">5.SKILLS</a>
        <a href="/app/inventory.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">6.GACHA&CHỢ</a>
        <a href="/app/pets.html" class="px-1.5 py-0.5 border border-black hover:bg-#[ffddb8]">7.LINH THÚ</a>
        <a href="/app/party.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">8.TỔ ĐỘI</a>
        <a href="/app/revival.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">9.VIETQR</a>
        <a href="/app/admin.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">10.ADMIN</a>
      `;
      document.body.appendChild(navBar);
    }
  }

  function syncHeaderHud(dash) {
    if (!dash) return;
    window.__nxvDashboard = dash;
    const header = document.querySelector('header');
    if (!header) return;

    const lvlBadge = header.querySelector('.relative.w-12.h-12 span.absolute');
    if (lvlBadge) lvlBadge.textContent = dash.currentLevel;

    const nameEl = header.querySelector('.flex.items-center.gap-space-xs span.font-title-retro');
    if (nameEl) nameEl.textContent = dash.characterName || dash.username;

    const goldSpan = header.querySelector('span.font-stat-counter');
    if (goldSpan) {
      goldSpan.textContent = Number(dash.gold || 0).toLocaleString('vi-VN') + ' VÀNG';
    }
  }

  document.addEventListener('DOMContentLoaded', async () => {
    wireGlobalNavigation();
    try {
      const dash = await ensureAuthenticatedSession();
      syncHeaderHud(dash);
    } catch (e) {
      console.warn('Bridge Init:', e);
    }
  });
})();