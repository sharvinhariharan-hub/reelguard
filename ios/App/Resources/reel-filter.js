(() => {
  'use strict';
  if (window.ReelGuardFilter) return;
  let enabled = window.__REELGUARD_INITIAL_ENABLED__ !== false;
  let scheduled = false;
  let frame = null;
  const marked = new Set();
  const attr = 'data-reelguard-hidden';
  const routeAttr = 'data-reelguard-route';
  const isReel = value => {
    try {
      const url = new URL(value, location.href);
      if (url.protocol !== 'https:' || !['instagram.com', 'www.instagram.com'].includes(url.hostname.toLowerCase())) return false;
      const first = decodeURIComponent(url.pathname).split('/').filter(Boolean)[0]?.toLowerCase();
      return first === 'reel' || first === 'reels';
    } catch { return false; }
  };
  const notify = () => window.webkit?.messageHandlers?.reelguard?.postMessage({type: 'blocked'});
  const pause = element => {
    for (const video of element.querySelectorAll('video')) video.pause();
    if (element.tagName === 'VIDEO') element.pause();
  };
  function ensureStyle() {
    if (!document.documentElement || document.getElementById('reelguard-style')) return;
    const style = document.createElement('style');
    style.id = 'reelguard-style';
    style.textContent = `[${attr}] { display: none !important; } html[${routeAttr}] body { visibility: hidden !important; }`;
    document.documentElement.append(style);
  }
  function apply() {
    scheduled = false;
    frame = null;
    if (!document.documentElement) return;
    ensureStyle();
    for (const element of marked) element.removeAttribute(attr);
    marked.clear();
    document.documentElement?.removeAttribute(routeAttr);
    if (!enabled) return;
    if (isReel(location.href)) {
      document.documentElement?.setAttribute(routeAttr, '');
      pause(document);
    }
    for (const link of document.querySelectorAll('a[href]')) {
      if (!isReel(link.href)) continue;
      // Preserve a DM conversation; remove only its shared Reel card/link.
      const inDM = location.pathname.toLowerCase().startsWith('/direct/');
      const target = inDM ? link : (link.closest('article') || link);
      target.setAttribute(attr, '');
      marked.add(target);
      pause(target);
    }
  }
  function schedule() {
    if (!scheduled) { scheduled = true; frame = requestAnimationFrame(apply); }
  }
  document.addEventListener('click', event => {
    if (!enabled) return;
    const link = event.target instanceof Element ? event.target.closest('a[href]') : null;
    if (link && isReel(link.href)) {
      event.preventDefault(); event.stopImmediatePropagation(); notify();
    }
  }, true);
  document.addEventListener('play', event => {
    if (enabled && event.target instanceof HTMLMediaElement &&
        (isReel(location.href) || event.target.closest(`[${attr}]`))) event.target.pause();
  }, true);
  for (const method of ['pushState', 'replaceState']) {
    const original = history[method];
    history[method] = function(state, title, url) {
      if (enabled && url != null && isReel(url)) { notify(); return; }
      const result = original.apply(this, arguments);
      schedule();
      return result;
    };
  }
  window.addEventListener('popstate', () => { apply(); if (enabled && isReel(location.href)) notify(); });
  const observer = new MutationObserver(schedule);
  const observe = () => observer.observe(document, {subtree: true, childList: true, attributes: true, attributeFilter: ['href']});
  window.addEventListener('pagehide', () => {
    observer.disconnect();
    if (frame !== null) cancelAnimationFrame(frame);
    frame = null; scheduled = false;
  });
  window.addEventListener('pageshow', () => { observe(); apply(); });
  observe();
  window.ReelGuardFilter = Object.freeze({
    setEnabled(value) { enabled = value === true; apply(); },
    isReelURL: isReel
  });
  apply();
})();
