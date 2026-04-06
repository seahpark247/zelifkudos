// Pass-through service worker — no caching.
// GSP is server-rendered, caching would serve stale pages.
// This file exists only to satisfy PWA install requirements.

self.addEventListener('fetch', function(event) {
  event.respondWith(fetch(event.request));
});
