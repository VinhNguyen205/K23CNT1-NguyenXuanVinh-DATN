/**
 * TASKTALE / LIFERPG - LIVE BACKEND API BRIDGE (v2.2)
 * Ket noi truc tiep 10 man hinh 16-Bit Neobrutalism cua Stitch voi Spring Boot & SQL Server
 */
(function () {
  const API_BASE = '/api/v1';

  // 1. Bang dinh tuyen Sidebar & Header giua 10 man hinh Stitch
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
        showRetroToast('NHÃ‚N Váº¬T ÄÃƒ Tá»¬ TRáº¬N (HP = 0)! Äang chuyá»ƒn tá»›i cá»•ng Há»“i Sinh VietQR...', 'error');
        setTimeout(() => { window.location.href = '/app/revival.html'; }, 1200);
      }
      throw new Error(json.message || 'Lá»—i káº¿t ná»‘i mÃ¡y chá»§ (' + res.status + ')');
    }
    return json.data;
  }

  // Toast thong bao chuan 16-Bit Neobrutalism
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
      <span>âš¡ [SQL SERVER LIVE]: ${msg}</span>
      <button onclick="this.parentElement.parentElement.style.display='none'" class="px-1.5 py-0.5 bg-black text-white">âœ•</button>
    </div>`;
    box.style.display = 'block';
    clearTimeout(window.__nxvToastTimer);
    window.__nxvToastTimer = setTimeout(() => { box.style.display = 'none'; }, 4500);
  }

  // Tu dong dang nhap hoac khoi tao tai khoan Demo tren SQL Server neu chua co token
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

  // 2. Gan su kien chuyen trang cho toan bo Sidebar & Header + Thanh Quick-Nav
  function wireGlobalNavigation() {
    document.querySelectorAll('aside nav a').forEach(a => {
      const dp = a.getAttribute('data-path');
      if (dp && ROUTE_MAP[dp]) {
        a.setAttribute('href', ROUTE_MAP[dp]);
        a.addEventListener('click', (e) => {
          e.preventDefault();
          window.location.href = ROUTE_MAP[dp];
        });
      }
    });

    // Nut [HOI SINH] va [Avatar] tren Header
    document.querySelectorAll('header button, header div').forEach(el => {
      if (el.innerText && el.innerText.trim() === 'Há»’I SINH' && el.tagName === 'BUTTON') {
        el.addEventListener('click', () => {
          window.location.href = '/app/revival.html';
        });
      }
      if (el.querySelector && el.querySelector('span.material-symbols-outlined')?.textContent.trim() === 'person') {
        el.style.cursor = 'pointer';
        el.title = 'Chuyá»ƒn tá»›i mÃ n hÃ¬nh TÃ i khoáº£n & Táº¡o nhÃ¢n váº­t';
        el.addEventListener('click', () => {
          window.location.href = '/app/login.html';
        });
      }
    });

    // Thanh dieu huong nhanh 10 man hinh o goc duoi ben trai (giup demo bao ve do an cuc nhanh)
    const navBar = document.createElement('div');
    navBar.className = 'fixed bottom-3 left-3 z-[9990] flex flex-wrap items-center gap-1 bg-white border-2 border-black p-1.5 shadow-[4px_4px_0px_0px_#000000] font-mono text-[10px] font-bold uppercase';
    navBar.innerHTML = `
      <span class="px-1.5 py-0.5 bg-[#b61722] text-white border border-black">10 MÃ€N HÃŒNH:</span>
      <a href="/app/login.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">1.AUTH</a>
      <a href="/app/dashboard.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">2.KANBAN</a>
      <a href="/app/levelup.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">3.LEVEL-UP</a>
      <a href="/app/gpa.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">4.GPA</a>
      <a href="/app/skills.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">5.SKILLS</a>
      <a href="/app/inventory.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">6.GACHA&CHá»¢</a>
      <a href="/app/pets.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">7.LINH THÃš</a>
      <a href="/app/party.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">8.Tá»” Äá»˜I</a>
      <a href="/app/revival.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">9.VIETQR</a>
      <a href="/app/admin.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">10.ADMIN</a>
    `;
    document.body.appendChild(navBar);
  }

  // 3. Dong bo chi so Nhan vat that tu SQL Server len Header HUD
  function syncHeaderHud(dash) {
    if (!dash) return;
    window.__nxvDashboard = dash;
    const header = document.querySelector('header');
    if (!header) return;

    // Cap nhat Level badge
    const lvlBadge = header.querySelector('.relative.w-12.h-12 span.absolute');
    if (lvlBadge) lvlBadge.textContent = dash.currentLevel;

    // Cap nhat Ten nhan vat & Class
    const nameEl = header.querySelector('.flex.items-center.gap-space-xs span.font-title-retro');
    if (nameEl) nameEl.textContent = dash.characterName || dash.username;
    const classEl = header.querySelector('.flex.items-center.gap-space-xs span.px-1\\.5');
    if (classEl) classEl.textContent = dash.currentClassName || 'Äáº I PHÃP SÆ¯';

    // Cap nhat HP, MP, EXP bars tren Header
    const statBlocks = header.querySelectorAll('.hidden.xl\\:flex > div');
    if (statBlocks.length >= 3) {
      // HP
      const hpText = statBlocks[0].querySelectorAll('span')[1];
      const hpBar = statBlocks[0].querySelector('.h-4 > div');
      const hpPct = dash.maxHp > 0 ? Math.min(100, Math.round((dash.currentHp / dash.maxHp) * 100)) : 100;
      if (hpText) hpText.textContent = `${dash.currentHp}/${dash.maxHp}`;
      if (hpBar) hpBar.style.width = `${hpPct}%`;

      // MP
      const mpText = statBlocks[1].querySelectorAll('span')[1];
      const mpBar = statBlocks[1].querySelector('.h-4 > div');
      const mpPct = dash.maxMp > 0 ? Math.min(100, Math.round((dash.currentMp / dash.maxMp) * 100)) : 100;
      if (mpText) mpText.textContent = `${dash.currentMp}/${dash.maxMp}`;
      if (mpBar) mpBar.style.width = `${mpPct}%`;

      // EXP
      const expText = statBlocks[2].querySelectorAll('span')[1];
      const expBar = statBlocks[2].querySelector('.h-4 > div');
      const expPct = dash.expToNextLevel > 0 ? Math.min(100, Math.round((dash.currentExp / dash.expToNextLevel) * 100)) : 0;
      if (expText) expText.textContent = `${dash.currentExp}/${dash.expToNextLevel}`;
      if (expBar) expBar.style.width = `${expPct}%`;
    }

    // Cap nhat Gold
    const goldSpan = header.querySelector('span.font-stat-counter');
    if (goldSpan) {
      goldSpan.textContent = Number(dash.gold || 0).toLocaleString('vi-VN') + ' VÃ€NG';
    }
  }

  // 4. Kich hoat Logic API rieng cho tung man hinh
  async function wireScreenSpecificApis(dash) {
    const path = window.location.pathname;

    // --- MAN HINH 1: LOGIN & REGISTER (login.html) ---
    if (path.endsWith('/login.html')) {
      document.querySelectorAll('button, form').forEach(el => {
        if (el.tagName === 'BUTTON' && (el.innerText.includes(' VÃ€O ') || el.innerText.includes('XÃC THá»°C') || el.innerText.includes('KHá»žI Táº O') || el.innerText.includes('ÄÄ‚NG NHáº¬P'))) {
          el.addEventListener('click', async () => {
            const inputs = document.querySelectorAll('input');
            let usernameOrEmail = 'thorin_archmage';
            let password = 'Password123!';
            inputs.forEach(inp => {
              if (inp.type === 'text' || inp.type === 'email') {
                if (inp.value.trim()) usernameOrEmail = inp.value.trim();
              } else if (inp.type === 'password') {
                if (inp.value.trim()) password = inp.value.trim();
              }
            });
            try {
              const res = await apiCall('/auth/login', 'POST', { usernameOrEmail, password });
              setToken(res.accessToken);
              showRetroToast('ÄÄƒng nháº­p thÃ nh cÃ´ng! Äang tiáº¿n vÃ o Äáº¡i Sáº£nh...', 'success');
              setTimeout(() => { window.location.href = '/app/dashboard.html'; }, 700);
            } catch (err) {
              try {
                const rand = Math.floor(1000 + Math.random() * 9000);
                const reg = await apiCall('/auth/register', 'POST', {
                  username: usernameOrEmail.replace(/[^a-zA-Z0-9_]/g, '') + '_' + rand,
                  email: 'hero_' + rand + '@tasktale.vn',
                  password: password,
                  gender: 0,
                  characterName: usernameOrEmail.toUpperCase()
                });
                setToken(reg.accessToken);
                showRetroToast('ÄÃ£ rÃ¨n NhÃ¢n Váº­t má»›i trÃªn SQL Server! Äang vÃ o Äáº¡i Sáº£nh...', 'success');
                setTimeout(() => { window.location.href = '/app/dashboard.html'; }, 700);
              } catch (e2) {
                showRetroToast(e2.message, 'error');
              }
            }
          });
        }
      });
    }

    // --- MAN HINH 2: KANBAN NHIEM VU COT LOI (dashboard.html) ---
    if (path.endsWith('/dashboard.html') || path === '/' || path === '/app/') {
      const cardsTodo = document.getElementById('cards-todo');
      const cardsDone = document.getElementById('cards-done');

      // Load cac task that tu SQL Server va chen len dau cot Kanban
      try {
        const dbTasks = await apiCall('/tasks-quests/tasks');
        if (Array.isArray(dbTasks) && cardsTodo && cardsDone) {
          dbTasks.forEach(t => {
            if (document.getElementById('db-quest-' + t.taskId)) return;
            const rankLabel = t.difficulty === 3 ? 'S' : t.difficulty === 2 ? 'A' : 'B';
            const card = document.createElement('article');
            card.id = 'db-quest-' + t.taskId;
            card.setAttribute('data-task-id', t.taskId);
            card.setAttribute('data-category', 'coding');
            card.setAttribute('data-rank', rankLabel);
            card.className = 'quest-card flex flex-col bg-surface-container-lowest border-2 border-black shadow-[4px_4px_0px_0px_#000000] hover:translate-x-[-2px] hover:translate-y-[-2px] hover:shadow-[6px_6px_0px_0px_#000000] transition-all';
            const isDone = t.status === 1;
            card.innerHTML = `
              <div class="flex items-center justify-between px-space-md py-space-xs bg-surface-container border-b-2 border-black">
                <div class="flex items-center gap-1.5">
                  <span class="px-1.5 py-0.5 bg-primary text-on-primary font-label-sm text-label-sm uppercase font-bold border border-black">SQL TASK #${t.taskId}</span>
                  <span class="px-1.5 py-0.5 bg-secondary-fixed text-on-secondary-fixed font-label-sm text-label-sm uppercase border border-black">${t.isFrozen ? 'ÄÃƒ ÄÃ“NG BÄ‚NG' : 'NHIá»†M Vá»¤ THáº¬T'}</span>
                </div>
                <div class="flex items-center gap-1 px-1.5 py-0.5 bg-primary-fixed border border-black">
                  <span class="font-title-retro text-title-retro text-primary font-bold">Háº NG ${rankLabel}</span>
                </div>
              </div>
              <div class="p-space-md flex flex-col gap-space-sm">
                <h3 class="font-headline-sm text-headline-sm text-on-surface ${isDone ? 'line-through decoration-2 decoration-black' : ''} leading-tight">${t.title}</h3>
                <p class="font-body-sm text-body-sm text-on-surface-variant">${t.descriptionMarkdown || 'Nhiá»‡m vá»¥ Ä‘á»“ng bá»™ trá»±c tiáº¿p tá»« báº£ng NxvTasks (SQL Server v2.2).'}</p>
                <div class="grid grid-cols-2 gap-1.5 pt-1">
                  <div class="flex items-center gap-1 px-2 py-1 bg-tertiary-fixed border border-black text-on-tertiary-fixed">
                    <span class="material-symbols-outlined text-[16px]">stars</span>
                    <span class="font-stat-counter text-stat-counter">+${t.rewardExp} EXP</span>
                  </div>
                  <div class="flex items-center gap-1 px-2 py-1 bg-tertiary-fixed-dim border border-black text-on-tertiary-fixed">
                    <span class="material-symbols-outlined text-[16px]">monetization_on</span>
                    <span class="font-stat-counter text-stat-counter">+${t.rewardGold} VÃ€NG</span>
                  </div>
                </div>
              </div>
              <div class="p-space-sm bg-surface-container border-t-2 border-black ${isDone ? 'flex items-center justify-between' : 'grid grid-cols-3 gap-1.5'}">
                ${isDone ? `
                  <span class="font-label-sm text-label-sm text-on-surface-variant">ÄÃƒ LÆ¯U SQL</span>
                  <button class="action-btn-restore flex items-center gap-1 px-2.5 py-1 bg-surface-container-lowest border-2 border-black font-label-md text-label-md font-bold text-on-surface shadow-[2px_2px_0px_0px_#000000]">
                    <span class="material-symbols-outlined text-[16px]">replay</span><span>KHÃ”I PHá»¤C</span>
                  </button>
                ` : `
                  <button class="action-btn-slay flex items-center justify-center gap-1 py-1.5 bg-primary text-on-primary border-2 border-black font-label-md text-label-md font-bold shadow-[2px_2px_0px_0px_#000000]">
                    <span class="material-symbols-outlined text-[16px]">swords</span><span>DIá»†T</span>
                  </button>
                  <button class="action-btn-freeze flex items-center justify-center gap-1 py-1.5 bg-secondary-fixed text-on-secondary-fixed border-2 border-black font-label-md text-label-md font-bold shadow-[2px_2px_0px_0px_#000000]">
                    <span class="material-symbols-outlined text-secondary text-[16px]">ac_unit</span><span>${t.isFrozen ? 'Má»ž BÄ‚NG' : 'ÄÃ“NG BÄ‚NG'}</span>
                  </button>
                  <button class="action-btn-skip flex items-center justify-center gap-1 py-1.5 bg-surface-container-lowest text-on-surface border-2 border-black font-label-md text-label-md font-bold shadow-[2px_2px_0px_0px_#000000]">
                    <span class="material-symbols-outlined text-[16px]">fast_forward</span><span>Bá»Ž QUA</span>
                  </button>
                `}
              </div>
            `;
            if (isDone) cardsDone.prepend(card);
            else cardsTodo.prepend(card);
          });
        }
      } catch (e) {
        console.warn('Load tasks warning:', e);
      }

      // Khi bam nut KHAC LEN CUON GIAY (#submit-forge-btn) -> Luu Task that vao SQL Server
      const submitForgeBtn = document.getElementById('submit-forge-btn');
      const titleInput = document.getElementById('quest-title-input');
      if (submitForgeBtn && titleInput) {
        submitForgeBtn.addEventListener('click', async () => {
          const titleVal = titleInput.value.trim();
          if (!titleVal) return;
          try {
            const created = await apiCall('/tasks-quests/tasks', 'POST', {
              title: titleVal,
              difficulty: 3,
              rewardExp: 250,
              rewardGold: 80,
              rewardMp: 15,
              penaltyHp: 20
            });
            const newestCard = cardsTodo?.querySelector('.quest-card');
            if (newestCard && !newestCard.getAttribute('data-task-id')) {
              newestCard.setAttribute('data-task-id', created.taskId);
            }
            showRetroToast(`ÄÃ£ lÆ°u nhiá»‡m vá»¥ "${created.title}" (TaskID=${created.taskId}) vÃ o SQL Server!`, 'success');
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        }, true);
      }

      // Bat su kien DIET / DONG BANG / BO QUA / KHOI PHUC tren Kanban goi Stored Procedure
      document.addEventListener('click', async (e) => {
        const card = e.target.closest('.quest-card');
        if (!card) return;
        let taskId = card.getAttribute('data-task-id');

        const slayBtn = e.target.closest('.action-btn-slay');
        const freezeBtn = e.target.closest('.action-btn-freeze');
        const skipBtn = e.target.closest('.action-btn-skip');
        const restoreBtn = e.target.closest('.action-btn-restore');

        if (!slayBtn && !freezeBtn && !skipBtn && !restoreBtn) return;

        try {
          // Neu la card mau cua Stitch chua co trong DB thi tu dong tao trong DB truoc khi goi SP
          if (!taskId) {
            const titleEl = card.querySelector('h3');
            const created = await apiCall('/tasks-quests/tasks', 'POST', {
              title: titleEl ? titleEl.innerText.trim() : 'Nhiá»‡m vá»¥ Chiáº¿n dá»‹ch',
              difficulty: 3,
              rewardExp: 350,
              rewardGold: 120,
              rewardMp: 15,
              penaltyHp: 25
            });
            taskId = created.taskId;
            card.setAttribute('data-task-id', taskId);
          }

          if (slayBtn) {
            const res = await apiCall(`/tasks-quests/tasks/${taskId}/complete`, 'POST');
            syncHeaderHud(res.updatedDashboard);
            const lvGained = res.spResult?.LevelsGained || res.spResult?.levelsGained || 0;
            showRetroToast(`DIá»†T THÃ€NH CÃ”NG (Task #${taskId})! +${res.spResult?.EXPGained || 350} EXP, +${res.spResult?.GoldGained || 120} VÃ€NG!`, 'success');
            if (lvGained > 0) {
              setTimeout(() => { window.location.href = '/app/levelup.html'; }, 1000);
            }
          } else if (freezeBtn) {
            await apiCall(`/tasks-quests/tasks/${taskId}/toggle-freeze`, 'POST');
            showRetroToast(`ÄÃ£ cáº­p nháº­t tráº¡ng thÃ¡i ÄÃ³ng BÄƒng cho Task #${taskId}!`, 'info');
          } else if (skipBtn) {
            const res = await apiCall(`/tasks-quests/tasks/${taskId}/skip`, 'POST');
            showRetroToast(`ÄÃ£ dÃ¹ng ${res.mpSpent} MP Ä‘á»ƒ Bá» qua an toÃ n Task #${taskId}!`, 'info');
            const freshDash = await apiCall('/characters/me/dashboard');
            syncHeaderHud(freshDash);
          } else if (restoreBtn) {
            await apiCall(`/tasks-quests/tasks/${taskId}/undo`, 'POST');
            showRetroToast(`ÄÃ£ hoÃ n tÃ¡c (Undo) Task #${taskId} vá» danh sÃ¡ch chá»!`, 'info');
            const freshDash = await apiCall('/characters/me/dashboard');
            syncHeaderHud(freshDash);
          }
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      });
    }

    // --- MAN HINH 3: MODAL HOAN THANH & THANG CAP (levelup.html) ---
    if (path.endsWith('/levelup.html')) {
      const continueBtn = document.getElementById('continue-adventure-btn');
      if (continueBtn) {
        continueBtn.addEventListener('click', () => {
          setTimeout(() => { window.location.href = '/app/dashboard.html'; }, 350);
        });
      }
    }

    // --- MAN HINH 4: CAY KY NANG & BO CHUYEN HOA NGHE (skills.html) ---
    if (path.endsWith('/skills.html')) {
      const castBtn = document.getElementById('cast-skill-btn');
      if (castBtn) {
        castBtn.addEventListener('click', async () => {
          try {
            const tasks = await apiCall('/tasks-quests/tasks?status=0');
            let targetTaskId = tasks && tasks.length > 0 ? tasks[0].taskId : null;
            if (!targetTaskId) {
              const t = await apiCall('/tasks-quests/tasks', 'POST', { title: 'Äá»“ Ã¡n giá»¯a ká»³ CS301', difficulty: 2 });
              targetTaskId = t.taskId;
            }
            // Mo khoa skill 1 neu chua mo khoa
            await apiCall('/skills-classes/skills/1/unlock', 'POST').catch(() => {});
            const res = await apiCall('/skills-classes/skills/1/cast', 'POST', { targetTaskId });
            showRetroToast(res.effectMessage, 'success');
            const freshDash = await apiCall('/characters/me/dashboard');
            syncHeaderHud(freshDash);
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        });
      }
    }

    // --- MAN HINH 5: LINH THU & PHONG AP TRUNG (pets.html) ---
    if (path.endsWith('/pets.html')) {
      let myPets = await apiCall('/pets-irl/pets/me').catch(() => []);
      if (!myPets || myPets.length === 0) {
        const adopted = await apiCall('/pets-irl/pets/adopt-egg', 'POST', { petName: 'CÃO MECHA', elementType: 'Lá»¬A CYBER' }).catch(() => null);
        if (adopted) {
          await apiCall(`/pets-irl/pets/${adopted.userPetId}/hatch`, 'POST').catch(() => {});
          myPets = await apiCall('/pets-irl/pets/me').catch(() => []);
        }
      }
      const activePetId = myPets && myPets.length > 0 ? myPets[0].userPetId : 1;

      const feedBtn = document.getElementById('btn-feed-pet');
      if (feedBtn) {
        feedBtn.addEventListener('click', async () => {
          try {
            const res = await apiCall(`/pets-irl/pets/${activePetId}/feed`, 'POST', { useGold: true });
            showRetroToast(res.message, 'success');
            const freshDash = await apiCall('/characters/me/dashboard');
            syncHeaderHud(freshDash);
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        });
      }

      const brushBtn = document.getElementById('btn-pet-brush');
      if (brushBtn) {
        brushBtn.addEventListener('click', async () => {
          try {
            const res = await apiCall(`/pets-irl/pets/${activePetId}/interact`, 'POST');
            const dlg = document.getElementById('kitsune-dialogue');
            if (dlg && res.dialogue) dlg.textContent = `â€œ${res.dialogue}â€`;
            showRetroToast('ÄÃ£ vuá»‘t ve Linh ThÃº (+5 Trung ThÃ nh)!', 'success');
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        });
      }

      // Override sendFamiliarMessage de goi AI Pet that tren Backend
      const origSend = window.sendFamiliarMessage;
      window.sendFamiliarMessage = async function () {
        const input = document.getElementById('pet-prompt-input');
        const dialogueEl = document.getElementById('kitsune-dialogue');
        const promptText = input ? input.value.trim() : '';
        if (origSend) origSend();
        try {
          const res = await apiCall(`/pets-irl/pets/${activePetId}/ai-chat`, 'POST', { userPrompt: promptText });
          if (dialogueEl && res.aiMessage) {
            dialogueEl.textContent = res.aiMessage;
          }
          showRetroToast('KITSUNE.AI Ä‘Ã£ phÃ¢n tÃ­ch dá»¯ liá»‡u Task tá»« SQL Server!', 'success');
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      };
    }

    // --- MAN HINH 6: KHO DO, GACHA & CHO P2P (inventory.html) ---
    if (path.endsWith('/inventory.html')) {
      window.performSummon = async function (pulls) {
        try {
          const banners = await apiCall('/gacha-market/banners');
          const bannerId = banners && banners.length > 0 ? banners[0].bannerId : 1;
          const endpoint = pulls === 10 ? `/gacha-market/banners/${bannerId}/pull-x10` : `/gacha-market/banners/${bannerId}/pull-x1`;
          const res = await apiCall(endpoint, 'POST');
          const itemNames = res.pulledItems.map(p => `- ${p.item.itemName} ${p.isPityTriggered ? '[Ná»” Báº¢O HIá»‚M PITY!]' : ''}`).join('\n');
          alert(`âœ¨ TRIá»†U Há»’I LÃ’ RÃˆN THÃ€NH CÃ”NG (${pulls}x)!\nBáº¡n nháº­n Ä‘Æ°á»£c:\n${itemNames}\n\nBá»™ Ä‘áº¿m Báº£o Hiá»ƒm hiá»‡n táº¡i: ${res.currentPityCounter}/${res.pityLimit}\nVÃ ng cÃ²n láº¡i: ${res.remainingGold} G`);
          const freshDash = await apiCall('/characters/me/dashboard');
          syncHeaderHud(freshDash);
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      };
    }

    // --- MAN HINH 7: NHIEM VU SU THI & GPA (gpa.html) ---
    if (path.endsWith('/gpa.html')) {
      const recalcBtn = document.getElementById('recalc-btn');
      if (recalcBtn) {
        recalcBtn.addEventListener('click', async () => {
          try {
            let quests = await apiCall('/tasks-quests/epic-quests');
            let gpaQuest = quests.find(q => q.isGpaQuest);
            if (!gpaQuest) {
              gpaQuest = await apiCall('/tasks-quests/epic-quests', 'POST', {
                title: 'Chiáº¿n dá»‹ch Tá»‘t nghiá»‡p Há»c ká»³ 1',
                isGpaQuest: true,
                targetGpa: 3.60,
                rewardExp: 3450,
                rewardGold: 850
              });
              await apiCall(`/tasks-quests/epic-quests/${gpaQuest.epicQuestId}/gpa-subjects`, 'POST', { subjectName: 'CS402: Kiáº¿n trÃºc Há»‡ phÃ¢n tÃ¡n', credits: 3, score: 9.35 });
              await apiCall(`/tasks-quests/epic-quests/${gpaQuest.epicQuestId}/gpa-subjects`, 'POST', { subjectName: 'SE310: Thiáº¿t káº¿ Pháº§n má»m', credits: 3, score: 8.79 });
              await apiCall(`/tasks-quests/epic-quests/${gpaQuest.epicQuestId}/gpa-subjects`, 'POST', { subjectName: 'GDQP101: GiÃ¡o dá»¥c Quá»‘c phÃ²ng', credits: 2, score: 5.0 });
            }
            const detail = await apiCall(`/tasks-quests/epic-quests/${gpaQuest.epicQuestId}`);
            showRetroToast(`ÄÃ£ thá»±c thi NxvSp_CalculateGPA! GPA tÃ­ch lÅ©y (Ä‘Ã£ loáº¡i GDQP & Ä‘iá»ƒm F): ${detail.currentCalculatedGpa} / 4.00`, 'success');
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        });
      }
    }

    // --- MAN HINH 9: HOI SINH TU TRAN VIETQR (revival.html) ---
    if (path.endsWith('/revival.html')) {
      try {
        await apiCall('/payment-revival/death-state/simulate-death', 'POST');
        const order = await apiCall('/payment-revival/orders', 'POST', { packageId: 2, paymentMethod: 'VIETQR' });
        window.__nxvCurrentOrderCode = order.orderCode;
        showRetroToast(`ÄÃ£ khá»Ÿi táº¡o mÃ£ Ä‘Æ¡n VietQR: ${order.orderCode} (50.000 VNÄ)`, 'info');

        const verifyBtn = document.getElementById('btn-verify');
        if (verifyBtn) {
          // Ghi de su kien click de goi Webhook HMAC-SHA256 that tren Backend
          const newVerifyBtn = verifyBtn.cloneNode(true);
          verifyBtn.parentNode.replaceChild(newVerifyBtn, verifyBtn);
          newVerifyBtn.addEventListener('click', async () => {
newVerifyBtn.disabled = true;
newVerifyBtn.innerHTML = `<span class="material-symbols-outlined text-[20px] animate-spin">refresh</span><span>ÄANG XÃC THá»°C CHá»® KÃ HMAC-SHA256 & Há»’I SINH...</span>`;
try {
const # ==============================================================================
# TASKTALE / LIFERPG - STITCH UI + SPRING BOOT LIVE INTEGRATION SCRIPT
# Dong goi 10 man hinh 16-Bit Neobrutalism cua Stitch & Noi truc tiep 9 Modules API
# ==============================================================================

$ErrorActionPreference = "Stop"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

$stitchRoot = "D:\DoAnTotNghiep\stitch_ui\stitch_tasktale_rpg_web_app"
if (-not (Test-Path $stitchRoot)) {
    $found = Get-ChildItem -Path "D:\DoAnTotNghiep\stitch_ui" -Directory -Filter "b_ng_nhi_m_v*" -Recurse | Select-Object -First 1
    if ($null -ne $found) {
        $stitchRoot = $found.Parent.FullName
    } else {
        Write-Error "Khong tim thay thu muc giai nen D:\DoAnTotNghiep\stitch_ui!"
        exit 1
    }
}

$staticAppDir = Join-Path (Get-Location) "src\main\resources\static\app"
$targetAppDir = Join-Path (Get-Location) "target\classes\static\app"
New-Item -ItemType Directory -Force -Path $staticAppDir | Out-Null
New-Item -ItemType Directory -Force -Path $targetAppDir | Out-Null

# Bang anh xa 10 thu muc Stitch sang ten file route gon dep
$screenMap = @{
    "x_c_th_c_t_o_nh_n_v_t_ti_ng_vi_t"              = "login.html"
    "b_ng_nhi_m_v_kanban_c_t_l_i_ti_ng_vi_t"         = "dashboard.html"
    "ho_n_th_nh_nhi_m_v_th_ng_c_p"                   = "levelup.html"
    "nhi_m_v_s_thi_theo_d_i_gpa_ti_ng_vi_t"          = "gpa.html"
    "c_y_k_n_ng_b_tr_ngh_ti_ng_vi_t"                 = "skills.html"
    "kho_khu_ch_gacha_ti_ng_vi_t"                    = "inventory.html"
    "h_th_ng_linh_th_bu_ng_p_tr_ng_ti_ng_vi_t"       = "pets.html"
    "t_i_bang_h_i_ti_ng_vi_t"                        = "party.html"
    "giao_di_n_to_n_c_c_h_i_sinh_t_tr_n_ti_ng_vi_t"  = "revival.html"
    "qu_n_tr_c_u_h_nh_h_th_ng_ti_ng_vi_t"            = "admin.html"
}

# ==============================================================================
# 1. TAO FILE CAU NOI API LIVE: tasktale-bridge.js
# ==============================================================================
$bridgeJs = @'
/**
 * TASKTALE / LIFERPG - LIVE BACKEND API BRIDGE (v2.2)
 * Ket noi truc tiep 10 man hinh 16-Bit Neobrutalism cua Stitch voi Spring Boot & SQL Server
 */
(function () {
  const API_BASE = '/api/v1';

  // 1. Bang dinh tuyen Sidebar & Header giua 10 man hinh Stitch
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
        showRetroToast('NHÃ‚N Váº¬T ÄÃƒ Tá»¬ TRáº¬N (HP = 0)! Äang chuyá»ƒn tá»›i cá»•ng Há»“i Sinh VietQR...', 'error');
        setTimeout(() => { window.location.href = '/app/revival.html'; }, 1200);
      }
      throw new Error(json.message || 'Lá»—i káº¿t ná»‘i mÃ¡y chá»§ (' + res.status + ')');
    }
    return json.data;
  }

  // Toast thong bao chuan 16-Bit Neobrutalism
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
      <span>âš¡ [SQL SERVER LIVE]: ${msg}</span>
      <button onclick="this.parentElement.parentElement.style.display='none'" class="px-1.5 py-0.5 bg-black text-white">âœ•</button>
    </div>`;
    box.style.display = 'block';
    clearTimeout(window.__nxvToastTimer);
    window.__nxvToastTimer = setTimeout(() => { box.style.display = 'none'; }, 4500);
  }

  // Tu dong dang nhap hoac khoi tao tai khoan Demo tren SQL Server neu chua co token
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

  // 2. Gan su kien chuyen trang cho toan bo Sidebar & Header + Thanh Quick-Nav
  function wireGlobalNavigation() {
    document.querySelectorAll('aside nav a').forEach(a => {
      const dp = a.getAttribute('data-path');
      if (dp && ROUTE_MAP[dp]) {
        a.setAttribute('href', ROUTE_MAP[dp]);
        a.addEventListener('click', (e) => {
          e.preventDefault();
          window.location.href = ROUTE_MAP[dp];
        });
      }
    });

    // Nut [HOI SINH] va [Avatar] tren Header
    document.querySelectorAll('header button, header div').forEach(el => {
      if (el.innerText && el.innerText.trim() === 'Há»’I SINH' && el.tagName === 'BUTTON') {
        el.addEventListener('click', () => {
          window.location.href = '/app/revival.html';
        });
      }
      if (el.querySelector && el.querySelector('span.material-symbols-outlined')?.textContent.trim() === 'person') {
        el.style.cursor = 'pointer';
        el.title = 'Chuyá»ƒn tá»›i mÃ n hÃ¬nh TÃ i khoáº£n & Táº¡o nhÃ¢n váº­t';
        el.addEventListener('click', () => {
          window.location.href = '/app/login.html';
        });
      }
    });

    // Thanh dieu huong nhanh 10 man hinh o goc duoi ben trai (giup demo bao ve do an cuc nhanh)
    const navBar = document.createElement('div');
    navBar.className = 'fixed bottom-3 left-3 z-[9990] flex flex-wrap items-center gap-1 bg-white border-2 border-black p-1.5 shadow-[4px_4px_0px_0px_#000000] font-mono text-[10px] font-bold uppercase';
    navBar.innerHTML = `
      <span class="px-1.5 py-0.5 bg-[#b61722] text-white border border-black">10 MÃ€N HÃŒNH:</span>
      <a href="/app/login.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">1.AUTH</a>
      <a href="/app/dashboard.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">2.KANBAN</a>
      <a href="/app/levelup.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">3.LEVEL-UP</a>
      <a href="/app/gpa.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">4.GPA</a>
      <a href="/app/skills.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">5.SKILLS</a>
      <a href="/app/inventory.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">6.GACHA&CHá»¢</a>
      <a href="/app/pets.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">7.LINH THÃš</a>
      <a href="/app/party.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">8.Tá»” Äá»˜I</a>
      <a href="/app/revival.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">9.VIETQR</a>
      <a href="/app/admin.html" class="px-1.5 py-0.5 border border-black hover:bg-[#ffddb8]">10.ADMIN</a>
    `;
    document.body.appendChild(navBar);
  }

  // 3. Dong bo chi so Nhan vat that tu SQL Server len Header HUD
  function syncHeaderHud(dash) {
    if (!dash) return;
    window.__nxvDashboard = dash;
    const header = document.querySelector('header');
    if (!header) return;

    // Cap nhat Level badge
    const lvlBadge = header.querySelector('.relative.w-12.h-12 span.absolute');
    if (lvlBadge) lvlBadge.textContent = dash.currentLevel;

    // Cap nhat Ten nhan vat & Class
    const nameEl = header.querySelector('.flex.items-center.gap-space-xs span.font-title-retro');
    if (nameEl) nameEl.textContent = dash.characterName || dash.username;
    const classEl = header.querySelector('.flex.items-center.gap-space-xs span.px-1\\.5');
    if (classEl) classEl.textContent = dash.currentClassName || 'Äáº I PHÃP SÆ¯';

    // Cap nhat HP, MP, EXP bars tren Header
    const statBlocks = header.querySelectorAll('.hidden.xl\\:flex > div');
    if (statBlocks.length >= 3) {
      // HP
      const hpText = statBlocks[0].querySelectorAll('span')[1];
      const hpBar = statBlocks[0].querySelector('.h-4 > div');
      const hpPct = dash.maxHp > 0 ? Math.min(100, Math.round((dash.currentHp / dash.maxHp) * 100)) : 100;
      if (hpText) hpText.textContent = `${dash.currentHp}/${dash.maxHp}`;
      if (hpBar) hpBar.style.width = `${hpPct}%`;

      // MP
      const mpText = statBlocks[1].querySelectorAll('span')[1];
      const mpBar = statBlocks[1].querySelector('.h-4 > div');
      const mpPct = dash.maxMp > 0 ? Math.min(100, Math.round((dash.currentMp / dash.maxMp) * 100)) : 100;
      if (mpText) mpText.textContent = `${dash.currentMp}/${dash.maxMp}`;
      if (mpBar) mpBar.style.width = `${mpPct}%`;

      // EXP
      const expText = statBlocks[2].querySelectorAll('span')[1];
      const expBar = statBlocks[2].querySelector('.h-4 > div');
      const expPct = dash.expToNextLevel > 0 ? Math.min(100, Math.round((dash.currentExp / dash.expToNextLevel) * 100)) : 0;
      if (expText) expText.textContent = `${dash.currentExp}/${dash.expToNextLevel}`;
      if (expBar) expBar.style.width = `${expPct}%`;
    }

    // Cap nhat Gold
    const goldSpan = header.querySelector('span.font-stat-counter');
    if (goldSpan) {
      goldSpan.textContent = Number(dash.gold || 0).toLocaleString('vi-VN') + ' VÃ€NG';
    }
  }

  // 4. Kich hoat Logic API rieng cho tung man hinh
  async function wireScreenSpecificApis(dash) {
    const path = window.location.pathname;

    // --- MAN HINH 1: LOGIN & REGISTER (login.html) ---
    if (path.endsWith('/login.html')) {
      document.querySelectorAll('button, form').forEach(el => {
        if (el.tagName === 'BUTTON' && (el.innerText.includes(' VÃ€O ') || el.innerText.includes('XÃC THá»°C') || el.innerText.includes('KHá»žI Táº O') || el.innerText.includes('ÄÄ‚NG NHáº¬P'))) {
          el.addEventListener('click', async () => {
            const inputs = document.querySelectorAll('input');
            let usernameOrEmail = 'thorin_archmage';
            let password = 'Password123!';
            inputs.forEach(inp => {
              if (inp.type === 'text' || inp.type === 'email') {
                if (inp.value.trim()) usernameOrEmail = inp.value.trim();
              } else if (inp.type === 'password') {
                if (inp.value.trim()) password = inp.value.trim();
              }
            });
            try {
              const res = await apiCall('/auth/login', 'POST', { usernameOrEmail, password });
              setToken(res.accessToken);
              showRetroToast('ÄÄƒng nháº­p thÃ nh cÃ´ng! Äang tiáº¿n vÃ o Äáº¡i Sáº£nh...', 'success');
              setTimeout(() => { window.location.href = '/app/dashboard.html'; }, 700);
            } catch (err) {
              try {
                const rand = Math.floor(1000 + Math.random() * 9000);
                const reg = await apiCall('/auth/register', 'POST', {
                  username: usernameOrEmail.replace(/[^a-zA-Z0-9_]/g, '') + '_' + rand,
                  email: 'hero_' + rand + '@tasktale.vn',
                  password: password,
                  gender: 0,
                  characterName: usernameOrEmail.toUpperCase()
                });
                setToken(reg.accessToken);
                showRetroToast('ÄÃ£ rÃ¨n NhÃ¢n Váº­t má»›i trÃªn SQL Server! Äang vÃ o Äáº¡i Sáº£nh...', 'success');
                setTimeout(() => { window.location.href = '/app/dashboard.html'; }, 700);
              } catch (e2) {
                showRetroToast(e2.message, 'error');
              }
            }
          });
        }
      });
    }

    // --- MAN HINH 2: KANBAN NHIEM VU COT LOI (dashboard.html) ---
    if (path.endsWith('/dashboard.html') || path === '/' || path === '/app/') {
      const cardsTodo = document.getElementById('cards-todo');
      const cardsDone = document.getElementById('cards-done');

      // Load cac task that tu SQL Server va chen len dau cot Kanban
      try {
        const dbTasks = await apiCall('/tasks-quests/tasks');
        if (Array.isArray(dbTasks) && cardsTodo && cardsDone) {
          dbTasks.forEach(t => {
            if (document.getElementById('db-quest-' + t.taskId)) return;
            const rankLabel = t.difficulty === 3 ? 'S' : t.difficulty === 2 ? 'A' : 'B';
            const card = document.createElement('article');
            card.id = 'db-quest-' + t.taskId;
            card.setAttribute('data-task-id', t.taskId);
            card.setAttribute('data-category', 'coding');
            card.setAttribute('data-rank', rankLabel);
            card.className = 'quest-card flex flex-col bg-surface-container-lowest border-2 border-black shadow-[4px_4px_0px_0px_#000000] hover:translate-x-[-2px] hover:translate-y-[-2px] hover:shadow-[6px_6px_0px_0px_#000000] transition-all';
            const isDone = t.status === 1;
            card.innerHTML = `
              <div class="flex items-center justify-between px-space-md py-space-xs bg-surface-container border-b-2 border-black">
                <div class="flex items-center gap-1.5">
                  <span class="px-1.5 py-0.5 bg-primary text-on-primary font-label-sm text-label-sm uppercase font-bold border border-black">SQL TASK #${t.taskId}</span>
                  <span class="px-1.5 py-0.5 bg-secondary-fixed text-on-secondary-fixed font-label-sm text-label-sm uppercase border border-black">${t.isFrozen ? 'ÄÃƒ ÄÃ“NG BÄ‚NG' : 'NHIá»†M Vá»¤ THáº¬T'}</span>
                </div>
                <div class="flex items-center gap-1 px-1.5 py-0.5 bg-primary-fixed border border-black">
                  <span class="font-title-retro text-title-retro text-primary font-bold">Háº NG ${rankLabel}</span>
                </div>
              </div>
              <div class="p-space-md flex flex-col gap-space-sm">
                <h3 class="font-headline-sm text-headline-sm text-on-surface ${isDone ? 'line-through decoration-2 decoration-black' : ''} leading-tight">${t.title}</h3>
                <p class="font-body-sm text-body-sm text-on-surface-variant">${t.descriptionMarkdown || 'Nhiá»‡m vá»¥ Ä‘á»“ng bá»™ trá»±c tiáº¿p tá»« báº£ng NxvTasks (SQL Server v2.2).'}</p>
                <div class="grid grid-cols-2 gap-1.5 pt-1">
                  <div class="flex items-center gap-1 px-2 py-1 bg-tertiary-fixed border border-black text-on-tertiary-fixed">
                    <span class="material-symbols-outlined text-[16px]">stars</span>
                    <span class="font-stat-counter text-stat-counter">+${t.rewardExp} EXP</span>
                  </div>
                  <div class="flex items-center gap-1 px-2 py-1 bg-tertiary-fixed-dim border border-black text-on-tertiary-fixed">
                    <span class="material-symbols-outlined text-[16px]">monetization_on</span>
                    <span class="font-stat-counter text-stat-counter">+${t.rewardGold} VÃ€NG</span>
                  </div>
                </div>
              </div>
              <div class="p-space-sm bg-surface-container border-t-2 border-black ${isDone ? 'flex items-center justify-between' : 'grid grid-cols-3 gap-1.5'}">
                ${isDone ? `
                  <span class="font-label-sm text-label-sm text-on-surface-variant">ÄÃƒ LÆ¯U SQL</span>
                  <button class="action-btn-restore flex items-center gap-1 px-2.5 py-1 bg-surface-container-lowest border-2 border-black font-label-md text-label-md font-bold text-on-surface shadow-[2px_2px_0px_0px_#000000]">
                    <span class="material-symbols-outlined text-[16px]">replay</span><span>KHÃ”I PHá»¤C</span>
                  </button>
                ` : `
                  <button class="action-btn-slay flex items-center justify-center gap-1 py-1.5 bg-primary text-on-primary border-2 border-black font-label-md text-label-md font-bold shadow-[2px_2px_0px_0px_#000000]">
                    <span class="material-symbols-outlined text-[16px]">swords</span><span>DIá»†T</span>
                  </button>
                  <button class="action-btn-freeze flex items-center justify-center gap-1 py-1.5 bg-secondary-fixed text-on-secondary-fixed border-2 border-black font-label-md text-label-md font-bold shadow-[2px_2px_0px_0px_#000000]">
                    <span class="material-symbols-outlined text-secondary text-[16px]">ac_unit</span><span>${t.isFrozen ? 'Má»ž BÄ‚NG' : 'ÄÃ“NG BÄ‚NG'}</span>
                  </button>
                  <button class="action-btn-skip flex items-center justify-center gap-1 py-1.5 bg-surface-container-lowest text-on-surface border-2 border-black font-label-md text-label-md font-bold shadow-[2px_2px_0px_0px_#000000]">
                    <span class="material-symbols-outlined text-[16px]">fast_forward</span><span>Bá»Ž QUA</span>
                  </button>
                `}
              </div>
            `;
            if (isDone) cardsDone.prepend(card);
            else cardsTodo.prepend(card);
          });
        }
      } catch (e) {
        console.warn('Load tasks warning:', e);
      }

      // Khi bam nut KHAC LEN CUON GIAY (#submit-forge-btn) -> Luu Task that vao SQL Server
      const submitForgeBtn = document.getElementById('submit-forge-btn');
      const titleInput = document.getElementById('quest-title-input');
      if (submitForgeBtn && titleInput) {
        submitForgeBtn.addEventListener('click', async () => {
          const titleVal = titleInput.value.trim();
          if (!titleVal) return;
          try {
            const created = await apiCall('/tasks-quests/tasks', 'POST', {
              title: titleVal,
              difficulty: 3,
              rewardExp: 250,
              rewardGold: 80,
              rewardMp: 15,
              penaltyHp: 20
            });
            const newestCard = cardsTodo?.querySelector('.quest-card');
            if (newestCard && !newestCard.getAttribute('data-task-id')) {
              newestCard.setAttribute('data-task-id', created.taskId);
            }
            showRetroToast(`ÄÃ£ lÆ°u nhiá»‡m vá»¥ "${created.title}" (TaskID=${created.taskId}) vÃ o SQL Server!`, 'success');
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        }, true);
      }

      // Bat su kien DIET / DONG BANG / BO QUA / KHOI PHUC tren Kanban goi Stored Procedure
      document.addEventListener('click', async (e) => {
        const card = e.target.closest('.quest-card');
        if (!card) return;
        let taskId = card.getAttribute('data-task-id');

        const slayBtn = e.target.closest('.action-btn-slay');
        const freezeBtn = e.target.closest('.action-btn-freeze');
        const skipBtn = e.target.closest('.action-btn-skip');
        const restoreBtn = e.target.closest('.action-btn-restore');

        if (!slayBtn && !freezeBtn && !skipBtn && !restoreBtn) return;

        try {
          // Neu la card mau cua Stitch chua co trong DB thi tu dong tao trong DB truoc khi goi SP
          if (!taskId) {
            const titleEl = card.querySelector('h3');
            const created = await apiCall('/tasks-quests/tasks', 'POST', {
              title: titleEl ? titleEl.innerText.trim() : 'Nhiá»‡m vá»¥ Chiáº¿n dá»‹ch',
              difficulty: 3,
              rewardExp: 350,
              rewardGold: 120,
              rewardMp: 15,
              penaltyHp: 25
            });
            taskId = created.taskId;
            card.setAttribute('data-task-id', taskId);
          }

          if (slayBtn) {
            const res = await apiCall(`/tasks-quests/tasks/${taskId}/complete`, 'POST');
            syncHeaderHud(res.updatedDashboard);
            const lvGained = res.spResult?.LevelsGained || res.spResult?.levelsGained || 0;
            showRetroToast(`DIá»†T THÃ€NH CÃ”NG (Task #${taskId})! +${res.spResult?.EXPGained || 350} EXP, +${res.spResult?.GoldGained || 120} VÃ€NG!`, 'success');
            if (lvGained > 0) {
              setTimeout(() => { window.location.href = '/app/levelup.html'; }, 1000);
            }
          } else if (freezeBtn) {
            await apiCall(`/tasks-quests/tasks/${taskId}/toggle-freeze`, 'POST');
            showRetroToast(`ÄÃ£ cáº­p nháº­t tráº¡ng thÃ¡i ÄÃ³ng BÄƒng cho Task #${taskId}!`, 'info');
          } else if (skipBtn) {
            const res = await apiCall(`/tasks-quests/tasks/${taskId}/skip`, 'POST');
            showRetroToast(`ÄÃ£ dÃ¹ng ${res.mpSpent} MP Ä‘á»ƒ Bá» qua an toÃ n Task #${taskId}!`, 'info');
            const freshDash = await apiCall('/characters/me/dashboard');
            syncHeaderHud(freshDash);
          } else if (restoreBtn) {
            await apiCall(`/tasks-quests/tasks/${taskId}/undo`, 'POST');
            showRetroToast(`ÄÃ£ hoÃ n tÃ¡c (Undo) Task #${taskId} vá» danh sÃ¡ch chá»!`, 'info');
            const freshDash = await apiCall('/characters/me/dashboard');
            syncHeaderHud(freshDash);
          }
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      });
    }

    // --- MAN HINH 3: MODAL HOAN THANH & THANG CAP (levelup.html) ---
    if (path.endsWith('/levelup.html')) {
      const continueBtn = document.getElementById('continue-adventure-btn');
      if (continueBtn) {
        continueBtn.addEventListener('click', () => {
          setTimeout(() => { window.location.href = '/app/dashboard.html'; }, 350);
        });
      }
    }

    // --- MAN HINH 4: CAY KY NANG & BO CHUYEN HOA NGHE (skills.html) ---
    if (path.endsWith('/skills.html')) {
      const castBtn = document.getElementById('cast-skill-btn');
      if (castBtn) {
        castBtn.addEventListener('click', async () => {
          try {
            const tasks = await apiCall('/tasks-quests/tasks?status=0');
            let targetTaskId = tasks && tasks.length > 0 ? tasks[0].taskId : null;
            if (!targetTaskId) {
              const t = await apiCall('/tasks-quests/tasks', 'POST', { title: 'Äá»“ Ã¡n giá»¯a ká»³ CS301', difficulty: 2 });
              targetTaskId = t.taskId;
            }
            // Mo khoa skill 1 neu chua mo khoa
            await apiCall('/skills-classes/skills/1/unlock', 'POST').catch(() => {});
            const res = await apiCall('/skills-classes/skills/1/cast', 'POST', { targetTaskId });
            showRetroToast(res.effectMessage, 'success');
            const freshDash = await apiCall('/characters/me/dashboard');
            syncHeaderHud(freshDash);
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        });
      }
    }

    // --- MAN HINH 5: LINH THU & PHONG AP TRUNG (pets.html) ---
    if (path.endsWith('/pets.html')) {
      let myPets = await apiCall('/pets-irl/pets/me').catch(() => []);
      if (!myPets || myPets.length === 0) {
        const adopted = await apiCall('/pets-irl/pets/adopt-egg', 'POST', { petName: 'CÃO MECHA', elementType: 'Lá»¬A CYBER' }).catch(() => null);
        if (adopted) {
          await apiCall(`/pets-irl/pets/${adopted.userPetId}/hatch`, 'POST').catch(() => {});
          myPets = await apiCall('/pets-irl/pets/me').catch(() => []);
        }
      }
      const activePetId = myPets && myPets.length > 0 ? myPets[0].userPetId : 1;

      const feedBtn = document.getElementById('btn-feed-pet');
      if (feedBtn) {
        feedBtn.addEventListener('click', async () => {
          try {
            const res = await apiCall(`/pets-irl/pets/${activePetId}/feed`, 'POST', { useGold: true });
            showRetroToast(res.message, 'success');
            const freshDash = await apiCall('/characters/me/dashboard');
            syncHeaderHud(freshDash);
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        });
      }

      const brushBtn = document.getElementById('btn-pet-brush');
      if (brushBtn) {
        brushBtn.addEventListener('click', async () => {
          try {
            const res = await apiCall(`/pets-irl/pets/${activePetId}/interact`, 'POST');
            const dlg = document.getElementById('kitsune-dialogue');
            if (dlg && res.dialogue) dlg.textContent = `â€œ${res.dialogue}â€`;
            showRetroToast('ÄÃ£ vuá»‘t ve Linh ThÃº (+5 Trung ThÃ nh)!', 'success');
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        });
      }

      // Override sendFamiliarMessage de goi AI Pet that tren Backend
      const origSend = window.sendFamiliarMessage;
      window.sendFamiliarMessage = async function () {
        const input = document.getElementById('pet-prompt-input');
        const dialogueEl = document.getElementById('kitsune-dialogue');
        const promptText = input ? input.value.trim() : '';
        if (origSend) origSend();
        try {
          const res = await apiCall(`/pets-irl/pets/${activePetId}/ai-chat`, 'POST', { userPrompt: promptText });
          if (dialogueEl && res.aiMessage) {
            dialogueEl.textContent = res.aiMessage;
          }
          showRetroToast('KITSUNE.AI Ä‘Ã£ phÃ¢n tÃ­ch dá»¯ liá»‡u Task tá»« SQL Server!', 'success');
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      };
    }

    // --- MAN HINH 6: KHO DO, GACHA & CHO P2P (inventory.html) ---
    if (path.endsWith('/inventory.html')) {
      window.performSummon = async function (pulls) {
        try {
          const banners = await apiCall('/gacha-market/banners');
          const bannerId = banners && banners.length > 0 ? banners[0].bannerId : 1;
          const endpoint = pulls === 10 ? `/gacha-market/banners/${bannerId}/pull-x10` : `/gacha-market/banners/${bannerId}/pull-x1`;
          const res = await apiCall(endpoint, 'POST');
          const itemNames = res.pulledItems.map(p => `- ${p.item.itemName} ${p.isPityTriggered ? '[Ná»” Báº¢O HIá»‚M PITY!]' : ''}`).join('\n');
          alert(`âœ¨ TRIá»†U Há»’I LÃ’ RÃˆN THÃ€NH CÃ”NG (${pulls}x)!\nBáº¡n nháº­n Ä‘Æ°á»£c:\n${itemNames}\n\nBá»™ Ä‘áº¿m Báº£o Hiá»ƒm hiá»‡n táº¡i: ${res.currentPityCounter}/${res.pityLimit}\nVÃ ng cÃ²n láº¡i: ${res.remainingGold} G`);
          const freshDash = await apiCall('/characters/me/dashboard');
          syncHeaderHud(freshDash);
        } catch (err) {
          showRetroToast(err.message, 'error');
        }
      };
    }

    // --- MAN HINH 7: NHIEM VU SU THI & GPA (gpa.html) ---
    if (path.endsWith('/gpa.html')) {
      const recalcBtn = document.getElementById('recalc-btn');
      if (recalcBtn) {
        recalcBtn.addEventListener('click', async () => {
          try {
            let quests = await apiCall('/tasks-quests/epic-quests');
            let gpaQuest = quests.find(q => q.isGpaQuest);
            if (!gpaQuest) {
              gpaQuest = await apiCall('/tasks-quests/epic-quests', 'POST', {
                title: 'Chiáº¿n dá»‹ch Tá»‘t nghiá»‡p Há»c ká»³ 1',
                isGpaQuest: true,
                targetGpa: 3.60,
                rewardExp: 3450,
                rewardGold: 850
              });
              await apiCall(`/tasks-quests/epic-quests/${gpaQuest.epicQuestId}/gpa-subjects`, 'POST', { subjectName: 'CS402: Kiáº¿n trÃºc Há»‡ phÃ¢n tÃ¡n', credits: 3, score: 9.35 });
              await apiCall(`/tasks-quests/epic-quests/${gpaQuest.epicQuestId}/gpa-subjects`, 'POST', { subjectName: 'SE310: Thiáº¿t káº¿ Pháº§n má»m', credits: 3, score: 8.79 });
              await apiCall(`/tasks-quests/epic-quests/${gpaQuest.epicQuestId}/gpa-subjects`, 'POST', { subjectName: 'GDQP101: GiÃ¡o dá»¥c Quá»‘c phÃ²ng', credits: 2, score: 5.0 });
            }
            const detail = await apiCall(`/tasks-quests/epic-quests/${gpaQuest.epicQuestId}`);
            showRetroToast(`ÄÃ£ thá»±c thi NxvSp_CalculateGPA! GPA tÃ­ch lÅ©y (Ä‘Ã£ loáº¡i GDQP & Ä‘iá»ƒm F): ${detail.currentCalculatedGpa} / 4.00`, 'success');
          } catch (err) {
            showRetroToast(err.message, 'error');
          }
        });
      }
    }

    // --- MAN HINH 9: HOI SINH TU TRAN VIETQR (revival.html) ---
    if (path.endsWith('/revival.html')) {
      try {
        await apiCall('/payment-revival/death-state/simulate-death', 'POST');
        const order = await apiCall('/payment-revival/orders', 'POST', { packageId: 2, paymentMethod: 'VIETQR' });
        window.__nxvCurrentOrderCode = order.orderCode;
        showRetroToast(`ÄÃ£ khá»Ÿi táº¡o mÃ£ Ä‘Æ¡n VietQR: ${order.orderCode} (50.000 VNÄ)`, 'info');

        const verifyBtn = document.getElementById('btn-verify');
        if (verifyBtn) {
          // Ghi de su kien click de goi Webhook HMAC-SHA256 that tren Backend
          const newVerifyBtn = verifyBtn.cloneNode(true);
          verifyBtn.parentNode.replaceChild(newVerifyBtn, verifyBtn);
          newVerifyBtn.addEventListener('click', async () => {
            newVerifyBtn.disabled = true;
            newVerifyBtn.innerHTML = `<span class="material-symbols-outlined text-[20px] animate-spin">refresh</span><span>ÄANG XÃC THá»°C CHá»® KÃ HMAC-SHA256 & Há»’I SINH...</span>`;
            try {
              const rev = await apiCall(`/payment-revival/webhook/simulate-paid/${window.__nxvCurrentOrderCode}`, 'POST');
              showRetroToast(rev.message, 'success');
              setTimeout(() => { window.location.href = '/app/dashboard.html'; }, 1200);
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

    // --- MAN HINH 10: QUAN TRI HE THONG ADMIN (admin.html) ---
    if (path.endsWith('/admin.html')) {
      try {
        const sum = await apiCall('/admin-system/dashboard/summary');
        showRetroToast(`Admin Kernel Connected: ${sum.totalUsers} Users | ${sum.totalTasks} Tasks | Doanh thu: ${Number(sum.totalRevenueVnd || 0).toLocaleString('vi-VN')} VNÄ`, 'info');
      } catch (err) {
        console.warn('Admin summary:', err);
      }
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