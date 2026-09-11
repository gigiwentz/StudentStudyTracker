const API_BASE = '';

async function api(path, options = {}) {
  const url = `${API_BASE}${path}`;
  const res = await fetch(url, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers },
  });
  if (!res.ok) throw new Error(await res.text());
  if (res.status === 204) return null;
  return res.json();
}

function $(id) {
  return document.getElementById(id);
}

function formatHHMM(minutes) {
  const h = Math.floor(minutes / 60);
  const m = Math.floor(minutes % 60);
  return `${h}:${String(m).padStart(2, '0')}`;
}

async function loadStudyHours() {
  try {
    const d = await api('/api/study-hours');
    $('studyHoursRemaining').textContent = d.remainingHHMM ?? '--:--';
  } catch (e) {
    $('studyHoursRemaining').textContent = '--:--';
    console.warn('Study hours:', e);
  }
}

async function loadAssignments() {
  try {
    const list = await api('/api/assignments');
    const select = $('assignmentSelect');
    select.innerHTML = '<option value="">— Select —</option>';
    list.forEach((a) => {
      const opt = document.createElement('option');
      opt.value = a.id;
      opt.textContent = a.name;
      select.appendChild(opt);
    });
  } catch (e) {
    console.warn('Assignments:', e);
  }
}

async function loadProgress() {
  try {
    const list = await api('/api/progress');
    const ul = $('progressList');
    ul.innerHTML = '';
    const placeholder = $('progressPlaceholder');
    if (!list || list.length === 0) {
      placeholder.style.display = 'block';
      return;
    }
    placeholder.style.display = 'none';
    list.forEach((item) => {
      const li = document.createElement('li');
      li.innerHTML = `<span>${escapeHtml(item.assignmentName)}</span><span class="minutes">${formatHHMM(item.loggedMinutes)} logged</span>`;
      ul.appendChild(li);
    });
  } catch (e) {
    console.warn('Progress:', e);
  }
}

function escapeHtml(s) {
  const div = document.createElement('div');
  div.textContent = s;
  return div.innerHTML;
}

async function refresh() {
  await Promise.all([loadStudyHours(), loadAssignments(), loadProgress()]);
}

$('addAssignmentBtn').addEventListener('click', async () => {
  const name = $('newAssignmentName').value?.trim();
  if (!name) return;
  try {
    await api('/api/assignments', {
      method: 'POST',
      body: JSON.stringify({ name }),
    });
    $('newAssignmentName').value = '';
    await refresh();
  } catch (e) {
    console.error(e);
    alert('Failed to add assignment');
  }
});

$('logWorkBtn').addEventListener('click', async () => {
  const assignmentId = $('assignmentSelect').value;
  const minutes = parseInt($('minutesInput').value, 10);
  if (!assignmentId || !minutes || minutes < 1) {
    alert('Select an assignment and enter minutes.');
    return;
  }
  try {
    await api('/api/work-sessions', {
      method: 'POST',
      body: JSON.stringify({ assignmentId, durationMinutes: minutes }),
    });
    await refresh();
  } catch (e) {
    console.error(e);
    alert('Failed to log work');
  }
});

$('newAssignmentName').addEventListener('keydown', (e) => {
  if (e.key === 'Enter') $('addAssignmentBtn').click();
});

refresh();
