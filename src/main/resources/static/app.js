const $  = (sel) => document.querySelector(sel);
const $$ = (sel) => Array.from(document.querySelectorAll(sel));


function show(name) {
  $$('.screen').forEach((s) => s.classList.toggle('active', s.id === 'screen-' + name));
}

async function api(path, body) {
  const options = body === undefined
    ? { method: 'GET' }
    : { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) };
  const res = await fetch('/api' + path, options);
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error || res.statusText || 'Request failed');
  return data;
}

function log(message, level = 'info') {
  const box = $('#log');
  const line = document.createElement('div');
  line.className = 'line ' + level;
  const mark = { warn: '! ', danger: '!! ' };  
  line.textContent = (mark[level] || '') + message;
  box.appendChild(line);
  box.scrollTop = box.scrollHeight;
}

function setBar(sel, value, max) {
  const bar = $(sel);
  const pct = max > 0 ? Math.max(0, Math.min(100, Math.round((value * 100) / max))) : 0;
  bar.style.setProperty('--v', pct);
  bar.classList.toggle('warn', pct <= 30 && pct > 15);
  bar.classList.toggle('crit', pct <= 15);
}

function showRaw(preSel, data) {
  const pre = $(preSel);
  pre.hidden = false;
  pre.textContent = JSON.stringify(data, null, 2);
}


function readDock() {
  return {
    hull: $('#hull').value,
    engine: $('#engine').value,
    light: $('#light').value,
    tool: $('#tool').value,
    biome: $('#biome').value,
    tactic: (document.querySelector('input[name="tactic"]:checked') || {}).value,
    upgrades: $$('input[name="upgrade"]:checked').map((c) => c.value),
  };
}

function renderSpec(spec) {
  if (spec.maxDepth === undefined) {        
    showRaw('#spec-raw', spec);
    return;
  }
  $('#spec-raw').hidden = true;
  $('#spec-desc').textContent = spec.description;
  $('#spec-warning').textContent = spec.warning || '';
  $('#spec-warning').hidden = !spec.warning;
  $('#spec-hull').textContent = spec.hull;
  $('#spec-depth').textContent = spec.maxDepth + ' m';
  $('#spec-oxygen').textContent = spec.oxygen;
  $('#spec-light').textContent = spec.light;
  $('#spec-attack').textContent = spec.attack;
  $('#spec-heat').textContent = spec.heatResistance + '%';
  $('#spec-cost').textContent = spec.cost + ' / ' + spec.budget;
  const budget = $('#bar-budget');
  budget.style.setProperty('--v', Math.min(100, Math.round((spec.cost * 100) / spec.budget)));
  budget.classList.toggle('warn', spec.affordable && spec.cost > spec.budget * 0.85);
  budget.classList.toggle('crit', !spec.affordable);
  $('#launch').disabled = !spec.affordable;
}

let specTimer;
function refreshSpec() {
  clearTimeout(specTimer);
  specTimer = setTimeout(async () => {
    try { renderSpec(await api('/build', readDock())); }
    catch (e) { showRaw('#spec-raw', { error: e.message }); }
  }, 200);
}

function renderState(state) {
  if (state.depth === undefined) {         
    log(JSON.stringify(state), 'info');
    return;
  }
  $('#hud-depth').textContent = state.depth + ' m';
  $('#hud-turn').textContent = state.turn + ' / ' + state.totalTurns;
  $('#hud-oxygen-num').textContent = state.oxygen + ' / ' + state.oxygenMax;
  $('#hud-hull-num').textContent = state.hull + ' / ' + state.hullMax;
  setBar('#bar-oxygen', state.oxygen, state.oxygenMax);
  setBar('#bar-hull', state.hull, state.hullMax);
  $('#tactic-label').textContent = 'Tactic: ' + state.tactic + ' · ' + state.biome;

  const enc = state.encounter;
  const routes = state.routes || [];
  renderRoutes(routes, state.over);
  $('#encounter-name').textContent = enc ? enc.name : (routes.length ? 'Choose a route' : 'Nothing ahead');
  $('#encounter-tags').textContent = enc
    ? [enc.kind, enc.danger > 0 ? 'danger ' + enc.danger : '', enc.reward > 0 ? 'reward ' + enc.reward : '']
        .filter(Boolean).join(' · ')
    : '';

  (state.events || []).forEach((ev) => log(ev.message, ev.level));
    $$('#actions [data-action]').forEach((b) => (b.disabled = state.over || !enc));
}

