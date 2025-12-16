function setActiveNav(id) {
  document.querySelectorAll('[data-nav]').forEach((el) => {
    if (el.getAttribute('data-nav') === id) el.classList.add('active');
    else el.classList.remove('active');
  });
}

async function fetchJsonSafe(url, options) {
  try {
    const res = await fetch(url, options);
    try {
      return await res.json();
    } catch (e) {
      return { ok: false, message: `서버 응답이 JSON이 아닙니다. (HTTP ${res.status})` };
    }
  } catch (e) {
    return { ok: false, message: `네트워크 오류: ${e.message || e}` };
  }
}


