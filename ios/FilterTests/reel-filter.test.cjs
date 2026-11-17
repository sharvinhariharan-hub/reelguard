const {test} = require('node:test');
const assert = require('node:assert/strict');
const {readFileSync} = require('node:fs');
const {join} = require('node:path');
const {JSDOM} = require('jsdom');
const source = readFileSync(join(__dirname, '../App/Resources/reel-filter.js'), 'utf8');

function page(html = '', path = '/', enabled = true) {
  const dom = new JSDOM(html, {url: `https://www.instagram.com${path}`, runScripts: 'outside-only', pretendToBeVisual: true});
  const messages = [];
  const pauses = [];
  dom.window.HTMLMediaElement.prototype.pause = function() { pauses.push(this.id); };
  dom.window.webkit = {messageHandlers: {reelguard: {postMessage: value => messages.push(value)}}};
  dom.window.__REELGUARD_INITIAL_ENABLED__ = enabled;
  dom.window.eval(source);
  const close = () => {
    dom.window.dispatchEvent(new dom.window.PageTransitionEvent('pagehide'));
    dom.window.close();
  };
  return {dom, w: dom.window, d: dom.window.document, messages, pauses, close};
}
const hidden = el => el.hasAttribute('data-reelguard-hidden');

test('blocks both Reel URL forms, preserves posts, DMs, Stories and lookalike hosts', () => {
  const p = page();
  for (const url of ['/reel/a/', '/reels/', '/%72eel/a', '/REEL/a']) assert.equal(p.w.ReelGuardFilter.isReelURL(url), true);
  for (const url of ['/p/a/', '/stories/a/', '/direct/inbox/', 'https://evil.example/reel/a', '/reelguard/']) assert.equal(p.w.ReelGuardFilter.isReelURL(url), false);
  p.close();
});
test('hides Reel articles, preserves photo articles and pauses only blocked media', () => {
  const p = page('<article id="reel"><a href="/reel/a/"><video id="rv"></video></a></article><article id="photo"><a href="/p/a/">Photo</a><video id="pv"></video></article>');
  assert.equal(hidden(p.d.querySelector('#reel')), true);
  assert.equal(hidden(p.d.querySelector('#photo')), false);
  assert.deepEqual(p.pauses, ['rv']);
  p.close();
});
test('a shared Reel is hidden without removing its DM conversation', () => {
  const p = page('<article id="conversation"><p>Hello friend</p><a id="card" href="/reel/shared/">Shared Reel</a><a id="post" href="/p/a/">Photo</a></article>', '/direct/t/123/');
  assert.equal(hidden(p.d.querySelector('#card')), true);
  assert.equal(hidden(p.d.querySelector('#conversation')), false);
  assert.equal(hidden(p.d.querySelector('#post')), false);
  assert.equal(p.d.querySelector('p').textContent, 'Hello friend');
  p.close();
});
test('off switch restores elements without changing pre-existing display styles', () => {
  const p = page('<a id="r" style="display:inline" href="/reel/a/">Reel</a>');
  p.w.ReelGuardFilter.setEnabled(false);
  assert.equal(hidden(p.d.querySelector('#r')), false);
  assert.equal(p.d.querySelector('#r').style.display, 'inline');
  p.w.ReelGuardFilter.setEnabled(true);
  assert.equal(hidden(p.d.querySelector('#r')), true);
  p.close();
});
test('disabled startup does not hide Reels', () => {
  const p = page('<a id="r" href="/reel/a/">Reel</a>', '/', false);
  assert.equal(hidden(p.d.querySelector('#r')), false);
  p.close();
});
test('SPA pushState and replaceState cannot enter Reels while blocking is on', () => {
  const p = page();
  p.w.history.pushState({}, '', '/reel/a/');
  p.w.history.replaceState({}, '', '/reels/');
  assert.equal(p.w.location.pathname, '/');
  assert.equal(p.messages.length, 2);
  p.w.ReelGuardFilter.setEnabled(false);
  p.w.history.pushState({}, '', '/reel/a/');
  assert.equal(p.w.location.pathname, '/reel/a/');
  p.close();
});
test('clicking a shared Reel is cancelled even before an observer refresh', () => {
  const p = page();
  const link = p.d.createElement('a'); link.href = '/reel/a/'; p.d.body.append(link);
  const event = new p.w.MouseEvent('click', {bubbles: true, cancelable: true});
  assert.equal(link.dispatchEvent(event), false);
  assert.equal(p.messages.length, 1);
  p.close();
});
test('new cards and changed hrefs are filtered after dynamic updates', async () => {
  const p = page('<a id="a" href="/p/a/">Post</a>');
  p.d.querySelector('#a').href = '/reel/a/';
  p.d.body.insertAdjacentHTML('beforeend', '<a id="b" href="/reels/">Reels</a>');
  await new Promise(resolve => p.w.setTimeout(resolve, 60));
  assert.equal(hidden(p.d.querySelector('#a')), true);
  assert.equal(hidden(p.d.querySelector('#b')), true);
  p.close();
});
test('a directly loaded Reel page is concealed and playback is paused', () => {
  const p = page('<video id="v"></video>', '/reel/a/');
  assert.equal(p.d.documentElement.hasAttribute('data-reelguard-route'), true);
  assert.deepEqual(p.pauses, ['v']);
  p.w.ReelGuardFilter.setEnabled(false);
  assert.equal(p.d.documentElement.hasAttribute('data-reelguard-route'), false);
  p.close();
});
test('attempted replay of hidden Reel media is paused again', () => {
  const p = page('<a href="/reel/a/"><video id="r"></video></a><video id="post"></video>');
  p.pauses.length = 0;
  p.d.querySelector('#r').dispatchEvent(new p.w.Event('play'));
  p.d.querySelector('#post').dispatchEvent(new p.w.Event('play'));
  assert.deepEqual(p.pauses, ['r']);
  p.close();
});
test('filter resumes after returning from the page cache', () => {
  const p = page();
  p.w.dispatchEvent(new p.w.PageTransitionEvent('pagehide'));
  p.d.body.insertAdjacentHTML('beforeend', '<a id="new" href="/reel/a/">Reel</a>');
  p.w.dispatchEvent(new p.w.PageTransitionEvent('pageshow', {persisted: true}));
  assert.equal(hidden(p.d.querySelector('#new')), true);
  p.close();
});
