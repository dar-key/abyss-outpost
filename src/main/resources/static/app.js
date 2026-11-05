/* ==========================================================================
   Abyss Outpost: page skeleton script

   Who edits what:
     P1  renderSpec()   + the Dock form (readDock, spec refresh)
     P2  renderState()  + the dive screen (bars, encounter, log, action buttons)
     P3  renderReport() + the debrief screen, and the JSON the backend returns

   Right now the backend is a placeholder and answers {"status":"TODO ..."}.
   Every render function therefore falls back to printing the raw JSON, so the
   page works end to end before any pattern exists.

   PROPOSED JSON SHAPES (agree them with P3 before relying on them):

   POST /api/build, response (spec sheet)
     { description, hull, maxDepth, oxygen, light, attack, heatResistance,
       cost, budget, affordable, warning }   (warning: text or "")

   POST /api/dive/start and POST /api/dive/step, response (dive state)
     { biome, biomeId, tactic, turn, totalTurns, depth, maxDepth,
       oxygen, oxygenMax, hull, hullMax, loot, over, outcome,
       encounter: { kind, emoji, name, danger, reward } | null,
       events: [ { type, message, level, value } ] }

   GET /api/report, response (debrief)
     { outcome, score, loot, depth, hullLeft, oxygenLeft, turns,
       submarine, biome, tactic, creatures: [..], items: [..] }
   ========================================================================== */

const $  = (sel) => document.querySelector(sel);
const $$ = (sel) => Array.from(document.querySelectorAll(sel));

/* ---------- small helpers ---------------------------------------------- */

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
  const mark = { warn: '! ', danger: '!! ' };   // plain text marks instead of colours
  line.textContent = (mark[level] || '') + message;
  box.appendChild(line);
  box.scrollTop = box.scrollHeight;
}

/* A bar needs --v (0 to 100). Under 30% it turns amber, under 15% red and blinks. */
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

/* ---------- P1: DOCK -------------------------------------------------- */

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
  if (spec.maxDepth === undefined) {          // placeholder backend: show raw JSON
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
  // budget bar: the opposite of oxygen, a FULL bar is the danger
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

/* ---------- P2: DIVE -------------------------------------------------- */

function renderState(state) {
  if (state.depth === undefined) {            // placeholder backend: show raw JSON
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
  $('#encounter-name').textContent = enc ? enc.name : 'Nothing ahead';
  $('#encounter-tags').textContent = enc
    ? [enc.kind, enc.danger > 0 ? 'danger ' + enc.danger : '', enc.reward > 0 ? 'reward ' + enc.reward : '']
        .filter(Boolean).join(' · ')
    : '';

  (state.events || []).forEach((ev) => log(ev.message, ev.level));
  $$('#actions [data-action]').forEach((b) => (b.disabled = state.over));
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

/* ---------- P3: DEBRIEF ------------------------------------------------ */

function renderReport(r) {
  if (r.score === undefined) {                // placeholder backend: show raw JSON
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

/* ---------- start-up and wiring --------------------------------------- */

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
  const action = { '1': 'SNEAK', '2': 'FIGHT', '3': 'FLEE' }[e.key];
  if (action) takeAction(action);
});

/* ---------- developer bar: look at each screen without the backend ----- */

$$('.devbar [data-go]').forEach((b) => b.addEventListener('click', () => show(b.dataset.go)));

$('#dev-fake').addEventListener('click', () => {
  renderSpec({ description: 'Titanium hull · Efficient engine · Searchlight · Plasma cutter',
               hull: 100, maxDepth: 450, oxygen: 135, light: 3, attack: 4, heatResistance: 0,
               cost: 570, budget: 600, affordable: true,
               warning: 'Not enough oxygen for 8 turns with the Greedy tactic.' });
  $('#log').replaceChildren();
  renderState({ biome: 'Magma Depths', biomeId: 'LAVA', tactic: 'Greedy', turn: 5, totalTurns: 8,
                depth: 250, maxDepth: 450, oxygen: 28, oxygenMax: 135, hull: 54, hullMax: 100, loot: 61,
                over: false, outcome: 'DIVING',
                encounter: { kind: 'creature', name: 'Magma Squid', danger: 16, reward: 22 },
                events: [
                  { type: 'LOG', level: 'info',   message: 'Launching into the Magma Depths with the Greedy tactic.' },
                  { type: 'DEPTH', level: 'info',  message: 'Turn 5/8: depth 250 m.' },
                  { type: 'LOG', level: 'good',   message: 'You snatch the Fire Crystal. +56' },
                  { type: 'LOG', level: 'warn',   message: 'Oxygen low: 20%' },
                  { type: 'LOG', level: 'danger', message: 'The Ember Eel gets the better of you: -26 hull.' },
                  { type: 'LOG', level: 'warn',   message: 'Ahead: Magma Squid.' },
                ] });
  renderReport({ outcome: 'SURFACED', score: 168, loot: 61, depth: 400, hullLeft: 54, oxygenLeft: 12, turns: 8,
                 submarine: 'Titanium hull · Efficient engine · Searchlight · Plasma cutter',
                 biome: 'Magma Depths', tactic: 'Greedy',
                 creatures: ['Magma Squid', 'Ember Eel'], items: ['Fire Crystal (+56)', 'Obsidian Shard (+25)'] });
});

ping();
refreshSpec();