function describeRoute(r) {
  if (r.kind === 'creature') return 'creature, danger ' + r.danger + ', reward ' + r.reward;
  if (r.kind === 'hazard') return 'hazard, danger ' + r.danger;
  if (r.kind === 'loot') return 'loot, reward ' + r.reward;
  return 'event';
}

function renderRoutes(routes, over) {
  const holder = $('#routes');
  holder.replaceChildren();
  $('#routes-box').hidden = over || routes.length === 0;
  routes.forEach((r) => {
    const b = document.createElement('button');
    b.type = 'button';
    b.textContent = (r.index + 1) + '. ' + r.label + ': ' +
      (r.known ? r.name + ' (' + describeRoute(r) + ')' : 'unknown');
    b.addEventListener('click', () => takeRoute(r.index));
    holder.appendChild(b);
  });
}

async function takeRoute(index) {
  try {
    const state = await api('/dive/route', { route: index });
    renderState(state);
    if (state.over) await loadReport();
  } catch (e) {
    log('Error: ' + e.message, 'danger');
  }
}


async function takeAction(action) {
  try {
    const state = await api('/dive/step', { action });
    renderState(state);
    if (state.over) await loadReport();
  } catch (e) {
    log('Error: ' + e.message, 'danger');
  }
}


function renderReport(r) {
  if (r.score === undefined) {             
    showRaw('#rep-raw', r);
    return;
  }
  $('#rep-raw').hidden = true;
  $('#rep-outcome').textContent = r.outcome.replaceAll('_', ' ');
  $('#rep-score').textContent = r.score;
  $('#rep-sub').textContent = r.submarine;
  $('#rep-biome').textContent = r.biome;
  $('#rep-tactic').textContent = r.tactic;
  $('#rep-depth').textContent = r.depth + ' m';
  $('#rep-hull').textContent = r.hullLeft;
  $('#rep-oxygen').textContent = r.oxygenLeft;
  $('#rep-loot').textContent = r.loot;
  fillList('#rep-creatures', r.creatures);
  fillList('#rep-items', r.items);
}

function fillList(sel, items) {
  const ul = $(sel);
  ul.replaceChildren();
  (items.length ? items : ['none']).forEach((text) => {
    const li = document.createElement('li');
    li.textContent = text;
    ul.appendChild(li);
  });
}

async function loadReport() {
  try { renderReport(await api('/report')); }
  catch (e) { showRaw('#rep-raw', { error: e.message }); }
  show('debrief');
}

async function ping() {
  const chip = $('#link-status');
  try {
    const res = await fetch('/api/ping');
    chip.textContent = (await res.text()).trim() || 'online';
    chip.className = 'status on';
  } catch (e) {
    chip.textContent = 'backend offline';
    chip.className = 'status off';
  }
}

async function launch() {
  const err = $('#dock-error');
  err.hidden = true;
  try {
    $('#log').replaceChildren();
    const state = await api('/dive/start', readDock());
    show('dive');
    renderState(state);
  } catch (e) {
    err.textContent = e.message;
    err.hidden = false;
  }
}

$('#launch').addEventListener('click', launch);
$('#dock-form').addEventListener('change', refreshSpec);
$$('#actions [data-action]').forEach((b) => b.addEventListener('click', () => takeAction(b.dataset.action)));
$('#again').addEventListener('click', () => show('dock'));

document.addEventListener('keydown', (e) => {
  if (!$('#screen-dive').classList.contains('active')) return;
    if (!$('#routes-box').hidden) {               
    const index = { '1': 0, '2': 1, '3': 2 }[e.key];
    if (index !== undefined) takeRoute(index);
    return;
  }
  const action = { '1': 'SNEAK', '2': 'FIGHT', '3': 'FLEE' }[e.key];
  if (action) takeAction(action);
});

ping();
refreshSpec();
