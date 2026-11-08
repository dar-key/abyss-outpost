(function () {
  'use strict';

  const $ = (sel) => document.querySelector(sel);
  const SVGNS = 'http://www.w3.org/2000/svg';
  const BEARINGS = [-50, 0, 50];            
  const PERIOD = 6500;                     

  const fx = !(window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches);

  const A0 = -125, A1 = 125;              

  function polar(r, deg) {
    const a = (deg * Math.PI) / 180;
    return [60 + r * Math.sin(a), 60 - r * Math.cos(a)];
  }

  function makeGauge(el) {
    const svg = document.createElementNS(SVGNS, 'svg');
    svg.setAttribute('viewBox', '0 0 120 120');
    svg.setAttribute('role', 'img');
    svg.innerHTML =
      '<circle class="g-face" cx="60" cy="60" r="57"/><circle class="g-rim" cx="60" cy="60" r="54"/>' +
      '<g class="g-ticks"></g><path class="g-red" d=""/>' +
      '<g class="g-needle"><line x1="60" y1="60" x2="60" y2="17"/><circle cx="60" cy="60" r="5"/></g>' +
      '<text class="g-val" x="60" y="94">-</text>';
    el.appendChild(svg);

    const ticks = svg.querySelector('.g-ticks');
    const red = svg.querySelector('.g-red');
    const needle = svg.querySelector('.g-needle');
    const val = svg.querySelector('.g-val');
    let cfgKey = '', cfg = null, jitter = 0;

    const g = {
      configure(c) {
        const key = [c.max, c.step, c.redFrom, c.redTo].join('/');
        if (key === cfgKey) return;
        cfgKey = key; cfg = c;
        ticks.innerHTML = '';
        const minor = c.step / 2;
        for (let v = 0; v <= c.max + 0.001; v += minor) {
          const major = Math.abs(v / c.step - Math.round(v / c.step)) < 0.001;
          const a = A0 + ((A1 - A0) * v) / c.max;
          const [x1, y1] = polar(major ? 44 : 48, a);
          const [x2, y2] = polar(52, a);
          const line = document.createElementNS(SVGNS, 'line');
          line.setAttribute('class', 'g-tick' + (major ? '' : ' minor'));
          line.setAttribute('x1', x1); line.setAttribute('y1', y1);
          line.setAttribute('x2', x2); line.setAttribute('y2', y2);
          ticks.appendChild(line);
          if (major) {
            const [tx, ty] = polar(35, a);
            const t = document.createElementNS(SVGNS, 'text');
            t.setAttribute('class', 'g-label');
            t.setAttribute('x', tx); t.setAttribute('y', ty + 3);
            t.textContent = c.fmt ? c.fmt(v) : Math.round(v);
            ticks.appendChild(t);
          }
        }
        if (c.redFrom !== undefined) {
          const a0 = A0 + ((A1 - A0) * c.redFrom) / c.max;
          const a1 = A0 + ((A1 - A0) * Math.min(c.redTo, c.max)) / c.max;
          const [sx, sy] = polar(55.5, a0);
          const [ex, ey] = polar(55.5, a1);
          red.setAttribute('d', 'M' + sx + ' ' + sy + ' A55.5 55.5 0 ' + (a1 - a0 > 180 ? 1 : 0) + ' 1 ' + ex + ' ' + ey);
        } else {
          red.setAttribute('d', '');
        }
      },
      set(v, text) {
        if (!cfg) return;
        const frac = Math.max(0, Math.min(1.02, v / cfg.max));
        const a = A0 + (A1 - A0) * frac + jitter;
        needle.style.transform = 'rotate(' + a.toFixed(1) + 'deg)';
        val.textContent = text;
        g.last = v;
      },
      shake() {                               
        if (!fx || g.last === undefined) return;
        jitter = (Math.random() - 0.5) * 1.6;
        g.set(g.last, val.textContent);
      },
    };
    return g;
  }

  const gaugeDepth = makeGauge($('#gauge-depth'));
  const gaugeOxygen = makeGauge($('#gauge-oxygen'));
  gaugeDepth.configure({ max: 350, step: 50, redFrom: 250, redTo: 350 });
  gaugeOxygen.configure({ max: 100, step: 20, redFrom: 0, redTo: 15 });
  gaugeDepth.set(0, '0'); gaugeOxygen.set(0, '-');
  setInterval(() => { gaugeDepth.shake(); gaugeOxygen.shake(); }, 900);

  const S = {
    state: null,
    sonarFitted: false,
    lastRoute: 1,
    contacts: [],            
  };

  function seed(a, b) {
    const x = Math.sin(a * 12.9898 + b * 78.233) * 43758.5453;
    return x - Math.floor(x);
  }

  function nice(v, step) { return Math.ceil(v / step) * step; }

  function setLamp(id, level) {              
    const li = $(id);
    if (li) li.className = level === 'off' ? '' : level;
  }

  function buildContacts(state) {
    const out = [];
    const old = new Map(S.contacts.map((c) => [c.key, c]));
    if (!state.over && state.routes && state.routes.length) {
      S.sonarFitted = state.routes[0].known;
      state.routes.forEach((r) => {
        const s = seed(state.turn, r.index);
        let dist = 0.4 + s * 0.45;
        if (r.known && (r.kind === 'creature' || r.kind === 'hazard')) {
          dist = Math.max(0.28, Math.min(0.8, 0.82 - r.danger / 55));  
        }
        out.push({ key: 't' + state.turn + 'r' + r.index, bearing: BEARINGS[r.index] || 0, dist,
                   kind: r.known ? r.kind : 'unknown', name: r.known ? r.name : '?', danger: r.danger, known: r.known });
      });
    } else if (!state.over && state.encounter) {
      const e = state.encounter;
      let dist = e.kind === 'loot' ? 0.55 + seed(state.turn, 9) * 0.2 : 0.8 - Math.min(e.danger, 30) / 55;
      dist = Math.max(0.28, Math.min(0.8, dist));
      out.push({ key: 't' + state.turn + 'e', bearing: BEARINGS[S.lastRoute] || 0, dist,
                 kind: e.kind, name: e.name, danger: e.danger, known: true, locked: true });
    }
    out.forEach((c) => { const o = old.get(c.key); c.lastHit = o ? o.lastHit : -1e9; });
    S.contacts = out;
  }

  function update(state) {
    const prev = S.state;
    if (state.turn === 1 && !state.over && (!prev || prev.over || prev.turn > 1)) {
      S.lastRoute = 1; S.sonarFitted = false;
    }
    S.state = state;
    buildContacts(state);

    const depthMax = nice(state.maxDepth * 1.4, 50);
    gaugeDepth.configure({ max: depthMax, step: depthMax / 5, redFrom: state.maxDepth, redTo: depthMax });
    gaugeDepth.set(state.depth, String(state.depth));
    gaugeOxygen.configure({ max: state.oxygenMax, step: state.oxygenMax / 5, redFrom: 0, redTo: state.oxygenMax * 0.15,
                            fmt: (v) => Math.round(v) });
    gaugeOxygen.set(state.oxygen, String(state.oxygen));

    const oxPct = (state.oxygen * 100) / state.oxygenMax;
    const hullPct = (state.hull * 100) / state.hullMax;
    setLamp('#lamp-oxygen', oxPct <= 15 ? 'crit' : oxPct <= 30 ? 'warn' : 'on');
    setLamp('#lamp-hull', hullPct <= 20 ? 'crit' : hullPct <= 40 ? 'warn' : 'on');
    setLamp('#lamp-depth', state.depth > state.maxDepth ? 'crit' : state.depth > state.maxDepth * 0.85 ? 'warn' : 'on');
    setLamp('#lamp-sonar', S.sonarFitted ? 'on' : 'off');

    const list = $('#contact-list');
    if (list) {
      list.textContent = S.contacts.length
        ? S.contacts.map((c) => {
            const side = c.locked ? 'LOCK ' : ['L ', 'A ', 'R '][BEARINGS.indexOf(c.bearing)] || '  ';
            const what = c.known ? c.kind + (c.danger ? ' d' + c.danger : '') : 'unidentified';
            return side + (c.known ? c.name : '?') + '\n   ' + what;
          }).join('\n')
        : 'no contact';
    }
  }

  function noteRoute(index) {
    S.lastRoute = index;
  }

  const canvas = $('#sonar');
  const ctx = canvas.getContext('2d');
  let W = 0, H = 0;

  function fit() {
    const r = canvas.getBoundingClientRect();
    if (!r.width) return;
    const dpr = Math.min(2, window.devicePixelRatio || 1);
    canvas.width = Math.round(r.width * dpr);
    canvas.height = Math.round(r.height * dpr);
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    W = r.width; H = r.height;
  }

  const PH = '176,200,130';                   
  let lastTheta = 0, lastT = 0;

  function drawContact(c, cx, cy, R, alpha, t) {
    const a = (c.bearing * Math.PI) / 180;
    const x = cx + Math.sin(a) * c.dist * R;
    const y = cy - Math.cos(a) * c.dist * R;
    ctx.fillStyle = 'rgba(' + PH + ',' + alpha + ')';
    ctx.strokeStyle = 'rgba(' + PH + ',' + alpha + ')';
    ctx.lineWidth = 1.6;
    if (c.kind === 'unknown') {              
    const flick = 0.8;      
    const g = ctx.createRadialGradient(x, y, 0, x, y, 20);
      g.addColorStop(0, 'rgba(' + PH + ',' + (alpha * 0.75 * flick) + ')');
      g.addColorStop(1, 'rgba(' + PH + ',0)');
      ctx.fillStyle = g;
      ctx.fillRect(x - 20, y - 20, 40, 40);
    } else if (c.kind === 'creature') {
      ctx.beginPath(); ctx.arc(x, y, 4 + Math.min(c.danger, 24) / 5, 0, 7); ctx.fill();
    } else if (c.kind === 'hazard') {
      ctx.beginPath(); ctx.moveTo(x, y - 7); ctx.lineTo(x + 7, y); ctx.lineTo(x, y + 7); ctx.lineTo(x - 7, y); ctx.closePath(); ctx.stroke();
    } else if (c.kind === 'loot') {
      ctx.fillRect(x - 3.5, y - 3.5, 7, 7);
    } else {                                   
      ctx.beginPath(); ctx.arc(x, y, 4, 0, 7); ctx.stroke(); ctx.beginPath(); ctx.arc(x, y, 8, 0, 7); ctx.stroke();
    }
    if (c.locked) {                            
      ctx.beginPath();
      [[-1, -1], [1, -1], [1, 1], [-1, 1]].forEach(([sx, sy]) => {
        ctx.moveTo(x + sx * 13, y + sy * 7); ctx.lineTo(x + sx * 13, y + sy * 13); ctx.lineTo(x + sx * 7, y + sy * 13);
      });
      ctx.stroke();
    }
    if (alpha > 0.3 && c.name !== '?') {
      ctx.font = '10px ' + getComputedStyle(document.body).fontFamily;
      const inward = Math.abs(x - cx) < R * 0.15 ? 1 : (x < cx ? 1 : -1); 
      ctx.textAlign = inward > 0 ? 'left' : 'right';
      ctx.fillText(c.name, x + inward * 12, y + (c.bearing < 0 ? -6 : c.bearing > 0 ? 14 : 3));
    }
  }

  function frame(t) {
    requestAnimationFrame(frame);
    if (!canvas.offsetParent) return;         
    const r = canvas.getBoundingClientRect();
    if (Math.abs(r.width - W) > 1) fit();
    if (!W) return;

    const cx = W / 2, cy = H / 2, R = W / 2 - 6;
    const theta = ((t % PERIOD) / PERIOD) * 360;          
    const crossed = (ang) => (lastTheta <= theta ? ang > lastTheta && ang <= theta : ang > lastTheta || ang <= theta);

    ctx.save();
    ctx.clearRect(0, 0, W, H);
    ctx.beginPath(); ctx.arc(cx, cy, R + 4, 0, 7); ctx.clip();
    ctx.fillStyle = '#0a0e08'; ctx.fillRect(0, 0, W, H);

    ctx.lineWidth = 1;
    [0.25, 0.5, 0.75, 1].forEach((f, i) => {
      ctx.strokeStyle = 'rgba(' + PH + ',' + (i === 3 ? 0.55 : 0.28) + ')';
      ctx.setLineDash(i === 1 ? [4, 4] : []);
      ctx.beginPath(); ctx.arc(cx, cy, R * f, 0, 7); ctx.stroke();
    });
    ctx.setLineDash([]);
    ctx.strokeStyle = 'rgba(' + PH + ',0.22)';
    for (let d = 0; d < 360; d += 30) {
      const a = (d * Math.PI) / 180;
      ctx.beginPath(); ctx.moveTo(cx + Math.sin(a) * R * 0.08, cy - Math.cos(a) * R * 0.08);
      ctx.lineTo(cx + Math.sin(a) * R, cy - Math.cos(a) * R); ctx.stroke();
    }
    ctx.fillStyle = 'rgba(' + PH + ',0.6)';
    ctx.font = '9px ' + getComputedStyle(document.body).fontFamily;
    ctx.textAlign = 'left';
    ctx.textAlign = 'left';
    ['25', '50', '75', '100 m'].forEach((s, i) => {
      const a = (150 * Math.PI) / 180, d = R * (i + 1) * 0.25;
      ctx.fillText(s, cx + Math.sin(a) * d + 3, cy - Math.cos(a) * d - 2);
    });
    ctx.textAlign = 'center';
    [['000', 0], ['090', 90], ['180', 180], ['270', 270]].forEach(([s, d]) => {
      const a = (d * Math.PI) / 180;
      ctx.fillText(s, cx + Math.sin(a) * (R - 14), cy - Math.cos(a) * (R - 14) + 3);
    });

    for (let i = 0; i < 36; i++) {
      const a0 = ((theta - (i + 1) * 1.6 - 90) * Math.PI) / 180;
      const a1 = ((theta - i * 1.6 - 90) * Math.PI) / 180;
      ctx.fillStyle = 'rgba(' + PH + ',' + (0.34 * Math.pow(1 - i / 36, 2)) + ')';
      ctx.beginPath(); ctx.moveTo(cx, cy); ctx.arc(cx, cy, R, a0, a1); ctx.closePath(); ctx.fill();
    }
    const sa = ((theta - 90) * Math.PI) / 180;
    ctx.strokeStyle = 'rgba(' + PH + ',0.9)'; ctx.lineWidth = 1.5;
    ctx.beginPath(); ctx.moveTo(cx, cy); ctx.lineTo(cx + Math.cos(sa) * R, cy + Math.sin(sa) * R); ctx.stroke();

    S.contacts.forEach((c) => {
      if (crossed((c.bearing + 360) % 360)) c.lastHit = t;
      const alpha = 0.9;      
      drawContact(c, cx, cy, R, alpha, t);
    });

    if (!S.contacts.length) {
      ctx.fillStyle = 'rgba(' + PH + ',0.5)'; ctx.font = '11px ' + getComputedStyle(document.body).fontFamily;
      ctx.textAlign = 'center'; ctx.fillText('NO CONTACT', cx, cy + R * 0.62);
    }
    ctx.restore();

    const g = ctx.createRadialGradient(cx, cy, R * 0.6, cx, cy, R + 6);
    g.addColorStop(0, 'rgba(0,0,0,0)'); g.addColorStop(1, 'rgba(0,0,0,.55)');
    ctx.fillStyle = g; ctx.fillRect(0, 0, W, H);
    ctx.fillStyle = 'rgba(0,0,0,.16)';
    for (let y = 0; y < H; y += 3) ctx.fillRect(0, y, W, 1);

    lastTheta = theta; lastT = t;
  }
  requestAnimationFrame(frame);
  window.addEventListener('resize', fit);

  window.Instruments = { update, noteRoute };
})();
