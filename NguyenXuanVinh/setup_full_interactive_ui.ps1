# ==============================================================================
# TASKTALE / LIFERPG - DEEP DOM HYDRATION v5.0 (ALL 9 MODULES + 10 SCREENS LIVE)
# ==============================================================================

$ErrorActionPreference = "Stop"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$staticAppDir = Join-Path (Get-Location) "src\main\resources\static\app"
$targetAppDir = Join-Path (Get-Location) "target\classes\static\app"

$bridgeJs = @'
/**
 * TASKTALE / LIFERPG - DEEP DOM HYDRATION BRIDGE (v5.0)
 * Ket noi toan dien 122 chuc nang (9 Modules) len 10 man hinh Stitch 16-Bit Neobrutalism
 */
(function () {
  const API_BASE = '/api/v1';

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

  function getToken() { return localStorage.getItem('tasktale_token'); }
  function setToken(t) { if (t) localStorage.setItem('tasktale_token', t); }

  function getInProgressSet() {
    try { return new Set(JSON.parse(localStorage.getItem('tasktale_inprogress_ids') || '[]')); }
    catch (e) { return new Set(); }
  }
  function saveInProgressSet(s) {
    localStorage.setItem('tasktale_inprogress_ids', JSON.stringify(Array.from(s)));
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
        showRetroToast('NH\u00C2N V\u1EACT \u0110\u00C3 T\u1EEC TR\u1EACN (HP = 0)! \u0110ang chuy\u1EC3n t\u1EDBi c\u1ED5ng H\u1ED3i Sinh VietQR...', 'error');
        setTimeout(() => { window.location.href = '/app/revival.html'; }, 900);
      }
      throw new Error(json.message || ('L\u1ED7i m\u00E1y ch\u1EE7 (' + res.status + ')'));
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
    box.innerHTML = '<div class="flex items-center justify-between gap-3">' +
      '<span>\u26A1 [SQL SERVER]: ' + msg + '</span>' +
      '<button onclick="this.parentElement.parentElement.style.display=\'none\'" class="px-1.5 py-0.5 bg-black text-white">\u2715</button>' +
      '</div>';
    box.style.display = 'block';
    clearTimeout(window.__nxvToastTimer);
    window.__nxvToastTimer = setTimeout(() => { box.style.display = 'none'; }, 4200);
  }

  async function ensureAuthenticatedSession() {
    if (getToken()) {
      try { return await apiCall('/characters/me/dashboard'); }
      catch (e) { localStorage.removeItem('tasktale_token'); }
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

  // ===========================================================================
  // MODULE 2: MODAL HO SO NHAN VAT, DANH HIEU, THANH TUU & LICH SU CHI SO
  // ===========================================================================
  async function openCharacterSheetModal() {
    let modal = document.getElementById('nxv-char-sheet-modal');
    if (!modal) {
      modal = document.createElement('div');
      modal.id = 'nxv-char-sheet-modal';
      modal.className = 'fixed inset-0 z-[9995] bg-black/70 flex items-center justify-center p-4';
      document.body.appendChild(modal);
    }
    modal.style.display = 'flex';
    modal.innerHTML = '<div class="bg-white border-4 border-black p-6 font-mono text-sm font-bold shadow-[8px_8px_0px_#000]">\u0110ANG T\u1EA2I H\u1ED2 S\u01A0 NH\u00C2N V\u1EACT T\u1EEC SQL SERVER...</div>';

    try {
      const [dash, titles, achs, logs] = await Promise.all([
        apiCall('/characters/me/dashboard'),
        apiCall('/characters/me/titles'),
        apiCall('/characters/me/achievements'),
        apiCall('/characters/me/stat-logs')
      ]);
      syncHeaderHud(dash);

      const titleOptions = (titles || []).map(t =>
        '<option value="' + t.titleId + '" ' + (t.isEquipped ? 'selected' : '') + '>' +
        t.titleName + (t.isUnlocked ? ' [\u0110\u00C3 M\u1EDA]' : ' [CH\u01AFA M\u1EDA]') + '</option>'
      ).join('');

      const achHtml = (achs || []).map(a =>
        '<div class="p-2 border-2 border-black ' + (a.isUnlocked ? 'bg-[#ffddb8]' : 'bg-[#f4ece8] opacity-70') + ' flex items-center justify-between">' +
          '<div>' +
            '<div class="font-bold text-xs uppercase">' + a.achievementName + '</div>' +
            '<div class="text-[11px] text-[#5b403e]">' + (a.description || '') + ' (+' + a.rewardExp + ' EXP / +' + a.rewardGold + ' G)</div>' +
          '</div>' +
          '<span class="px-1.5 py-0.5 border border-black text-[10px] font-bold ' + (a.isUnlocked ? 'bg-[#b61722] text-white' : 'bg-white') + '">' +
            (a.isUnlocked ? '\u0110\u00C3 \u0110\u1EA0T' : 'CH\u01AFA \u0110\u1EA0T') +
          '</span>' +
        '</div>'
      ).join('');

      const logHtml = (logs || []).slice(0, 8).map(l =>
        '<div class="flex justify-between border-b border-black/20 py-1 text-[11px] font-mono">' +
          '<span>[' + l.logType + '] ' + l.sourceType + '</span>' +
          '<span class="font-bold ' + (l.delta >= 0 ? 'text-[#0058be]' : 'text-[#b61722]') + '">' + (l.delta >= 0 ? '+' : '') + l.delta + '</span>' +
        '</div>'
      ).join('');

      modal.innerHTML =
        '<div class="w-full max-w-2xl max-h-[90vh] overflow-y-auto bg-[#fff8f5] border-4 border-black shadow-[8px_8px_0px_#000000] flex flex-col">' +
          '<div class="bg-[#b61722] text-white px-4 py-3 border-b-2 border-black flex items-center justify-between">' +
            '<span class="font-mono font-bold uppercase tracking-wider">\u2694\uFE0F H\u1ED2 S\u01A0 NH\u00C2N V\u1EACT & DANH HI\u1EC6U (MODULE 2)</span>' +
            '<button id="nxv-close-char-modal" class="px-2 py-0.5 bg-black text-white border border-white font-mono text-xs">\u2715 \u0110\u00D3NG</button>' +
          '</div>' +
          '<div class="p-5 flex flex-col gap-4">' +
            '<div class="grid grid-cols-1 md:grid-cols-2 gap-3 bg-white p-3 border-2 border-black shadow-[3px_3px_0px_#000]">' +
              '<div>' +
                '<label class="block font-mono text-[10px] uppercase text-[#5b403e] font-bold">\u0110\u1ED4I T\u00CAN NH\u00C2N V\u1EACT (#12):</label>' +
                '<div class="flex gap-1 mt-1">' +
                  '<input id="nxv-char-name-inp" type="text" value="' + (dash.characterName || '') + '" class="flex-1 px-2 py-1 border-2 border-black font-mono text-xs font-bold"/>' +
                  '<button id="nxv-save-name-btn" class="px-2.5 py-1 bg-[#b61722] text-white border-2 border-black font-mono text-xs font-bold">L\u01AFU</button>' +
                '</div>' +
              '</div>' +
              '<div>' +
                '<label class="block font-mono text-[10px] uppercase text-[#5b403e] font-bold">DANH HI\u1EC6U HI\u1EC2N TH\u1ECA (#19):</label>' +
                '<div class="flex gap-1 mt-1">' +
                  '<select id="nxv-char-title-sel" class="flex-1 px-2 py-1 border-2 border-black font-mono text-xs font-bold">' + titleOptions + '</select>' +
                  '<button id="nxv-save-title-btn" class="px-2.5 py-1 bg-[#0058be] text-white border-2 border-black font-mono text-xs font-bold">TRANG B\u1ECA</button>' +
                '</div>' +
              '</div>' +
            '</div>' +
            '<div class="grid grid-cols-4 gap-2 text-center font-mono text-xs">' +
              '<div class="p-2 bg-white border-2 border-black">HP G\u1ED0C: <b>' + dash.baseMaxHp + '</b><br/>BUFF: <b>+' + dash.totalBuffMaxHp + '</b></div>' +
              '<div class="p-2 bg-white border-2 border-black">MP G\u1ED0C: <b>' + dash.baseMaxMp + '</b><br/>BUFF: <b>+' + dash.totalBuffMaxMp + '</b></div>' +
              '<div class="p-2 bg-white border-2 border-black">TASK XONG:<br/><b>' + dash.totalTasksDone + ' TASKS</b></div>' +
              '<div class="p-2 bg-white border-2 border-black">CHU\u1ED6I STREAK:<br/><b>' + dash.streakDays + ' NG\u00C0Y</b></div>' +
            '</div>' +
            '<div class="bg-white p-3 border-2 border-black shadow-[3px_3px_0px_#000]">' +
              '<div class="font-mono text-xs font-bold uppercase mb-2 border-b border-black pb-1">\uD83C\uDFC6 TH\u00C0NH T\u1EF0U H\u1EC6 TH\u1ED0NG (#101)</div>' +
              '<div class="flex flex-col gap-1.5">' + achHtml + '</div>' +
            '</div>' +
            '<div class="bg-white p-3 border-2 border-black shadow-[3px_3px_0px_#000]">' +
              '<div class="font-mono text-xs font-bold uppercase mb-2 border-b border-black pb-1">\uD83D\uDCCA L\u1ECACH S\u1EEC BI\u1EBEN \u0110\u1ED8NG CH\u1EC8 S\u1ED0 G\u1EA6N \u0110\u00C2Y (#17)</div>' +
              '<div class="flex flex-col">' + (logHtml || '<span class="text-xs">Ch\u01B0a c\u00F3 log bi\u1EBFn \u0111\u1ED9ng.</span>') + '</div>' +
            '</div>' +
          '</div>' +
        '</div>';

      document.getElementById('nxv-close-char-modal').onclick = () => { modal.style.display = 'none'; };
      document.getElementById('nxv-save-name-btn').onclick = async () => {
        const val = document.getElementById('nxv-char-name-inp').value.trim();
        if (!val) return;
        const updated = await apiCall('/characters/me/name', 'PUT', { characterName: val });
        syncHeaderHud(updated);
        showRetroToast('\u0110\u00E3 \u0111\u1ED5i t\u00EAn nh\u00E2n v\u1EADt th\u00E0nh: ' + val, 'success');
      };
      document.getElementById('nxv-save-title-btn').onclick = async () => {
        const tid = parseInt(document.getElementById('nxv-char-title-sel').value, 10);
        try {
          const updated = await apiCall('/characters/me/title', 'PUT', { titleId: tid });
          syncHeaderHud(updated);
          showRetroToast('\u0110\u00E3 trang b\u1ECB Danh hi\u1EC7u m\u1EDBi!', 'success');
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      };
    } catch (e) {
      modal.style.display = 'none';
      showRetroToast(e.message, 'error');
    }
  }

  // 1. DIEU HUONG SIDEBAR, HEADER & THANH 10 MAN HINH
  function wireGlobalNavigation() {
    const currentPath = window.location.pathname;

    document.querySelectorAll('aside nav a').forEach(a => {
      const dp = a.getAttribute('data-path') || '';
      let targetUrl = ROUTE_MAP[dp];
      const text = a.textContent.trim().toUpperCase();

      if (!targetUrl) {
        if (text.includes('B\u1EA2NG') || text.includes('T\u1ED4NG QUAN')) targetUrl = '/app/dashboard.html';
        else if (text.includes('S\u1EEC THI') || text.includes('GPA')) targetUrl = '/app/gpa.html';
        else if (text.includes('K\u1ECE N\u0102NG') || text.includes('K\u1EF8 N\u0102NG')) targetUrl = '/app/skills.html';
        else if (text.includes('KHO \u0110\u1ED2') || text.includes('T\u00DAI \u0110\u1ED2') || text.includes('C\u1EECA H\u00C0NG')) targetUrl = '/app/inventory.html';
        else if (text.includes('LINH TH\u00DA')) targetUrl = '/app/pets.html';
        else if (text.includes('T\u1ED4 \u0110\u1ED8I') || text.includes('BANG H\u1ED8I')) targetUrl = '/app/party.html';
        else if (text.includes('C\u1EA4U H\u00CCNH') || text.includes('H\u1EC6 TH\u1ED0NG')) targetUrl = '/app/admin.html';
      }

      if (targetUrl) {
        a.setAttribute('href', targetUrl);
        if (currentPath.endsWith(targetUrl)) {
          a.classList.remove('border-transparent', 'text-on-surface-variant');
          a.classList.add('bg-surface-container', 'text-on-surface', 'border-2', 'border-black', 'shadow-[2px_2px_0px_#000000]', 'translate-x-1');
        } else {
          a.classList.remove('bg-surface-container', 'border-black', 'shadow-[2px_2px_0px_#000000]', 'translate-x-1');
          a.classList.add('border-transparent', 'text-on-surface-variant');
        }
        a.onclick = (e) => {
          e.preventDefault();
          window.location.href = targetUrl;
        };
      }
    });

    // Click vao Khung Avatar / Ten Nhan Vat goc trai Header -> Mo Modal Ho So Nhan Vat (Module 2)
    const heroBox = document.querySelector('header > div:first-child');
    if (heroBox) {
      heroBox.style.cursor = 'pointer';
      heroBox.title = 'Nh\u1EA5n \u0111\u1EC3 m\u1EDF H\u1ED3 S\u01A1 Nh\u00E2n V\u1EADt, Danh Hi\u1EC7u & Th\u00E0nh T\u1EF1u';
      heroBox.onclick = () => openCharacterSheetModal();
    }

    document.querySelectorAll('header button, header div').forEach(el => {
      if (el.tagName === 'BUTTON' && el.innerText && (el.innerText.toUpperCase().includes('H\u1ED2I SINH') || el.innerText.toLowerCase().includes('skull'))) {
        el.onclick = (e) => {
          e.preventDefault();
          window.location.href = '/app/revival.html';
        };
      }
      if (el.querySelector && el.querySelector('span.material-symbols-outlined')?.textContent.trim() === 'person') {
        el.style.cursor = 'pointer';
        el.title = 'Chuy\u1EC3n t\u1EDBi m\u00E0n h\u00ECnh \u0110\u0103ng nh\u1EADp / T\u1EA1o nh\u00E2n v\u1EADt';
        el.onclick = () => { window.location.href = '/app/login.html'; };
      }
    });

    let navBar = document.getElementById('nxv-quick-navbar');
    if (!navBar) {
      navBar = document.createElement('div');
      navBar.id = 'nxv-quick-navbar';
      navBar.className = 'fixed bottom-3 left-3 z-[9990] flex flex-wrap items-center gap-1 bg-white border-2 border-black p-1.5 shadow-[4px_4px_0px_0px_#000000] font-mono text-[10px] font-bold uppercase';
      const links = [
        ['/app/login.html', '1.AUTH'],
        ['/app/dashboard.html', '2.KANBAN'],
        ['/app/levelup.html', '3.LEVEL-UP'],
        ['/app/gpa.html', '4.GPA'],
        ['/app/skills.html', '5.K\u1EF8 N\u0102NG'],
        ['/app/inventory.html', '6.KHO&GACHA'],
        ['/app/pets.html', '7.LINH TH\u00DA'],
        ['/app/party.html', '8.T\u1ED4 \u0110\u1ED8I'],
        ['/app/revival.html', '9.VIETQR'],
        ['/app/admin.html', '10.ADMIN']
      ];
      navBar.innerHTML = '<span class="px-1.5 py-0.5 bg-[#b61722] text-white border border-black">10 M\u00C0N:</span>' +
        links.map(([url, label]) => {
          const active = currentPath.endsWith(url) ? 'bg-[#ffddb8] underline' : 'bg-white hover:bg-[#ffddb8]';
          return '<a href="' + url + '" class="px-1.5 py-0.5 border border-black ' + active + '">' + label + '</a>';
        }).join('') +
        '<button id="nxv-quick-profile-btn" class="px-1.5 py-0.5 bg-[#0058be] text-white border border-black hover:opacity-90">H\u1ED2 S\u01A0</button>';
      document.body.appendChild(navBar);
      document.getElementById('nxv-quick-profile-btn').onclick = () => openCharacterSheetModal();
    }
  }

  // 2. DONG BO HEADER HUD TU SQL SERVER
  function syncHeaderHud(dash) {
    if (!dash) return;
    window.__nxvDashboard = dash;
    const header = document.querySelector('header');
    if (!header) return;

    const lvlBadge = header.querySelector('.relative.w-12.h-12 span.absolute');
    if (lvlBadge) lvlBadge.textContent = dash.currentLevel;

    const nameEl = header.querySelector('.flex.items-center.gap-space-xs span.font-title-retro');
    if (nameEl) nameEl.textContent = dash.characterName || dash.username;

    const classEl = header.querySelector('.flex.items-center.gap-space-xs span.px-1\\.5');
    if (classEl) {
      const cName = dash.currentClassName || '';
      classEl.textContent = cName.toLowerCase().includes('knight')
        ? 'HI\u1EC6P S\u0128 TH\u1EC2 CH\u1EA4T'
        : 'PH\u00C1P S\u01AF C\u00D4NG NGH\u1EC6';
    }

    const subtitleEl = header.querySelector('.flex.flex-col > span.font-label-sm');
    if (subtitleEl && dash.currentTitleName) {
      subtitleEl.textContent = 'DANH HI\u1EC6U: ' + dash.currentTitleName.toUpperCase() + ' // STREAK: ' + (dash.streakDays || 0) + ' NG\u00C0Y';
    }

    const statBlocks = header.querySelectorAll('.hidden.xl\\:flex > div');
    if (statBlocks.length >= 3) {
      const hpText = statBlocks[0].querySelectorAll('span')[1];
      const hpBar = statBlocks[0].querySelector('.h-4 > div');
      const hpPct = dash.maxHp > 0 ? Math.min(100, Math.round((dash.currentHp / dash.maxHp) * 100)) : 100;
      if (hpText) hpText.textContent = dash.currentHp + '/' + dash.maxHp;
      if (hpBar) hpBar.style.width = hpPct + '%';

      const mpText = statBlocks[1].querySelectorAll('span')[1];
      const mpBar = statBlocks[1].querySelector('.h-4 > div');
      const mpPct = dash.maxMp > 0 ? Math.min(100, Math.round((dash.currentMp / dash.maxMp) * 100)) : 100;
      if (mpText) mpText.textContent = dash.currentMp + '/' + dash.maxMp;
      if (mpBar) mpBar.style.width = mpPct + '%';

      const expText = statBlocks[2].querySelectorAll('span')[1];
      const expBar = statBlocks[2].querySelector('.h-4 > div');
      const expPct = dash.expToNextLevel > 0 ? Math.min(100, Math.round((dash.currentExp / dash.expToNextLevel) * 100)) : 0;
      if (expText) expText.textContent = dash.currentExp + '/' + dash.expToNextLevel;
      if (expBar) expBar.style.width = expPct + '%';
    }

    const goldSpan = header.querySelector('span.font-stat-counter');
    if (goldSpan) {
      goldSpan.textContent = Number(dash.gold || 0).toLocaleString('vi-VN') + ' V\u00C0NG';
    }
  }

  // 3. LOGIC TUNG MAN HINH
  async function wireScreenSpecificApis(dash) {
    const path = window.location.pathname;

    // =========================================================================
    // MAN 1: AUTH & TAO NHAN VAT (login.html)
    // =========================================================================
    if (path.endsWith('/login.html')) {
      document.querySelectorAll('button').forEach(btn => {
        const txt = btn.innerText.toUpperCase();
        if (txt.includes('V\u00C0O') || txt.includes('X\u00C1C TH\u1EF0C') || txt.includes('KH\u1EDCI T\u1EA0O') || txt.includes('\u0110\u0102NG NH\u1EACP')) {
          btn.onclick = async (e) => {
            e.preventDefault();
            const inputs = document.querySelectorAll('input');
            let usernameOrEmail = 'thorin_archmage';
            let password = 'Password123!';
            inputs.forEach(inp => {
              if ((inp.type === 'text' || inp.type === 'email') && inp.value.trim().length >= 3) {
                usernameOrEmail = inp.value.trim();
              } else if (inp.type === 'password' && inp.value.trim().length >= 6) {
                password = inp.value.trim();
              }
            });
            try {
              const res = await apiCall('/auth/login', 'POST', { usernameOrEmail, password });
              setToken(res.accessToken);
              showRetroToast('\u0110\u0103ng nh\u1EADp th\u00E0nh c\u00F4ng! \u0110ang v\u00E0o B\u1EA3ng Nhi\u1EC7m V\u1EE5...', 'success');
              setTimeout(() => { window.location.href = '/app/dashboard.html'; }, 500);
            } catch (err) {
              const rand = Math.floor(1000 + Math.random() * 9000);
              const reg = await apiCall('/auth/register', 'POST', {
                username: usernameOrEmail.replace(/[^a-zA-Z0-9_]/g, '') + '_' + rand,
                email: 'hero_' + rand + '@tasktale.vn',
                password: password,
                gender: 0,
                characterName: usernameOrEmail.toUpperCase()
              });
              setToken(reg.accessToken);
              showRetroToast('\u0110\u00E3 kh\u1EDFi t\u1EA1o Nh\u00E2n V\u1EADt m\u1EDBi tr\u00EAn SQL Server!', 'success');
              setTimeout(() => { window.location.href = '/app/dashboard.html'; }, 500);
            }
          };
        }
      });
    }

    // =========================================================================
    // MAN 2: BANG NHIEM VU KANBAN (dashboard.html)
    // =========================================================================
    if (path.endsWith('/dashboard.html') || path === '/' || path === '/app/') {
      const cardsTodo = document.getElementById('cards-todo');
      const cardsProgress = document.getElementById('cards-progress');
      const cardsDone = document.getElementById('cards-done');

      const shieldFooterSpan = document.querySelector('footer span.bg-surface-container-lowest');
      if (shieldFooterSpan && dash) {
        shieldFooterSpan.textContent = dash.isShieldActive ? '\u0110\u00C3 B\u1EACT (1 L\u1EA6N)' : 'CH\u01AFA K\u00CDCH HO\u1EA0T';
      }

      const forgeBtn = document.getElementById('forge-quest-btn');
      if (forgeBtn && !document.getElementById('nxv-checkin-btn')) {
        const checkInBtn = document.createElement('button');
        checkInBtn.id = 'nxv-checkin-btn';
        checkInBtn.type = 'button';
        checkInBtn.className = 'flex items-center gap-1.5 px-space-md py-2.5 bg-tertiary-fixed text-on-tertiary-fixed border-2 border-black font-title-retro text-title-retro uppercase shadow-[4px_4px_0px_0px_#000000] hover:translate-x-[-2px] hover:translate-y-[-2px] active:translate-x-[2px] active:translate-y-[2px] transition-all';
        checkInBtn.innerHTML = '<span class="material-symbols-outlined text-[20px]">local_fire_department</span>' +
          '<span>\u0110I\u1EC2M DANH (' + (dash?.streakDays || 0) + ' NG\u00C0Y)</span>';
        checkInBtn.onclick = async () => {
          try {
            const res = await apiCall('/characters/me/check-in', 'POST');
            syncHeaderHud(res.dashboard);
            checkInBtn.innerHTML = '<span class="material-symbols-outlined text-[20px]">local_fire_department</span>' +
              '<span>STREAK: ' + res.streakDays + ' NG\u00C0Y \u2713</span>';
            showRetroToast('\u0110i\u1EC3m danh th\u00E0nh c\u00F4ng! Chu\u1ED7i Streak: ' + res.streakDays + ' ng\u00E0y (+' + res.expGained + ' EXP, +' + res.goldGained + ' V\u00C0NG)', 'success');
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        };
        forgeBtn.parentElement.insertBefore(checkInBtn, forgeBtn);
      }

      function inferCategory(title, colorLabel) {
        if (colorLabel && ['academic', 'health', 'coding', 'life'].includes(colorLabel)) return colorLabel;
        const t = (title || '').toLowerCase();
        if (t.includes('squat') || t.includes('ch\u1EA1y') || t.includes('cardio') || t.includes('gym') || t.includes('s\u1EE9c kh\u1ECFe')) return 'health';
        if (t.includes('code') || t.includes('api') || t.includes('rust') || t.includes('pr') || t.includes('sql') || t.includes('spring')) return 'coding';
        if (t.includes('s\u0103n') || t.includes('mua') || t.includes('\u0111\u1EDDi s\u1ED1ng')) return 'life';
        return 'academic';
      }

      function buildKanbanCardHtml(t, inProgressSet) {
        const rankLabel = t.difficulty === 3 ? 'S' : t.difficulty === 2 ? 'A' : 'B';
        const cat = inferCategory(t.title, t.colorLabel);
        const catLabel = cat === 'health' ? 'S\u1EE8C KH\u1ECEE' : cat === 'coding' ? 'L\u1EACP TR\u00CCNH' : cat === 'life' ? '\u0110\u1EDCI S\u1ED0NG' : 'H\u1ECCC THU\u1EACT';
        const isDone = t.status === 1;
        const isInProgress = !isDone && inProgressSet.has(t.taskId);

        const article = document.createElement('article');
        article.id = 'db-quest-' + t.taskId;
        article.setAttribute('data-task-id', t.taskId);
        article.setAttribute('data-category', cat);
        article.setAttribute('data-rank', rankLabel);
        article.className = 'quest-card flex flex-col bg-surface-container-lowest border-2 border-black shadow-[4px_4px_0px_0px_#000000] hover:translate-x-[-2px] hover:translate-y-[-2px] hover:shadow-[6px_6px_0px_0px_#000000] transition-all';

        article.innerHTML =
          '<div class="flex items-center justify-between px-space-md py-space-xs ' + (isDone ? 'bg-tertiary-fixed/40' : 'bg-surface-container') + ' border-b-2 border-black">' +
            '<div class="flex items-center gap-1.5">' +
              '<span class="px-1.5 py-0.5 ' + (isDone ? 'bg-tertiary-container text-on-tertiary' : 'bg-primary text-on-primary') + ' font-label-sm text-label-sm uppercase font-bold border border-black">' +
                (isDone ? '\u0110\u00C3 DI\u1EC6T' : (t.isFrozen ? '\u0110\u00D3NG B\u0102NG' : 'TASK #' + t.taskId)) +
              '</span>' +
              '<span class="px-1.5 py-0.5 bg-secondary-fixed text-on-secondary-fixed font-label-sm text-label-sm uppercase border border-black">' + catLabel + '</span>' +
            '</div>' +
            '<div class="flex items-center gap-1">' +
              '<div class="flex items-center gap-1 px-1.5 py-0.5 bg-primary-fixed border border-black">' +
                '<span class="font-title-retro text-title-retro text-primary font-bold">H\u1EA0NG ' + rankLabel + '</span>' +
              '</div>' +
              '<button type="button" class="nxv-btn-delete px-1.5 py-0.5 bg-surface-container-lowest border border-black text-on-surface hover:bg-error hover:text-white font-mono text-xs font-bold" title="X\u00F3a nhi\u1EC7m v\u1EE5">\u2715</button>' +
            '</div>' +
          '</div>' +
          '<div class="p-space-md flex flex-col gap-space-sm">' +
            '<h3 class="font-headline-sm text-headline-sm text-on-surface ' + (isDone ? 'line-through decoration-2 decoration-black' : '') + ' leading-tight">' + t.title + '</h3>' +
            '<p class="font-body-sm text-body-sm text-on-surface-variant">' + (t.descriptionMarkdown || 'Nhi\u1EC7m v\u1EE5 \u0111\u1ED3ng b\u1ED9 tr\u1EF1c ti\u1EBFp t\u1EEB SQL Server (NxvTasks).') + '</p>' +
            '<div class="grid grid-cols-2 gap-1.5 pt-1">' +
              '<div class="flex items-center gap-1 px-2 py-1 bg-tertiary-fixed border border-black text-on-tertiary-fixed shadow-[1px_1px_0px_0px_#000000]">' +
                '<span class="material-symbols-outlined text-[16px]">stars</span>' +
                '<span class="font-stat-counter text-stat-counter">+' + t.rewardExp + ' EXP</span>' +
              '</div>' +
              '<div class="flex items-center gap-1 px-2 py-1 bg-tertiary-fixed-dim border border-black text-on-tertiary-fixed shadow-[1px_1px_0px_0px_#000000]">' +
                '<span class="material-symbols-outlined text-[16px]">monetization_on</span>' +
                '<span class="font-stat-counter text-stat-counter">+' + t.rewardGold + ' V\u00C0NG</span>' +
              '</div>' +
              '<div class="col-span-2 flex items-center justify-between px-2 py-1 bg-error-container border border-black text-on-error-container shadow-[1px_1px_0px_0px_#000000]">' +
                '<div class="flex items-center gap-1">' +
                  '<span class="material-symbols-outlined text-error text-[16px]">heart_broken</span>' +
                  '<span class="font-label-md text-label-md font-bold text-error">-' + t.penaltyHp + ' HP PH\u1EA0T</span>' +
                '</div>' +
                '<button type="button" class="nxv-btn-move font-label-sm text-label-sm px-1.5 py-0.5 bg-surface-container-lowest border border-black font-bold text-secondary hover:bg-secondary hover:text-white">' +
                  (isDone ? '\u0110\u00C3 HO\u00C0N T\u1EA4T' : (isInProgress ? '\u2190 V\u1EC0 CH\u1EDC' : '\u25B6 CHI\u1EBEN \u0110\u1EA4U')) +
                '</button>' +
              '</div>' +
            '</div>' +
          '</div>' +
          '<div class="p-space-sm bg-surface-container border-t-2 border-black ' + (isDone ? 'flex items-center justify-between' : 'grid grid-cols-3 gap-1.5') + '">' +
            (isDone
              ? '<span class="font-label-sm text-label-sm text-on-surface-variant">\u0110\u00C3 DI\u1EC6T XONG</span>' +
                '<button type="button" class="nxv-btn-restore flex items-center gap-1 px-2.5 py-1 bg-surface-container-lowest border-2 border-black font-label-md text-label-md font-bold text-on-surface shadow-[2px_2px_0px_0px_#000000] hover:bg-surface-container-high">' +
                  '<span class="material-symbols-outlined text-[16px]">replay</span><span>KH\u00D4I PH\u1EE4C</span>' +
                '</button>'
              : '<button type="button" class="nxv-btn-slay flex items-center justify-center gap-1 py-1.5 bg-primary text-on-primary border-2 border-black font-label-md text-label-md font-bold shadow-[2px_2px_0px_0px_#000000]">' +
                  '<span class="material-symbols-outlined text-[16px]">swords</span><span>DI\u1EC6T</span>' +
                '</button>' +
                '<button type="button" class="nxv-btn-freeze flex items-center justify-center gap-1 py-1.5 ' + (t.isFrozen ? 'bg-secondary text-on-secondary' : 'bg-secondary-fixed text-on-secondary-fixed') + ' border-2 border-black font-label-md text-label-md font-bold shadow-[2px_2px_0px_0px_#000000]">' +
                  '<span class="material-symbols-outlined text-[16px]">' + (t.isFrozen ? 'lock' : 'ac_unit') + '</span><span>' + (t.isFrozen ? 'M\u1EDA B\u0102NG' : '\u0110\u00D3NG B\u0102NG') + '</span>' +
                '</button>' +
                '<button type="button" class="nxv-btn-skip flex items-center justify-center gap-1 py-1.5 bg-surface-container-lowest text-on-surface border-2 border-black font-label-md text-label-md font-bold shadow-[2px_2px_0px_0px_#000000] hover:bg-error-container">' +
                  '<span class="material-symbols-outlined text-[16px]">fast_forward</span><span>B\u1ECE QUA</span>' +
                '</button>'
            ) +
          '</div>';
        return article;
      }

      async function reloadKanbanFromDb() {
        if (!cardsTodo || !cardsProgress || !cardsDone) return;
        let dbTasks = await apiCall('/tasks-quests/tasks');

        if (!dbTasks || dbTasks.length === 0) {
          const seedList = [
            { title: 'CS402: H\u1EC7 ph\u00E2n t\u00E1n \u0110\u1ED3ng thu\u1EADn Raft', descriptionMarkdown: 'Tri\u1EC3n khai nh\u1ECBp tim b\u1EA7u ch\u1ECDn l\u00E3nh \u0111\u1EA1o v\u00E0 sao ch\u00E9p nh\u1EADt k\u00FD RPC.', colorLabel: 'academic', difficulty: 3, rewardExp: 350, rewardGold: 120, rewardMp: 15, penaltyHp: 30 },
            { title: 'Th\u00E0nh tr\u00EC Th\u00E9p: Giao th\u1EE9c Squat n\u1EB7ng 5x5', descriptionMarkdown: 'Kh\u1EDFi \u0111\u1ED9ng kim t\u1EF1 th\u00E1p v\u00E0 ghi l\u1EA1i d\u1EEF li\u1EC7u t\u1EADp luy\u1EC7n.', colorLabel: 'health', difficulty: 2, rewardExp: 280, rewardGold: 80, rewardMp: 10, penaltyHp: 20 },
            { title: 'T\u1ED1i \u01B0u API Spring Boot & Stored Procedures', descriptionMarkdown: 'Ki\u1EC3m \u0111\u1ECBnh 9 Modules Backend v\u00E0 b\u1EA3o m\u1EADt JWT.', colorLabel: 'coding', difficulty: 3, rewardExp: 400, rewardGold: 150, rewardMp: 20, penaltyHp: 25 },
            { title: '\u0110i s\u0103n nhu y\u1EBFu ph\u1EA9m tu\u1EA7n c\u1EE7a Bang', descriptionMarkdown: '\u1EE8c g\u00E0, tr\u1EE9ng, y\u1EBFn m\u1EA1ch v\u00E0 c\u00E0 ph\u00EA pha mana.', colorLabel: 'life', difficulty: 1, rewardExp: 110, rewardGold: 45, rewardMp: 10, penaltyHp: 10 }
          ];
          for (const item of seedList) {
            await apiCall('/tasks-quests/tasks', 'POST', item);
          }
          dbTasks = await apiCall('/tasks-quests/tasks');
          if (dbTasks.length >= 2) {
            const setInit = getInProgressSet();
            setInit.add(dbTasks[1].taskId);
            saveInProgressSet(setInit);
          }
        }

        const inProgressSet = getInProgressSet();
        cardsTodo.innerHTML = '';
        cardsProgress.innerHTML = '';
        cardsDone.innerHTML = '';

        let cTodo = 0, cProg = 0, cDone = 0;
        const catCounts = { all: 0, academic: 0, health: 0, coding: 0, life: 0 };

        dbTasks.forEach(t => {
          if (t.status === 3) return;
          const cat = inferCategory(t.title, t.colorLabel);
          catCounts.all++;
          if (catCounts[cat] !== undefined) catCounts[cat]++;

          const cardEl = buildKanbanCardHtml(t, inProgressSet);
          if (t.status === 1) {
            cardsDone.appendChild(cardEl);
            cDone++;
          } else if (inProgressSet.has(t.taskId)) {
            cardsProgress.appendChild(cardEl);
            cProg++;
          } else {
            cardsTodo.appendChild(cardEl);
            cTodo++;
          }
        });

        const pad2 = (n) => (n < 10 ? '0' : '') + n;
        const elTodo = document.getElementById('count-todo');
        const elProg = document.getElementById('count-progress');
        const elDone = document.getElementById('count-done');
        if (elTodo) elTodo.textContent = pad2(cTodo) + ' NHI\u1EC6M V\u1EE4';
        if (elProg) elProg.textContent = pad2(cProg) + ' \u0110ANG L\u00C0M';
        if (elDone) elDone.textContent = pad2(cDone) + ' \u0110\u00C3 HO\u00C0N T\u1EA4T';

        document.querySelectorAll('.filter-realm').forEach(btn => {
          const f = btn.getAttribute('data-filter');
          if (f === 'all') btn.textContent = 'T\u1EA4T C\u1EA2 (' + catCounts.all + ')';
          else if (f === 'academic') btn.innerHTML = '<span class="inline-block w-2 h-2 bg-secondary mr-1"></span>H\u1ECCC THU\u1EACT (' + catCounts.academic + ')';
          else if (f === 'health') btn.innerHTML = '<span class="inline-block w-2 h-2 bg-primary mr-1"></span>S\u1EE8C KH\u1ECEE (' + catCounts.health + ')';
          else if (f === 'coding') btn.innerHTML = '<span class="inline-block w-2 h-2 bg-tertiary-container mr-1"></span>L\u1EACP TR\u00CCNH (' + catCounts.coding + ')';
          else if (f === 'life') btn.innerHTML = '<span class="inline-block w-2 h-2 bg-inverse-surface mr-1"></span>\u0110\u1EDCI S\u1ED0NG (' + catCounts.life + ')';
        });
      }

      await reloadKanbanFromDb().catch(e => console.warn(e));

      const submitForgeBtn = document.getElementById('submit-forge-btn');
      const titleInput = document.getElementById('quest-title-input');
      const forgeModal = document.getElementById('forge-modal');
      if (submitForgeBtn && titleInput) {
        const cleanSubmit = submitForgeBtn.cloneNode(true);
        submitForgeBtn.parentNode.replaceChild(cleanSubmit, submitForgeBtn);
        cleanSubmit.addEventListener('click', async () => {
          const titleVal = titleInput.value.trim();
          if (!titleVal) { titleInput.focus(); return; }
          const selects = forgeModal ? forgeModal.querySelectorAll('select') : [];
          const catVal = selects[0] ? selects[0].value : 'coding';
          const rankVal = selects[1] ? selects[1].value : 'B';
          const diff = (rankVal === 'S' || rankVal === 'A') ? 3 : (rankVal === 'B' || rankVal === 'C') ? 2 : 1;
          const expMap = { E: 50, D: 100, C: 180, B: 250, A: 350, S: 500 };
          const exp = expMap[rankVal] || 250;
          const gold = Math.round(exp * 0.35);

          try {
            const created = await apiCall('/tasks-quests/tasks', 'POST', {
              title: titleVal,
              descriptionMarkdown: 'Nhi\u1EC7m v\u1EE5 r\u00E8n m\u1EDBi t\u1EEB B\u00E0n \u0110i\u1EC1u Khi\u1EC3n \u0110\u1EA1i Ph\u00E1p S\u01B0.',
              colorLabel: catVal,
              difficulty: diff,
              rewardExp: exp,
              rewardGold: gold,
              rewardMp: 15,
              penaltyHp: diff * 10
            });
            titleInput.value = '';
            if (forgeModal) { forgeModal.classList.add('hidden'); forgeModal.classList.remove('flex'); }
            await reloadKanbanFromDb();
            showRetroToast('\u0110\u00E3 r\u00E8n nhi\u1EC7m v\u1EE5 m\u1EDBi "' + created.title + '" (#' + created.taskId + ') v\u00E0o SQL Server!', 'success');
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        });
      }

      document.addEventListener('click', async (e) => {
        const card = e.target.closest('.quest-card');
        if (!card) return;
        const taskId = Number(card.getAttribute('data-task-id'));
        if (!taskId) return;

        const slayBtn = e.target.closest('.nxv-btn-slay');
        const freezeBtn = e.target.closest('.nxv-btn-freeze');
        const skipBtn = e.target.closest('.nxv-btn-skip');
        const restoreBtn = e.target.closest('.nxv-btn-restore');
        const moveBtn = e.target.closest('.nxv-btn-move');
        const delBtn = e.target.closest('.nxv-btn-delete');

        if (!slayBtn && !freezeBtn && !skipBtn && !restoreBtn && !moveBtn && !delBtn) return;
        e.stopPropagation();

        try {
          if (moveBtn) {
            const setProg = getInProgressSet();
            if (setProg.has(taskId)) setProg.delete(taskId);
            else setProg.add(taskId);
            saveInProgressSet(setProg);
            await reloadKanbanFromDb();
            return;
          }
          if (delBtn) {
            if (!confirm('X\u00F3a nhi\u1EC7m v\u1EE5 #' + taskId + ' kh\u1ECFi SQL Server?')) return;
            await apiCall('/tasks-quests/tasks/' + taskId, 'DELETE');
            await reloadKanbanFromDb();
            showRetroToast('\u0110\u00E3 x\u00F3a nhi\u1EC7m v\u1EE5 #' + taskId + '!', 'info');
            return;
          }
          if (slayBtn) {
            const res = await apiCall('/tasks-quests/tasks/' + taskId + '/complete', 'POST');
            syncHeaderHud(res.updatedDashboard);
            await reloadKanbanFromDb();
            const lvGained = res.spResult?.LevelsGained || res.spResult?.levelsGained || 0;
            showRetroToast('DI\u1EC6T TH\u00C0NH C\u00D4NG! +' + (res.spResult?.EXPGained || 250) + ' EXP, +' + (res.spResult?.GoldGained || 80) + ' V\u00C0NG!', 'success');
            if (lvGained > 0) {
              setTimeout(() => { window.location.href = '/app/levelup.html'; }, 900);
            }
          } else if (freezeBtn) {
            await apiCall('/tasks-quests/tasks/' + taskId + '/toggle-freeze', 'POST');
            await reloadKanbanFromDb();
            showRetroToast('\u0110\u00E3 c\u1EADp nh\u1EADt tr\u1EA1ng th\u00E1i \u0110\u00F3ng B\u0103ng cho Task #' + taskId + '!', 'info');
          } else if (skipBtn) {
            if (!confirm('D\u00F9ng MP \u0111\u1EC3 B\u1ECF qua an to\u00E0n nhi\u1EC7m v\u1EE5 n\u00E0y m\u00E0 kh\u00F4ng b\u1ECB tr\u1EEB HP?')) return;
            const res = await apiCall('/tasks-quests/tasks/' + taskId + '/skip', 'POST');
            await reloadKanbanFromDb();
            syncHeaderHud(await apiCall('/characters/me/dashboard'));
            showRetroToast('\u0110\u00E3 d\u00F9ng ' + res.mpSpent + ' MP \u0111\u1EC3 B\u1ECF qua an to\u00E0n Task #' + taskId + '!', 'info');
          } else if (restoreBtn) {
            await apiCall('/tasks-quests/tasks/' + taskId + '/undo', 'POST');
            await reloadKanbanFromDb();
            syncHeaderHud(await apiCall('/characters/me/dashboard'));
            showRetroToast('\u0110\u00E3 kh\u00F4i ph\u1EE5c (Undo) Task #' + taskId + ' v\u1EC1 danh s\u00E1ch ch\u1EDD!', 'info');
          }
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      }, true);
    }

    // =========================================================================
    // MAN 3: MODAL HOAN THANH & THANG CAP (levelup.html)
    // =========================================================================
    if (path.endsWith('/levelup.html')) {
      if (dash) {
        const prevLv = Math.max(1, (dash.currentLevel || 2) - 1);
        const lvBanner = document.querySelector('.bg-primary .font-headline-md');
        if (lvBanner) {
          lvBanner.textContent = 'TH\u0102NG C\u1EA4P! LV. ' + prevLv + ' \u2192 ' + dash.currentLevel + ' (' + (dash.characterName || 'THORIN') + ')';
        }
      }
      const continueBtn = document.getElementById('continue-adventure-btn');
      if (continueBtn) {
        const cleanBtn = continueBtn.cloneNode(true);
        continueBtn.parentNode.replaceChild(cleanBtn, continueBtn);
        cleanBtn.addEventListener('click', () => {
          window.location.href = '/app/dashboard.html';
        });
      }
    }

    // =========================================================================
    // MAN 4: NHIEM VU SU THI & SO CAI GPA DAI HOC (gpa.html)
    // =========================================================================
    if (path.endsWith('/gpa.html')) {
      let currentGpaQuestId = null;
      const subjectGrid = document.querySelector('section.flex.flex-col.gap-space-md > div.grid');

      function renderGpaSubjectsGrid(detail) {
        const bigGpaEl = document.querySelector('.font-headline-xl.text-primary');
        if (bigGpaEl && detail.currentCalculatedGpa != null) {
          bigGpaEl.textContent = Number(detail.currentCalculatedGpa).toFixed(2);
        }
        const subjects = detail.gpaSubjects || [];
        const totalCredits = subjects.filter(s => !s.isExcluded).reduce((acc, s) => acc + (s.credits || 0), 0);

        const statCounters = document.querySelectorAll('.font-stat-counter.font-bold');
        if (statCounters[0]) statCounters[0].textContent = totalCredits + ' T\u00CDN CH\u1EC8 T\u00CDNH GPA';
        if (statCounters[1]) statCounters[1].textContent = subjects.length + ' M\u00D4N H\u1ECCC';

        if (!subjectGrid) return;
        subjectGrid.innerHTML = '';

        subjects.forEach(s => {
          const isExcluded = Boolean(s.isExcluded);
          const card = document.createElement('article');
          card.className = isExcluded
            ? 'bg-surface-container border-2 border-black p-space-md shadow-[4px_4px_0px_#000000] flex flex-col justify-between gap-space-md opacity-90'
            : 'bg-surface-container-lowest border-2 border-black p-space-md shadow-[4px_4px_0px_#000000] flex flex-col justify-between gap-space-md hover:-translate-x-0.5 hover:-translate-y-0.5 transition-all';

          card.innerHTML =
            '<div class="flex flex-col gap-space-xs">' +
              '<div class="flex items-center justify-between border-b-2 border-black pb-2">' +
                '<div class="flex items-center gap-1">' +
                  '<span class="px-1.5 py-0.5 ' + (isExcluded ? 'bg-error text-on-error' : 'bg-primary text-on-primary') + ' border border-black font-label-sm text-label-sm uppercase font-bold">' +
                    (isExcluded ? 'LO\u1EA0I TR\u1EEC GPA' : 'M\u00D4N T\u00CDNH GPA') +
                  '</span>' +
                  '<span class="font-label-sm text-label-sm text-on-surface-variant">' + s.credits + ' T\u00CDN CH\u1EC8</span>' +
                '</div>' +
                '<span class="px-1.5 py-0.5 bg-tertiary-fixed border border-black font-label-sm text-label-sm uppercase font-bold text-on-tertiary-fixed">' +
                  'H\u1EA0NG ' + (s.letterGrade || 'A') + ' (' + (s.gradePoint4 ?? 4.0) + ')' +
                '</span>' +
              '</div>' +
              '<h3 class="font-headline-sm text-headline-sm uppercase text-on-surface mt-1 leading-snug">' + s.subjectName + '</h3>' +
              '<div class="mt-2 p-2 bg-surface-container border border-black flex flex-col gap-1">' +
                '<div class="flex justify-between font-label-sm text-label-sm">' +
                  '<span class="text-on-surface-variant">\u0110I\u1EC2M H\u1EC6 10:</span>' +
                  '<span class="font-bold text-on-surface">' + s.score + ' / 10.0</span>' +
                '</div>' +
                '<div class="flex justify-between font-label-sm text-label-sm">' +
                  '<span class="text-on-surface-variant">QUY \u0110\u1ED4I H\u1EC6 4:</span>' +
                  '<span class="font-bold text-primary">' + (s.gradePoint4 ?? 0) + ' / 4.0 (' + (s.letterGrade || 'F') + ')</span>' +
                '</div>' +
                (isExcluded ? '<div class="text-[11px] font-bold text-error mt-1">L\u00FD do lo\u1EA1i tr\u1EEB: ' + (s.excludeReason || 'M\u00F4n GDQP ho\u1EB7c \u0111i\u1EC3m F') + '</div>' : '') +
              '</div>' +
            '</div>' +
            '<div class="flex flex-col gap-space-sm pt-space-xs border-t-2 border-black">' +
              '<div class="grid grid-cols-2 gap-space-xs">' +
                '<button type="button" data-sub-id="' + s.subjectId + '" data-sub-name="' + s.subjectName + '" data-sub-cr="' + s.credits + '" class="nxv-gpa-edit py-1.5 bg-surface-container border-2 border-black font-title-retro text-label-md uppercase text-on-surface shadow-[2px_2px_0px_#000000] hover:bg-surface-container-high">[ S\u1EECA \u0110I\u1EC2M ]</button>' +
                '<button type="button" data-sub-id="' + s.subjectId + '" class="nxv-gpa-del py-1.5 bg-error-container border-2 border-black font-title-retro text-label-md uppercase text-on-error-container shadow-[2px_2px_0px_#000000] hover:bg-error hover:text-white">[ X\u00D3A M\u00D4N ]</button>' +
              '</div>' +
            '</div>';
          subjectGrid.appendChild(card);
        });
      }

      async function ensureGpaQuestLoaded() {
        let quests = await apiCall('/tasks-quests/epic-quests');
        let gpaQuest = quests.find(q => q.isGpaQuest);
        if (!gpaQuest) {
          gpaQuest = await apiCall('/tasks-quests/epic-quests', 'POST', {
            title: 'Chi\u1EBFn d\u1ECBch T\u1ED1t nghi\u1EC7p H\u1ECDc k\u1EF3 1',
            isGpaQuest: true,
            targetGpa: 3.60,
            rewardExp: 3450,
            rewardGold: 850
          });
          await apiCall('/tasks-quests/epic-quests/' + gpaQuest.epicQuestId + '/gpa-subjects', 'POST', { subjectName: 'CS402: Ki\u1EBFn tr\u00FAc H\u1EC7 th\u1ED1ng Ph\u00E2n t\u00E1n', credits: 3, score: 9.35 });
          await apiCall('/tasks-quests/epic-quests/' + gpaQuest.epicQuestId + '/gpa-subjects', 'POST', { subjectName: 'SE310: M\u1EABu Thi\u1EBFt k\u1EBF Ph\u1EA7n m\u1EC1m', credits: 3, score: 8.79 });
          await apiCall('/tasks-quests/epic-quests/' + gpaQuest.epicQuestId + '/gpa-subjects', 'POST', { subjectName: 'MA201: X\u00E1c su\u1EA5t & Qu\u00E1 tr\u00ECnh Ng\u1EABu nhi\u00EAn', credits: 4, score: 8.10 });
          await apiCall('/tasks-quests/epic-quests/' + gpaQuest.epicQuestId + '/gpa-subjects', 'POST', { subjectName: 'AI405: H\u1ECDc m\u00E1y & M\u1EA1ng N\u01A1-ron', credits: 3, score: 9.20 });
          await apiCall('/tasks-quests/epic-quests/' + gpaQuest.epicQuestId + '/gpa-subjects', 'POST', { subjectName: 'GDQP101: Gi\u00E1o d\u1EE5c Qu\u1ED1c ph\u00F2ng', credits: 2, score: 7.50 });
          await apiCall('/tasks-quests/epic-quests/' + gpaQuest.epicQuestId + '/gpa-subjects', 'POST', { subjectName: 'EE102: M\u1EA1ch T\u01B0\u01A1ng t\u1EF1 (\u0110i\u1EC3m F)', credits: 3, score: 4.20 });
        }
        currentGpaQuestId = gpaQuest.epicQuestId;
        const detail = await apiCall('/tasks-quests/epic-quests/' + currentGpaQuestId);
        renderGpaSubjectsGrid(detail);
        return detail;
      }

      await ensureGpaQuestLoaded().catch(() => {});

      document.addEventListener('click', async (e) => {
        const editBtn = e.target.closest('.nxv-gpa-edit');
        const delBtn = e.target.closest('.nxv-gpa-del');
        if (editBtn) {
          const subId = editBtn.getAttribute('data-sub-id');
          const subName = editBtn.getAttribute('data-sub-name');
          const subCr = parseInt(editBtn.getAttribute('data-sub-cr') || '3', 10);
          const newSc = parseFloat(prompt('Nh\u1EADp \u0111i\u1EC3m m\u1EDBi (h\u1EC7 10) cho m\u00F4n [' + subName + ']:', '9.0') || '');
          if (isNaN(newSc)) return;
          try {
            await apiCall('/tasks-quests/gpa-subjects/' + subId, 'DELETE');
            const detail = await apiCall('/tasks-quests/epic-quests/' + currentGpaQuestId + '/gpa-subjects', 'POST', {
              subjectName: subName,
              credits: subCr,
              score: newSc
            });
            renderGpaSubjectsGrid(detail);
            showRetroToast('\u0110\u00E3 c\u1EADp nh\u1EADt \u0111i\u1EC3m m\u00F4n "' + subName + '"! GPA m\u1EDBi: ' + Number(detail.currentCalculatedGpa).toFixed(2), 'success');
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        } else if (delBtn) {
          const subId = delBtn.getAttribute('data-sub-id');
          if (!confirm('X\u00F3a m\u00F4n h\u1ECDc n\u00E0y v\u00E0 t\u00EDnh l\u1EA1i GPA?')) return;
          try {
            const detail = await apiCall('/tasks-quests/gpa-subjects/' + subId, 'DELETE');
            renderGpaSubjectsGrid(detail);
            showRetroToast('\u0110\u00E3 x\u00F3a m\u00F4n h\u1ECDc! GPA m\u1EDBi: ' + Number(detail.currentCalculatedGpa || 0).toFixed(2), 'info');
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        }
      });

      document.querySelectorAll('button').forEach(btn => {
        const txt = btn.innerText.toUpperCase();
        if (txt.includes('T\u00CDNH TO\u00C1N L\u1EA0I')) {
          btn.onclick = async () => {
            const detail = await ensureGpaQuestLoaded();
            showRetroToast('NxvSp_CalculateGPA: GPA = ' + Number(detail.currentCalculatedGpa || 0).toFixed(2) + ' / 4.00 (\u0110\u00E3 t\u1EF1 \u0111\u1ED9ng lo\u1EA1i GDQP & \u0111i\u1EC3m F)!', 'success');
          };
        } else if (txt.includes('\u0110\u0102NG K\u00DD M\u00D4N H\u1ECCC M\u1EDCI')) {
          btn.onclick = async () => {
            const subName = prompt('Nh\u1EADp t\u00EAn m\u00F4n h\u1ECDc m\u1EDBi (G\u00F5 "GDQP" ho\u1EB7c \u0111i\u1EC3m < 4.0 \u0111\u1EC3 test t\u1EF1 \u0111\u1ED9ng lo\u1EA1i tr\u1EEB):', 'IT409: L\u1EADp tr\u00ECnh Java Spring Boot');
            if (!subName) return;
            const cr = parseInt(prompt('S\u1ED1 t\u00EDn ch\u1EC9:', '3') || '3', 10);
            const sc = parseFloat(prompt('\u0110i\u1EC3m t\u1ED5ng k\u1EBFt h\u1EC7 10 (>= 8.6 l\u00E0 \u0111i\u1EC3m A = 4.0):', '9.0') || '9.0');
            try {
              const detail = await apiCall('/tasks-quests/epic-quests/' + currentGpaQuestId + '/gpa-subjects', 'POST', {
                subjectName: subName,
                credits: cr,
                score: sc
              });
              renderGpaSubjectsGrid(detail);
              showRetroToast('\u0110\u00E3 th\u00EAm m\u00F4n "' + subName + '"! GPA m\u1EDBi: ' + Number(detail.currentCalculatedGpa || 0).toFixed(2) + ' / 4.00', 'success');
            } catch (e) {
              showRetroToast(e.message, 'error');
            }
          };
        } else if (txt.includes('XU\u1EA4T S\u1ED4 C\u00C1I B\u1EA2NG \u0110I\u1EC2M')) {
          btn.onclick = async () => {
            const detail = await ensureGpaQuestLoaded();
            const rows = [['MaMon', 'TenMonHoc', 'SoTinChi', 'DiemHe10', 'DiemChu', 'He4', 'TrangThai']];
            (detail.gpaSubjects || []).forEach(s => {
              rows.push([s.subjectId, '"' + s.subjectName + '"', s.credits, s.score, s.letterGrade, s.gradePoint4, s.isExcluded ? 'LOAI_TRU' : 'TINH_GPA']);
            });
            const csvContent = '\uFEFF' + rows.map(r => r.join(',')).join('\r\n');
            const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.download = 'BangDiem_GPA_TaskTale_' + Number(detail.currentCalculatedGpa || 3.72).toFixed(2) + '.csv';
            link.click();
            showRetroToast('\u0110\u00E3 xu\u1EA5t file CSV S\u1ED5 C\u00E1i B\u1EA3ng \u0110i\u1EC3m th\u00E0nh c\u00F4ng!', 'success');
          };
        }
      });
    }

    // =========================================================================
    // MAN 5: CAY KY NANG & LOP NHAN VAT (skills.html)
    // =========================================================================
    if (path.endsWith('/skills.html')) {
      async function refreshSkillTreeState() {
        try {
          const tree = await apiCall('/skills-classes/tree');
          const spBanner = document.querySelector('.bg-tertiary-fixed .font-stat-counter');
          if (spBanner) spBanner.textContent = tree.availableSkillPoints + ' \u0110I\u1EC2M CH\u01AFA D\u00D9NG';
        } catch (e) {}
      }
      await refreshSkillTreeState();

      document.querySelectorAll('section.w-full .grid > div').forEach((card, idx) => {
        card.addEventListener('click', async () => {
          const targetClassId = idx === 0 ? 1 : 2;
          try {
            const res = await apiCall('/skills-classes/classes/change', 'POST', { classId: targetClassId });
            showRetroToast('\u0110\u00E3 chuy\u1EC3n sang L\u1EDBp nh\u00E2n v\u1EADt: ' + res.currentClassName + '!', 'success');
            syncHeaderHud(await apiCall('/characters/me/dashboard'));
          } catch (e) {
            showRetroToast(e.message, 'error');
          }
        });
      });

      const castBtn = document.getElementById('cast-skill-btn');
      if (castBtn) {
        castBtn.addEventListener('click', async () => {
          const titleText = document.getElementById('inspect-title')?.innerText.toUpperCase() || '';
          const skillId = titleText.includes('KHI\u00CAN') ? 2 : 1;
          try {
            const tasks = await apiCall('/tasks-quests/tasks?status=0');
            let targetTaskId = tasks && tasks.length > 0 ? tasks[0].taskId : null;
            if (!targetTaskId) {
              const t = await apiCall('/tasks-quests/tasks', 'POST', { title: '\u0110\u1ED3 \u00E1n gi\u1EEFa k\u1EF3 CS301', difficulty: 2 });
              targetTaskId = t.taskId;
            }
            await apiCall('/skills-classes/skills/' + skillId + '/unlock', 'POST').catch(() => {});
            const res = await apiCall('/skills-classes/skills/' + skillId + '/cast', 'POST', { targetTaskId });
            showRetroToast(res.effectMessage, 'success');
            syncHeaderHud(await apiCall('/characters/me/dashboard'));
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        });
      }

      const upgradeBtn = document.getElementById('upgrade-skill-btn');
      if (upgradeBtn) {
        upgradeBtn.addEventListener('click', async () => {
          const titleText = document.getElementById('inspect-title')?.innerText.toUpperCase() || '';
          const skillId = titleText.includes('KHI\u00CAN') ? 2 : 1;
          try {
            await apiCall('/skills-classes/skills/' + skillId + '/unlock', 'POST').catch(async () => {
              await apiCall('/skills-classes/skills/' + skillId + '/upgrade', 'POST');
            });
            await refreshSkillTreeState();
            showRetroToast('\u0110\u00E3 n\u00E2ng b\u1EADc k\u1EF9 n\u0103ng [' + titleText + '] th\u00E0nh c\u00F4ng!', 'success');
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        });
      }

      const respecBtn = document.getElementById('respec-btn');
      if (respecBtn) {
        const cleanRespec = respecBtn.cloneNode(true);
        respecBtn.parentNode.replaceChild(cleanRespec, respecBtn);
        cleanRespec.addEventListener('click', async () => {
          if (!confirm('X\u00E1c nh\u1EADn d\u00F9ng 150 V\u00E0ng \u0111\u1EC3 Reset to\u00E0n b\u1ED9 C\u00E2y k\u1EF9 n\u0103ng?')) return;
          try {
            const tree = await apiCall('/skills-classes/tree/reset', 'POST');
            await refreshSkillTreeState();
            syncHeaderHud(await apiCall('/characters/me/dashboard'));
            showRetroToast('\u0110\u00E3 Reset C\u00E2y k\u1EF9 n\u0103ng! \u0110i\u1EC3m k\u1EF9 n\u0103ng hi\u1EC7n c\u00F3: ' + tree.availableSkillPoints + ' SP.', 'success');
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        });
      }
    }

    // =========================================================================
    // MAN 6: KHO DO, GACHA & CHO P2P (inventory.html) - DEEP DOM HYDRATION
    // =========================================================================
    if (path.endsWith('/inventory.html')) {
      let selectedDbItem = null;

      async function refreshFullInventoryScreen() {
        try {
          const [inv, banners, globalGacha, marketListings] = await Promise.all([
            apiCall('/inventory/me'),
            apiCall('/gacha-market/banners'),
            apiCall('/gacha-market/gacha/history/global'),
            apiCall('/gacha-market/marketplace/listings')
          ]);

          // 1. Cap nhat Gold & Pity Counter tren Top Sub-Header
          const subHeaderCounters = document.querySelectorAll('.font-stat-counter');
          if (subHeaderCounters[1]) subHeaderCounters[1].textContent = Number(inv.currentGold || 0).toLocaleString('vi-VN') + ' G';

          if (banners && banners.length > 0) {
            const b = banners[0];
            const pct = Math.min(100, Math.round((b.userCurrentPullCount / b.pityLimit) * 100));
            document.querySelectorAll('.font-stat-counter.text-primary').forEach(el => {
              if (el.innerText.includes('/')) {
                el.textContent = b.userCurrentPullCount + ' / ' + b.pityLimit + (el.innerText.includes('L\u1EA6N') ? ' L\u1EA6N QUAY (' + pct + '%)' : '');
              }
            });
            const pityBar = document.querySelector('#view-gacha .h-6 > div.bg-tertiary-fixed-dim');
            if (pityBar) pityBar.style.width = pct + '%';
          }

          // 2. Render Luoi 16 O Tui Do that tu SQL Server
          const gridContainer = document.querySelector('#view-gear .lg\\:col-span-7 .grid.grid-cols-4');
          const capacitySpan = document.querySelector('#view-gear .lg\\:col-span-7 span.font-label-sm.uppercase');
          const items = inv.items || [];
          if (capacitySpan) capacitySpan.textContent = 'S\u1EE8C CH\u1EE8A: ' + items.length + ' / ' + (inv.maxSlots || 50) + ' \u00D4';

          if (gridContainer) {
            gridContainer.innerHTML = '';
            items.forEach((entry, idx) => {
              const it = entry.item;
              if (!selectedDbItem && idx === 0) selectedDbItem = entry;
              const isSelected = selectedDbItem && selectedDbItem.item.itemId === it.itemId;
              const slotDiv = document.createElement('div');
              slotDiv.className = 'inv-slot cursor-pointer border-2 ' + (isSelected ? 'border-primary bg-primary-fixed' : 'border-black bg-surface hover:bg-surface-container-high') + ' p-2 transition-all shadow-[2px_2px_0px_#000000] flex flex-col items-center justify-center relative aspect-square';
              slotDiv.setAttribute('data-type', it.itemType === 0 ? 'WEAPON' : it.itemType === 1 ? 'CONSUMABLE' : 'RELIC');
              const iconName = it.itemType === 1 ? 'science' : (it.equipSlotId === 2 ? 'checkroom' : 'swords');
              slotDiv.innerHTML =
                '<span class="absolute top-1 left-1 font-label-sm text-label-sm font-bold text-primary">' + (entry.isEquipped ? '\u0110ANG M\u1EB6C' : 'S') + '</span>' +
                '<span class="absolute top-1 right-1 font-label-sm text-label-sm bg-black text-white px-1">x' + entry.quantity + '</span>' +
                '<span class="material-symbols-outlined text-[28px] text-primary">' + iconName + '</span>' +
                '<span class="font-label-sm text-label-sm uppercase truncate w-full text-center mt-1 font-bold">' + it.itemName + '</span>';
              slotDiv.onclick = () => {
                selectedDbItem = entry;
                updateInspectorFromDbItem(entry);
                refreshFullInventoryScreen();
              };
              gridContainer.appendChild(slotDiv);
            });

            // Cac o trong + O mo rong tui do (#83)
            const emptyCount = Math.max(2, 11 - items.length);
            for (let i = 0; i < emptyCount; i++) {
              const emp = document.createElement('div');
              emp.className = 'border-2 border-dashed border-black/40 p-2 bg-surface-container-low flex flex-col items-center justify-center relative aspect-square opacity-60';
              emp.innerHTML = '<span class="font-label-sm text-label-sm uppercase text-on-surface-variant">TR\u1ED0NG</span>';
              gridContainer.appendChild(emp);
            }

            const expBtn = document.createElement('div');
            expBtn.className = 'border-2 border-black p-2 bg-surface-container-high hover:bg-tertiary-fixed cursor-pointer transition-all flex flex-col items-center justify-center relative aspect-square shadow-[2px_2px_0px_#000000]';
            expBtn.innerHTML = '<span class="material-symbols-outlined text-[24px] text-tertiary">lock_open</span><span class="font-label-sm text-label-sm text-tertiary uppercase font-bold mt-1">+10 \u00D4 (200G)</span>';
            expBtn.onclick = async () => {
              if (!confirm('D\u00F9ng 200 V\u00E0ng \u0111\u1EC3 m\u1EDF r\u1ED9ng th\u00EAm +10 \u00F4 ch\u1EE9a T\u00FAi \u0111\u1ED3 (#83)?')) return;
              try {
                await apiCall('/inventory/expand', 'POST', { packs: 1 });
                await refreshFullInventoryScreen();
                syncHeaderHud(await apiCall('/characters/me/dashboard'));
                showRetroToast('\u0110\u00E3 m\u1EDF r\u1ED9ng th\u00EAm +10 \u00F4 ch\u1EE9a T\u00FAi \u0111\u1ED3!', 'success');
              } catch (err) {
                showRetroToast(err.message, 'error');
              }
            };
            gridContainer.appendChild(expBtn);
          }

          if (selectedDbItem) updateInspectorFromDbItem(selectedDbItem);

          // 3. Populate Select trong #sell-drawer va Danh sach Cho P2P
          const sellSelect = document.querySelector('#sell-drawer select');
          if (sellSelect && items.length > 0) {
            sellSelect.innerHTML = items.map(e =>
              '<option value="' + e.item.itemId + '">' + e.item.itemName + ' (SL: x' + e.quantity + ')</option>'
            ).join('');
          }

          // 4. Cap nhat Lich su Gacha toan Server
          const historyGrid = document.querySelector('#view-gacha .grid.sm\\:grid-cols-2.md\\:grid-cols-4');
          if (historyGrid && Array.isArray(globalGacha) && globalGacha.length > 0) {
            historyGrid.innerHTML = globalGacha.slice(0, 4).map(h =>
              '<div class="p-1.5 border border-black bg-surface flex items-center justify-between">' +
                '<span class="truncate">#' + h.gachaHistoryId + ' ' + h.itemName + '</span>' +
                '<span class="font-bold text-primary">' + (h.isPity ? '[PITY]' : '[' + h.characterName + ']') + '</span>' +
              '</div>'
            ).join('');
          }

          // 5. Render danh sach mat hang dang rao ban tren Cho P2P
          const marketListContainer = document.querySelector('#view-market .flex.flex-col.gap-space-xs');
          if (marketListContainer && Array.isArray(marketListings) && marketListings.length > 0) {
            let html = '<span class="font-label-sm text-label-sm text-on-surface-variant uppercase font-bold">V\u1EACT PH\u1EA8M \u0110ANG RAO B\u00C1N TR\u00CAN S\u00C0N P2P (SQL SERVER LIVE)</span>';
            marketListings.forEach(l => {
              const isMine = window.__nxvDashboard && l.sellerUserId === window.__nxvDashboard.userId;
              html +=
                '<div class="border-2 border-black bg-surface p-space-sm shadow-[2px_2px_0px_#000000] flex flex-col md:flex-row md:items-center justify-between gap-space-sm">' +
                  '<div class="flex items-center gap-space-sm">' +
                    '<div class="w-10 h-10 border-2 border-black bg-tertiary-fixed flex items-center justify-center">' +
                      '<span class="material-symbols-outlined text-tertiary text-[24px]">storefront</span>' +
                    '</div>' +
                    '<div class="flex flex-col">' +
                      '<span class="font-title-retro text-title-retro uppercase text-on-surface">' + l.item.itemName + ' (x' + l.quantity + ')</span>' +
                      '<span class="font-label-sm text-label-sm text-on-surface-variant">NG\u01AF\u1EDCI B\u00C1N: @' + l.sellerUsername + ' \u2022 M\u00C3 #' + l.listingId + '</span>' +
                    '</div>' +
                  '</div>' +
                  '<div class="flex items-center gap-space-md justify-between md:justify-end">' +
                    '<div class="flex flex-col items-end">' +
                      '<span class="font-stat-counter text-stat-counter text-tertiary">' + l.totalPrice + ' G</span>' +
                      '<span class="font-label-sm text-label-sm text-on-surface-variant">THU\u1EBE 5%: ' + l.estimatedTaxGold + ' G</span>' +
                    '</div>' +
                    (isMine
                      ? '<button onclick="window.nxvCancelListing(' + l.listingId + ')" class="px-space-md py-1.5 border-2 border-black bg-surface-container-high font-title-retro text-title-retro uppercase shadow-[2px_2px_0px_#000000]">G\u1EE0 B\u00C1N</button>'
                      : '<button onclick="window.nxvBuyListing(' + l.listingId + ',\'' + l.item.itemName + '\',' + l.totalPrice + ')" class="px-space-md py-1.5 border-2 border-black bg-primary text-on-primary font-title-retro text-title-retro uppercase shadow-[2px_2px_0px_#000000]">MUA NGAY</button>'
                    ) +
                  '</div>' +
                '</div>';
            });
            marketListContainer.innerHTML = html;
          }
        } catch (e) { console.warn('Refresh inventory err:', e); }
      }

      function updateInspectorFromDbItem(entry) {
        if (!entry || !entry.item) return;
        const it = entry.item;
        const tEl = document.getElementById('insp-title');
        const rEl = document.getElementById('insp-rank');
        const sEl = document.getElementById('insp-sub');
        const vEl = document.getElementById('insp-val');
        const st1 = document.getElementById('insp-stat1');
        const st2 = document.getElementById('insp-stat2');
        const btn = document.getElementById('insp-primary-action');

        if (tEl) tEl.textContent = it.itemName + ' (x' + entry.quantity + ')';
        if (rEl) rEl.textContent = entry.isEquipped ? '\u0110ANG M\u1EB6C' : 'V\u1EACT PH\u1EA8M DB';
        if (sEl) sEl.textContent = it.itemTypeLabel || 'V\u1EACT PH\u1EA8M';
        if (vEl) vEl.textContent = (it.sellPrice || 50) + ' G';
        if (st1) st1.textContent = it.itemType === 1 ? ('+' + it.healHp + ' H\u1ED2I HP') : ('+' + it.buffMaxHp + ' MAX HP / +' + it.buffMaxMp + ' MAX MP');
        if (st2) st2.textContent = '+' + Math.round((it.buffExpBonus || 0) * 100) + '% EXP / +' + Math.round((it.buffGoldBonus || 0) * 100) + '% V\u00C0NG';
        if (btn) {
          btn.textContent = it.itemType === 1 ? '[ S\u1EEC D\u1EE4NG B\u00CCNH THU\u1ED0C (H\u1ED2I HP) ]' : '[ M\u1EB6C TRANG B\u1ECA L\u00CAN NG\u01AF\u1EDCI ]';
        }

        // Them nut Ban Thu Hoi Vang (#84) canh nut Dang Ban Tren Cho
        const actionRow = btn ? btn.parentElement : null;
        if (actionRow && !document.getElementById('nxv-sell-npc-btn')) {
          const sellNpcBtn = document.createElement('button');
          sellNpcBtn.id = 'nxv-sell-npc-btn';
          sellNpcBtn.className = 'py-2 px-3 border-2 border-black bg-surface-container-high font-title-retro text-title-retro uppercase shadow-[3px_3px_0px_#000000]';
          sellNpcBtn.textContent = '[ B\u00C1N SHOP ]';
          sellNpcBtn.onclick = async () => {
            if (!selectedDbItem) return;
            try {
              const res = await apiCall('/inventory/sell', 'POST', { itemId: selectedDbItem.item.itemId, quantity: 1 });
              selectedDbItem = null;
              await refreshFullInventoryScreen();
              syncHeaderHud(await apiCall('/characters/me/dashboard'));
              showRetroToast('\u0110\u00E3 b\u00E1n 1x [' + res.soldItemName + '] thu v\u1EC1 +' + res.goldEarned + ' V\u00E0ng!', 'success');
            } catch (err) {
              showRetroToast(err.message, 'error');
            }
          };
          actionRow.appendChild(sellNpcBtn);
        }
      }

      await refreshFullInventoryScreen();

      window.performSummon = async function (pulls) {
        try {
          const banners = await apiCall('/gacha-market/banners');
          const bannerId = banners && banners.length > 0 ? banners[0].bannerId : 1;
          const endpoint = pulls === 10 ? ('/gacha-market/banners/' + bannerId + '/pull-x10') : ('/gacha-market/banners/' + bannerId + '/pull-x1');
          const res = await apiCall(endpoint, 'POST');
          const itemNames = res.pulledItems.map(p => '\u2022 ' + p.item.itemName + (p.isPityTriggered ? ' [\u2605 N\u1ED4 B\u1EA2O HI\u1EC2M PITY!]' : '')).join('\n');
          alert('\u2728 TRI\u1EC6U H\u1ED2I L\u00D2 R\u00C8N TH\u00C0NH C\u00D4NG (' + pulls + 'x)!\n\nV\u1EADt ph\u1EA9m nh\u1EADn \u0111\u01B0\u1EE3c v\u00E0o T\u00FAi \u0111\u1ED3:\n' + itemNames + '\n\nB\u1ED9 \u0111\u1EBFm B\u1EA3o Hi\u1EC3m Pity: ' + res.currentPityCounter + '/' + res.pityLimit + '\nKho V\u00E0ng c\u00F2n l\u1EA1i: ' + res.remainingGold + ' G');
          await refreshFullInventoryScreen();
          syncHeaderHud(await apiCall('/characters/me/dashboard'));
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      };

      window.handleEquipOrUse = async function () {
        try {
          const targetItemId = selectedDbItem ? selectedDbItem.item.itemId : 1;
          const isConsumable = selectedDbItem && selectedDbItem.item.itemType === 1;
          if (isConsumable) {
            await apiCall('/inventory/use-consumable', 'POST', { itemId: targetItemId });
            showRetroToast('\u0110\u00E3 u\u1ED1ng B\u00ECnh Thu\u1ED1c H\u1ED3i Ph\u1EE5c (NxvSp_UseConsumable)!', 'success');
          } else {
            await apiCall('/inventory/equip', 'POST', { itemId: targetItemId });
            showRetroToast('\u0110\u00E3 m\u1EB7c Trang b\u1ECB (NxvSp_EquipItem) v\u00E0 c\u1ED9ng Buff v\u00E0o MaxHP/MaxMP!', 'success');
          }
          await refreshFullInventoryScreen();
          syncHeaderHud(await apiCall('/characters/me/dashboard'));
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      };

      window.simulateUnequip = async function (slotName) {
        const slotId = slotName.includes('V\u0169 kh\u00ED') ? 1 : slotName.includes('Gi\u00E1p') ? 2 : 3;
        try {
          await apiCall('/inventory/unequip', 'POST', { slotId });
          await refreshFullInventoryScreen();
          syncHeaderHud(await apiCall('/characters/me/dashboard'));
          showRetroToast('\u0110\u00E3 th\u00E1o ' + slotName + ' (NxvSp_UnequipItem)!', 'info');
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      };

      window.confirmGiftSent = async function () {
        const recipient = document.getElementById('gift-recipient-input')?.value.trim();
        if (!recipient) { alert('Vui l\u00F2ng nh\u1EADp ID ng\u01B0\u1EDDi nh\u1EADn!'); return; }
        const receiverId = parseInt(recipient.replace(/\D/g, '') || '1', 10);
        const itemId = selectedDbItem ? selectedDbItem.item.itemId : 1;
        try {
          const res = await apiCall('/inventory/gift', 'POST', {
            receiverUserId: receiverId,
            itemId: itemId,
            quantity: 1,
            message: 'Qu\u00E0 t\u1EB7ng t\u1EEB Bang H\u1ED9i TaskTale!'
          });
          document.getElementById('gift-modal')?.classList.add('hidden');
          await refreshFullInventoryScreen();
          showRetroToast('\u0110\u00E3 t\u1EB7ng [' + res.itemName + '] cho User #' + res.receiverUserId + '!', 'success');
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      };

      window.confirmListing = async function () {
        const price = parseInt(document.getElementById('tax-calc-input')?.value || '200', 10);
        const selEl = document.querySelector('#sell-drawer select');
        const chosenItemId = selEl && selEl.value ? parseInt(selEl.value, 10) : (selectedDbItem ? selectedDbItem.item.itemId : null);
        if (!chosenItemId) {
          showRetroToast('H\u00E3y sang Tab 2 quay Gacha 1x \u0111\u1EC3 c\u00F3 v\u1EADt ph\u1EA9m tr\u01B0\u1EDBc khi \u0111\u0103ng b\u00E1n!', 'error');
          return;
        }
        try {
          const listing = await apiCall('/gacha-market/marketplace/listings', 'POST', {
            itemId: chosenItemId,
            quantity: 1,
            pricePerUnit: price
          });
          document.getElementById('sell-drawer')?.classList.add('hidden');
          await refreshFullInventoryScreen();
          showRetroToast('\u0110\u00E3 \u0111\u0103ng b\u00E1n [' + listing.item.itemName + '] gi\u00E1 ' + price + ' G l\u00EAn Ch\u1EE3 P2P!', 'success');
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      };

      window.nxvBuyListing = async function (listingId, name, price) {
        if (!confirm('X\u00E1c nh\u1EADn mua "' + name + '" v\u1EDBi gi\u00E1 ' + price + ' V\u00E0ng?')) return;
        try {
          const tx = await apiCall('/gacha-market/marketplace/listings/' + listingId + '/buy', 'POST');
          await refreshFullInventoryScreen();
          syncHeaderHud(await apiCall('/characters/me/dashboard'));
          showRetroToast('\u0110\u00E3 mua [' + tx.itemName + ']! Thu\u1EBF s\u00E0n 5%: ' + tx.serverTaxGold + ' G.', 'success');
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      };

      window.nxvCancelListing = async function (listingId) {
        try {
          await apiCall('/gacha-market/marketplace/listings/' + listingId + '/cancel', 'POST');
          await refreshFullInventoryScreen();
          showRetroToast('\u0110\u00E3 g\u1EE1 b\u00E0i \u0111\u0103ng b\u00E1n #' + listingId + ' v\u00E0 nh\u1EADn l\u1EA1i v\u1EADt ph\u1EA9m v\u1EC1 T\u00FAi \u0111\u1ED3!', 'info');
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      };
    }

    // =========================================================================
    // MAN 7: LINH THU & PHONG AP TRUNG (pets.html)
    // =========================================================================
    if (path.endsWith('/pets.html')) {
      let myPets = await apiCall('/pets-irl/pets/me').catch(() => []);
      if (!myPets || myPets.length === 0) {
        const adopted = await apiCall('/pets-irl/pets/adopt-egg', 'POST', { petName: 'C\u00C1O MECHA', elementType: 'L\u1EECA CYBER' }).catch(() => null);
        if (adopted) {
          await apiCall('/pets-irl/pets/' + adopted.userPetId + '/hatch', 'POST').catch(() => {});
          myPets = await apiCall('/pets-irl/pets/me').catch(() => []);
        }
      }
      const activePet = myPets && myPets.length > 0 ? myPets[0] : null;
      const activePetId = activePet ? activePet.userPetId : 1;

      function syncPetMeters(petObj) {
        if (!petObj) return;
        const counters = document.querySelectorAll('.lg\\:col-span-4 .font-stat-counter');
        const bars = document.querySelectorAll('.lg\\:col-span-4 .h-4.w-full > div');
        if (counters[0]) counters[0].textContent = petObj.hunger + ' / 100';
        if (bars[0]) bars[0].style.width = petObj.hunger + '%';
        if (counters[1]) counters[1].textContent = petObj.affection + ' / 100';
        if (bars[1]) bars[1].style.width = petObj.affection + '%';
      }
      syncPetMeters(activePet);

      // Goi AI Pet that khi bam 4 nut goi y
      window.updateDialogue = async function (preset) {
        try {
          const res = await apiCall('/pets-irl/pets/' + activePetId + '/ai-chat', 'POST', { userPrompt: preset });
          const dlg = document.getElementById('kitsune-dialogue');
          if (dlg && res.aiMessage) dlg.textContent = res.aiMessage;
          showRetroToast('KITSUNE.AI \u0111\u00E3 ph\u1EA3n h\u1ED3i ch\u1EE7 \u0111\u1EC1: ' + preset, 'success');
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      };

      const cols = document.querySelectorAll('.grid.lg\\:grid-cols-12 > div');
      document.querySelectorAll('section button.font-title-retro').forEach(btn => {
        const t = btn.innerText.toUpperCase();
        btn.addEventListener('click', async () => {
          cols.forEach(c => c.classList.remove('ring-4', 'ring-primary'));
          if (t.includes('\u0110\u1ED2NG H\u00C0NH') && cols[0]) {
            cols[0].classList.add('ring-4', 'ring-primary');
            cols[0].scrollIntoView({ behavior: 'smooth', block: 'center' });
          } else if (t.includes('PH\u00D2NG \u1EA4P TR\u1EE8NG') && cols[1]) {
            cols[1].classList.add('ring-4', 'ring-primary');
            cols[1].scrollIntoView({ behavior: 'smooth', block: 'center' });
          } else if (t.includes('T\u00C0I LI\u1EC6U LINH TH\u00DA') && cols[2]) {
            cols[2].classList.add('ring-4', 'ring-primary');
            cols[2].scrollIntoView({ behavior: 'smooth', block: 'center' });
          } else if (t.includes('HU\u1EA4N LUY\u1EC6N VI\u1EC4N CHINH')) {
            try {
              const res = await apiCall('/pets-irl/focus-sessions/complete', 'POST', { durationMinutes: 30 });
              syncHeaderHud(await apiCall('/characters/me/dashboard'));
              showRetroToast('Ho\u00E0n th\u00E0nh 30 ph\u00FAt T\u1EADp trung (NxvSp_CompleteFocusSession)! +' + res.expGained + ' EXP, +' + res.goldGained + ' V\u00C0NG!', 'success');
            } catch (err) {
              showRetroToast(err.message, 'error');
            }
          }
        });
      });

      const feedBtn = document.getElementById('btn-feed-pet');
      if (feedBtn) {
        feedBtn.addEventListener('click', async () => {
          try {
            const res = await apiCall('/pets-irl/pets/' + activePetId + '/feed', 'POST', { useGold: true });
            syncPetMeters(res.pet);
            showRetroToast(res.message, 'success');
            syncHeaderHud(await apiCall('/characters/me/dashboard'));
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        });
      }

      const brushBtn = document.getElementById('btn-pet-brush');
      if (brushBtn) {
        brushBtn.addEventListener('click', async () => {
          try {
            const res = await apiCall('/pets-irl/pets/' + activePetId + '/interact', 'POST');
            const dlg = document.getElementById('kitsune-dialogue');
            if (dlg && res.dialogue) dlg.textContent = '\u201C' + res.dialogue + '\u201D';
            showRetroToast('\u0110\u00E3 vu\u1ED1t ve Linh Th\u00FA (+5 T\u00ECnh c\u1EA3m & Trung th\u00E0nh)!', 'success');
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        });
      }

      const hatchBtn = document.getElementById('btn-hatch-egg');
      if (hatchBtn) {
        hatchBtn.addEventListener('click', async () => {
          try {
            const egg = await apiCall('/pets-irl/pets/adopt-egg', 'POST', { petName: 'R\u1ED2NG CYBER IGNIS', elementType: 'H\u1EC6 L\u1EECA' });
            const hatched = await apiCall('/pets-irl/pets/' + egg.userPetId + '/hatch', 'POST');
            showRetroToast('\u0110\u00E3 \u1EA5p n\u1EDF th\u00E0nh c\u00F4ng [' + hatched.petName + '] v\u00E0 m\u1EDF kh\u00F3a v\u00E0o Kho Linh Th\u00FA!', 'success');
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        });
      }

      const origSend = window.sendFamiliarMessage;
      window.sendFamiliarMessage = async function () {
        const input = document.getElementById('pet-prompt-input');
        const dialogueEl = document.getElementById('kitsune-dialogue');
        const promptText = input ? input.value.trim() : '';
        if (origSend) origSend();
        try {
          const res = await apiCall('/pets-irl/pets/' + activePetId + '/ai-chat', 'POST', { userPrompt: promptText });
          if (dialogueEl && res.aiMessage) dialogueEl.textContent = res.aiMessage;
          showRetroToast('KITSUNE.AI \u0111\u00E3 ph\u00E2n t\u00EDch d\u1EEF li\u1EC7u Task t\u1EEB SQL Server!', 'success');
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      };
    }

    // =========================================================================
    // MAN 8: TO DOI, PHAT MAU LIEN DOI, BOSS & BANG XEP HANG (party.html)
    // =========================================================================
    if (path.endsWith('/party.html')) {
      let myParties = await apiCall('/social-parties/parties/me').catch(() => []);
      if (!myParties || myParties.length === 0) {
        const created = await apiCall('/social-parties/parties', 'POST', {
          partyName: 'H\u1ED8I PH\u00C1P S\u01AF TASKTALE',
          description: 'T\u1ED5 \u0111\u1ED9i c\u00E0y \u0111\u1ED3 \u00E1n t\u1ED1t nghi\u1EC7p',
          maxMembers: 5
        }).catch(() => null);
        if (created) myParties = [created];
      }
      const party = myParties && myParties.length > 0 ? myParties[0] : null;
      const partyId = party ? party.partyId : 1;

      // Chen Thanh Dieu Khien To Doi Live (Share Task #95, Phat Mau Ca Doi #96, Danh Boss #97, Leaderboard #99)
      const mainContainer = document.querySelector('main > div');
      if (mainContainer && !document.getElementById('nxv-party-live-deck')) {
        const deck = document.createElement('section');
        deck.id = 'nxv-party-live-deck';
        deck.className = 'mb-6 p-4 bg-white border-2 border-black shadow-[4px_4px_0px_#000000] flex flex-col gap-3';
        deck.innerHTML =
          '<div class="flex flex-wrap items-center justify-between border-b-2 border-black pb-2 gap-2">' +
            '<span class="font-mono text-sm font-bold uppercase text-[#b61722]">\u2694\uFE0F B\u00C0N CH\u1EC8 HUY T\u1ED4 \u0110\u1ED8I LIVE (PARTY ID #' + partyId + ': ' + (party?.partyName || 'TASKTALE') + ')</span>' +
            '<div class="flex flex-wrap gap-2">' +
              '<button id="nxv-party-share-task" class="px-3 py-1 bg-[#0058be] text-white border-2 border-black font-mono text-xs font-bold shadow-[2px_2px_0px_#000]">[#95] GIAO TASK CHUNG TO\u00C0N \u0110\u1ED8I</button>' +
              '<button id="nxv-party-team-dmg" class="px-3 py-1 bg-[#b61722] text-white border-2 border-black font-mono text-xs font-bold shadow-[2px_2px_0px_#000]">[#96] TEST PH\u1EA0T TR\u1EEC HP C\u1EA2 \u0110\u1ED8I</button>' +
              '<button id="nxv-party-boss-win" class="px-3 py-1 bg-[#ffddb8] text-black border-2 border-black font-mono text-xs font-bold shadow-[2px_2px_0px_#000]">[#97] TI\u00CAU DI\u1EC6T BOSS & CHIA TH\u01AF\u1EDENG</button>' +
            '</div>' +
          '</div>' +
          '<div id="nxv-party-leaderboard-box" class="grid grid-cols-1 md:grid-cols-3 gap-3 font-mono text-xs"></div>';
        mainContainer.prepend(deck);

        document.getElementById('nxv-party-share-task').onclick = async () => {
          const title = prompt('Nh\u1EADp t\u00EAn Nhi\u1EC7m v\u1EE5 chung ph\u00E2n ph\u1ED1i cho to\u00E0n b\u1ED9 th\u00E0nh vi\u00EAn trong Party:', 'Sprint Chung: Ho\u00E0n thi\u1EC7n Slide B\u1EA3o v\u1EC7 \u0110ATN');
          if (!title) return;
          try {
            const res = await apiCall('/social-parties/parties/' + partyId + '/shared-tasks', 'POST', {
              title: title,
              difficulty: 3,
              rewardExp: 300,
              rewardGold: 150,
              penaltyHp: 20
            });
            window.__nxvLastSharedTaskId = res.rootPartyTaskId;
            showRetroToast('\u0110\u00E3 giao Task chung (#' + res.rootPartyTaskId + ') cho ' + res.assignedMembersCount + ' th\u00E0nh vi\u00EAn trong Party!', 'success');
          } catch (e) { showRetroToast(e.message, 'error'); }
        };

        document.getElementById('nxv-party-team-dmg').onclick = async () => {
          try {
            let taskId = window.__nxvLastSharedTaskId;
            if (!taskId) {
              const st = await apiCall('/social-parties/parties/' + partyId + '/shared-tasks', 'POST', {
                title: 'Task Chung Qu\u00E1 H\u1EA1n',
                difficulty: 2,
                penaltyHp: 15
              });
              taskId = st.rootPartyTaskId;
            }
            const res = await apiCall('/social-parties/parties/' + partyId + '/shared-tasks/' + taskId + '/trigger-team-penalty', 'POST');
            syncHeaderHud(await apiCall('/characters/me/dashboard'));
            showRetroToast('PH\u1EA0T LI\u00CAN \u0110\u1EDCI (#96): C\u1EA3 \u0111\u1ED9i \u0111\u00E3 b\u1ECB tr\u1EEB HP do 1 th\u00E0nh vi\u00EAn tr\u1EC5 Task chung!', 'error');
          } catch (e) { showRetroToast(e.message, 'error'); }
        };

        document.getElementById('nxv-party-boss-win').onclick = async () => {
          try {
            const boss = await apiCall('/social-parties/parties/' + partyId + '/boss-quests', 'POST', {
              title: 'Boss Tu\u1EA7n: R\u1ED3ng H\u1ED7n Mang Deadline',
              rewardExp: 500,
              rewardGold: 300
            });
            const res = await apiCall('/social-parties/parties/' + partyId + '/boss-quests/' + boss.epicQuestId + '/complete', 'POST');
            syncHeaderHud(await apiCall('/characters/me/dashboard'));
            showRetroToast('TI\u00CAU DI\u1EC6T BOSS TH\u00C0NH C\u00D4NG (#97)! C\u1EA3 \u0111\u1ED9i nh\u1EADn +500 EXP v\u00E0 +300 V\u00E0ng!', 'success');
          } catch (e) { showRetroToast(e.message, 'error'); }
        };

        // Load Top Leaderboard (#99, #100)
        try {
          const [lvTop, goldTop, taskTop] = await Promise.all([
            apiCall('/social-parties/leaderboards/levels'),
            apiCall('/social-parties/leaderboards/gold?period=ALL'),
            apiCall('/social-parties/leaderboards/tasks?period=ALL')
          ]);
          const lbBox = document.getElementById('nxv-party-leaderboard-box');
          if (lbBox) {
            const renderList = (arr, unit) => (arr || []).slice(0, 3).map(r =>
              '<div class="flex justify-between py-0.5"><span>#' + r.rank + ' ' + r.characterName + '</span><b>' + (unit === 'LV' ? ('Lv.' + r.currentLevel) : unit === 'G' ? (r.gold + ' G') : (r.totalTasksDone + ' Tasks')) + '</b></div>'
            ).join('');
            lbBox.innerHTML =
              '<div class="p-2 bg-[#faf2ee] border border-black"><div class="font-bold border-b border-black mb-1">\uD83C\uDFC6 TOP C\u1EA4P \u0110\u1ED8 (#99)</div>' + renderList(lvTop, 'LV') + '</div>' +
              '<div class="p-2 bg-[#faf2ee] border border-black"><div class="font-bold border-b border-black mb-1">\uD83E\uDE99 TOP PH\u00DA H\u1ED8 V\u00C0NG (#100)</div>' + renderList(goldTop, 'G') + '</div>' +
              '<div class="p-2 bg-[#faf2ee] border border-black"><div class="font-bold border-b border-black mb-1">\u2694\uFE0F TOP CHI\u1EBEN TH\u1EA6N TASK (#100)</div>' + renderList(taskTop, 'T') + '</div>';
          }
        } catch (e) {}
      }

      document.querySelectorAll('button').forEach(btn => {
        const txt = btn.innerText.toUpperCase();
        if (txt.includes('G\u1ECCI') || txt.includes('PH\u00C1T S\u00D3NG') || txt.includes('TRUY\u1EC0N TIN')) {
          btn.onclick = async () => {
            const inp = document.querySelector('input[type="text"]');
            const msg = inp && inp.value.trim() ? inp.value.trim() : 'Anh em t\u1EADp trung ho\u00E0n th\u00E0nh Sprint \u0111\u00FAng h\u1EA1n nh\u00E9!';
            try {
              await apiCall('/social-parties/parties/' + partyId + '/messages', 'POST', { content: msg });
              if (inp) inp.value = '';
              showRetroToast('\u0110\u00E3 g\u1EEBi tin nh\u1EAFn v\u00E0o K\u00EAnh Bang H\u1ED9i (#' + partyId + '): "' + msg + '"', 'success');
            } catch (e) { showRetroToast(e.message, 'error'); }
          };
        }
      });
    }

    // =========================================================================
    // MAN 9: HOI SINH TU TRAN VIETQR (revival.html) - ANH VIETQR DONG THAT
    // =========================================================================
    if (path.endsWith('/revival.html')) {
      try {
        await apiCall('/payment-revival/death-state/simulate-death', 'POST');
        const order = await apiCall('/payment-revival/orders', 'POST', { packageId: 2, paymentMethod: 'VIETQR' });
        window.__nxvCurrentOrderCode = order.orderCode;

        const transferSpan = document.querySelector('.select-all');
        if (transferSpan && order.transferContent) {
          transferSpan.textContent = order.transferContent;
        }

        // Thay SVG mock bang anh QR Dong VietQR.io that (#118)
        const qrSvg = document.querySelector('.w-36.h-36 svg');
        if (qrSvg && order.dynamicVietQrImageUrl) {
          const img = document.createElement('img');
          img.src = order.dynamicVietQrImageUrl;
          img.alt = 'VietQR Dynamic Code';
          img.className = 'w-full h-full object-contain p-1';
          qrSvg.parentNode.replaceChild(img, qrSvg);
        }

        showRetroToast('\u0110\u00E3 kh\u1EDFi t\u1EA1o \u0111\u01A1n H\u1ED3i Sinh VietQR: ' + order.orderCode + ' (50.000 VN\u0110)', 'info');

        const verifyBtn = document.getElementById('btn-verify');
        if (verifyBtn) {
          const newVerifyBtn = verifyBtn.cloneNode(true);
          verifyBtn.parentNode.replaceChild(newVerifyBtn, verifyBtn);
          newVerifyBtn.addEventListener('click', async () => {
            newVerifyBtn.disabled = true;
            newVerifyBtn.innerHTML = '<span class="material-symbols-outlined text-[20px] animate-spin">refresh</span><span>\u0110ANG X\u00C1C TH\u1EF0C WEBHOOK HMAC-SHA256 & H\u1ED2I SINH...</span>';
            try {
              const rev = await apiCall('/payment-revival/webhook/simulate-paid/' + window.__nxvCurrentOrderCode, 'POST');
              showRetroToast(rev.message, 'success');
              setTimeout(() => { window.location.href = '/app/dashboard.html'; }, 800);
            } catch (err) {
              showRetroToast(err.message, 'error');
              newVerifyBtn.disabled = false;
            }
          });
        }
      } catch (err) {
        console.warn('Revival init:', err);
      }
    }

    // =========================================================================
    // MAN 10: QUAN TRI HE THONG ADMIN (admin.html) - LIVE GM PANEL
    // =========================================================================
    if (path.endsWith('/admin.html')) {
      try {
        const [sum, users, logs] = await Promise.all([
          apiCall('/admin-system/dashboard/summary'),
          apiCall('/admin-system/users'),
          apiCall('/admin-system/logs/stat-logs')
        ]);

        const mainContainer = document.querySelector('main > div');
        if (mainContainer && !document.getElementById('nxv-admin-live-panel')) {
          const panel = document.createElement('section');
          panel.id = 'nxv-admin-live-panel';
          panel.className = 'mb-6 p-4 bg-white border-2 border-black shadow-[4px_4px_0px_#000000] flex flex-col gap-4';

          const userRows = (users || []).slice(0, 6).map(u =>
            '<tr class="border-b border-black/20">' +
              '<td class="py-1.5 px-2">#' + u.userId + ' <b>' + u.username + '</b> (' + (u.characterName || '') + ')</td>' +
              '<td class="py-1.5 px-2">Lv.' + u.currentLevel + ' | ' + u.gold + 'G</td>' +
              '<td class="py-1.5 px-2">' + u.roleLabel + '</td>' +
              '<td class="py-1.5 px-2">' + (u.isLocked ? '<span class="text-[#b61722] font-bold">B\u1ECA KH\u00D3A</span>' : '<span class="text-[#0058be] font-bold">HO\u1EA0T \u0110\u1ED8NG</span>') + '</td>' +
              '<td class="py-1.5 px-2 flex gap-1">' +
                '<button onclick="window.nxvToggleBan(' + u.userId + ',' + (!u.isLocked) + ')" class="px-2 py-0.5 border border-black bg-[#ffdad6] font-bold">' + (u.isLocked ? 'M\u1EDA KH\u00D3A' : 'BAN') + '</button>' +
                '<button onclick="window.nxvSetAdminRole(' + u.userId + ')" class="px-2 py-0.5 border border-black bg-[#d8e2ff] font-bold">\u0110\u1ED4I ROLE</button>' +
              '</td>' +
            '</tr>'
          ).join('');

          panel.innerHTML =
            '<div class="flex flex-wrap items-center justify-between border-b-2 border-black pb-2 gap-2">' +
              '<span class="font-mono text-sm font-bold uppercase text-[#b61722]">\u2699\uFE0F B\u1EA2NG QU\u1EA2N TR\u1ECA VI\u00CAN LIVE (SQL SERVER: ' + sum.totalUsers + ' USERS | ' + sum.totalTasks + ' TASKS | DOANH THU: ' + Number(sum.totalRevenueVnd || 0).toLocaleString('vi-VN') + ' VN\u0110)</span>' +
              '<div class="flex gap-2">' +
                '<button id="nxv-admin-broadcast-btn" class="px-3 py-1 bg-[#0058be] text-white border-2 border-black font-mono text-xs font-bold shadow-[2px_2px_0px_#000]">[#110] PH\u00C1T TH\u00D4NG B\u00C1O TO\u00C0N SERVER</button>' +
                '<button id="nxv-admin-cron-btn" class="px-3 py-1 bg-[#b61722] text-white border-2 border-black font-mono text-xs font-bold shadow-[2px_2px_0px_#000]">[#109] CH\u1EA0Y TO\u00C0N B\u1ED8 CRONJOBS</button>' +
              '</div>' +
            '</div>' +
            '<div class="overflow-x-auto">' +
              '<table class="w-full text-left font-mono text-xs border border-black">' +
                '<thead class="bg-[#f4ece8] border-b-2 border-black"><tr><th class="p-2">T\u00C0I KHO\u1EA2N</th><th class="p-2">CH\u1EC8 S\u1ED0</th><th class="p-2">PH\u00C2N QUY\u1EC0N (#113)</th><th class="p-2">TR\u1EA0NG TH\u00C1I (#104)</th><th class="p-2">THAO T\u00C1C</th></tr></thead>' +
                '<tbody>' + userRows + '</tbody>' +
              '</table>' +
            '</div>';

          mainContainer.prepend(panel);

          document.getElementById('nxv-admin-broadcast-btn').onclick = async () => {
            const title = prompt('Ti\u00EAu \u0111\u1EC1 Th\u00F4ng b\u00E1o to\u00E0n Server (#110):', 'B\u1EA2O TR\u00CC N\u00C2NG C\u1EA4P M\u00D9A 2');
            if (!title) return;
            const content = prompt('N\u1ED9i dung Th\u00F4ng b\u00E1o:', 'T\u1EA5t c\u1EA3 ng\u01B0\u1EDDi ch\u01A1i nh\u1EADn x2 EXP khi ho\u00E0n th\u00E0nh Task h\u00F4m nay!');
            await apiCall('/admin-system/notifications/send', 'POST', { title, content: content || title });
            showRetroToast('\u0110\u00E3 ph\u00E1t Global Notification t\u1EDBi to\u00E0n b\u1ED9 ng\u01B0\u1EDDi ch\u01A1i!', 'success');
          };

          document.getElementById('nxv-admin-cron-btn').onclick = async () => {
            await apiCall('/admin-system/cronjobs/run-all', 'POST');
            showRetroToast('\u0110\u00E3 th\u1EF1c thi Cronjob Qu\u00E9t Task Qu\u00E1 H\u1EA1n & Ki\u1EC3m Tra Pet Ng\u1EE7 \u0110\u00F4ng!', 'success');
          };
        }

        window.nxvToggleBan = async function (uid, lock) {
          await apiCall('/admin-system/users/' + uid + '/ban-toggle?lock=' + lock, 'POST');
          showRetroToast('\u0110\u00E3 c\u1EADp nh\u1EADt tr\u1EA1ng th\u00E1i Ban/Unban cho User #' + uid + '!', 'success');
          setTimeout(() => window.location.reload(), 500);
        };

        window.nxvSetAdminRole = async function (uid) {
          const r = parseInt(prompt('Nh\u1EADp Role m\u1EDBi cho User #' + uid + ' (0 = User, 1 = Moderator, 2 = SuperAdmin):', '2') || '2', 10);
          await apiCall('/admin-system/users/' + uid + '/role', 'PUT', { role: r });
          showRetroToast('\u0110\u00E3 \u0111\u1ED5i quy\u1EC1n User #' + uid + ' sang Role = ' + r + '!', 'success');
          setTimeout(() => window.location.reload(), 500);
        };
      } catch (err) { console.warn('Admin load:', err); }
    }
  }

  document.addEventListener('DOMContentLoaded', async () => {
    wireGlobalNavigation();
    try {
      const dash = await ensureAuthenticatedSession();
      syncHeaderHud(dash);
      await wireScreenSpecificApis(dash);
    } catch (e) {
      console.warn('TaskTale Bridge Init:', e);
    }
  });
})();
'@

[System.IO.File]::WriteAllText("$staticAppDir\tasktale-bridge.js", $bridgeJs, $utf8NoBom)
[System.IO.File]::WriteAllText("$targetAppDir\tasktale-bridge.js", $bridgeJs, $utf8NoBom)

Write-Host "==> DA NANG CAP TASKTALE-BRIDGE.JS LEN BAN v5.0 (DEEP DOM HYDRATION CHO CA 9 MODULES)!" -ForegroundColor Green