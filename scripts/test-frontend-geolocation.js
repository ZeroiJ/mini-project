// Regression test for the stuck "Locating…" bug on the student page.
// No browser needed: geolocation and the DOM are stubbed and the real
// frontend/app.js + student.html code is executed in a vm context.
//
//   node scripts/test-frontend-geolocation.js
const fs = require('fs'), vm = require('vm'), path = require('path'), assert = require('assert');
const ROOT = path.join(__dirname, '..');
const FRONTEND = path.join(ROOT, 'frontend');

const appjs = fs.readFileSync(path.join(FRONTEND, 'app.js'), 'utf8');
const html = fs.readFileSync(path.join(FRONTEND, 'student.html'), 'utf8');
const inline = [...html.matchAll(/<script>([\s\S]*?)<\/script>/g)].map(m => m[1]);
assert.strictEqual(inline.length, 1, 'expected exactly one inline script in student.html');

const el = () => ({ textContent: '', innerHTML: '', hidden: false, disabled: false });

function makeSandbox(queue) {
  const els = { s: el(), go: el(), m: el(), e: el() };
  const calls = [];
  const geoError = code => ({ code, message: 'stub', PERMISSION_DENIED: 1, POSITION_UNAVAILABLE: 2, TIMEOUT: 3 });
  const sandbox = {
    console, URLSearchParams,
    API_BASE: 'https://example.test',
    location: { search: '?s=abc123', href: '' },
    localStorage: { getItem: k => ({ token: 'tok_1_x', role: 'STUDENT' }[k] ?? null), setItem() {}, clear() {} },
    document: { getElementById: id => els[id] },
    alert: () => {},
    navigator: {
      geolocation: {
        getCurrentPosition(ok, err, opts) {
          calls.push({ opts, ok, err });
          const step = queue.shift();
          if (!step) return;                       // never calls back -> the old hang
          if (step === 'ok') ok({ coords: { latitude: 19.0761, longitude: 72.8779 } });
          else err(geoError(step));
        },
      },
    },
    fetch: async url => {
      const body = String(url).includes('/api/attendance')
        ? { ok: true, distanceMeters: 23.78 }
        : { subject: 'FSJP', endsAt: '2026-10-21T10:55:08Z', expired: false };
      return { ok: true, status: 200, json: async () => body };
    },
    ...els,
  };
  vm.createContext(sandbox);
  vm.runInContext(appjs, sandbox);
  vm.runInContext(inline[0], sandbox);
  return { sandbox, els, calls };
}

const results = [];
const check = (name, fn) => { try { fn(); results.push(['PASS', name]); } catch (e) { results.push(['FAIL', name + ' -> ' + e.message]); } };
const tick = () => new Promise(r => setTimeout(r, 0));

(async () => {
  // A. happy path: high-accuracy first attempt succeeds
  const a = makeSandbox(['ok']);
  await tick(); await a.sandbox.mark();
  check('high-accuracy fix requested with a finite timeout', () => {
    assert.strictEqual(a.calls[0].opts.enableHighAccuracy, true);
    assert.ok(Number.isFinite(a.calls[0].opts.timeout) && a.calls[0].opts.timeout > 0, 'timeout must be finite and positive');
  });
  check('success shows the distance and re-enables the button', () => {
    assert.match(a.els.m.textContent, /Present\. 24m from classroom\./);
    assert.strictEqual(a.els.go.disabled, false);
    assert.strictEqual(a.calls.length, 1);
  });

  // B. the reported bug: high-accuracy times out -> fast fallback, no hang
  const b = makeSandbox([3, 'ok']);
  await tick(); await b.sandbox.mark();
  check('timed-out high-accuracy attempt falls back instead of hanging', () => {
    assert.strictEqual(b.calls.length, 2);
    assert.strictEqual(b.calls[1].opts.enableHighAccuracy, false);
    assert.ok(Number.isFinite(b.calls[1].opts.timeout) && b.calls[1].opts.timeout > 0);
  });
  check('the fallback fix still marks attendance', () => assert.match(b.els.m.textContent, /Present\./));
  check('total wait is bounded under 15 s', () => {
    const total = b.calls.reduce((s, c) => s + (c.opts.timeout || 0), 0);
    assert.ok(total <= 15000, 'bounded wait, got ' + total);
  });

  // C. permission denied: no retry, clear message
  const c = makeSandbox([1]);
  await tick(); await c.sandbox.mark();
  check('permission denied is not retried and is reported', () => {
    assert.strictEqual(c.calls.length, 1);
    assert.match(c.els.e.textContent, /GPS denied/);
    assert.strictEqual(c.els.go.disabled, false);
  });

  // D. both attempts fail: message, button usable again
  const d = makeSandbox([3, 3]);
  await tick(); await d.sandbox.mark();
  check('missing fix ends with a clear message, not a stuck button', () => {
    assert.strictEqual(d.calls.length, 2);
    assert.match(d.els.m.textContent, /No GPS fix/);
    assert.doesNotMatch(d.els.m.textContent, /Locating/);
    assert.strictEqual(d.els.go.disabled, false);
  });

  // E. no geolocation call anywhere in the frontend may omit a finite timeout
  check('frontend never calls getCurrentPosition without a finite timeout', () => {
    for (const f of fs.readdirSync(FRONTEND).filter(f => f.endsWith('.js') || f.endsWith('.html'))) {
      const src = fs.readFileSync(path.join(FRONTEND, f), 'utf8');
      for (const call of src.match(/getCurrentPosition\([^)]*\)/g) || []) {
        if (call.includes('(res, rej, o)')) continue;      // the shared wrapper
        assert.ok(/timeout/.test(call), `${f}: ${call} has no timeout`);
      }
    }
  });

  for (const [s, n] of results) console.log(s, n);
  const failed = results.filter(r => r[0] === 'FAIL').length;
  console.log(`\n=== ${results.length - failed} passed, ${failed} failed ===`);
  process.exit(failed ? 1 : 0);
})();
