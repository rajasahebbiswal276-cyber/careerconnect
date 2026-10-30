/* CareerConnect single-page frontend (plain JavaScript, talks to the Spring Boot REST API). */
const S = { token: localStorage.getItem('cc_token'), user: JSON.parse(localStorage.getItem('cc_user') || 'null') };
const app = document.getElementById('app');
const STATUSES = ['APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'REJECTED', 'HIRED'];
const TYPES = ['FULL_TIME', 'PART_TIME', 'INTERNSHIP', 'CONTRACT', 'REMOTE'];

/* ---------- helpers ---------- */
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const nice = s => String(s).replace(/_/g, ' ').toLowerCase().replace(/^\w/, c => c.toUpperCase());
const lakh = n => '₹' + (n / 100000).toFixed(1).replace(/\.0$/, '') + 'L';
const pay = j => (j.salaryMin || j.salaryMax)
  ? [j.salaryMin && lakh(j.salaryMin), j.salaryMax && lakh(j.salaryMax)].filter(Boolean).join(' – ') + ' / yr' : 'Salary not disclosed';
const when = d => new Date(d).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
const chips = (list, have = []) => (list || []).map(s => `<span class="chip ${have.includes(s.toLowerCase()) ? 'have' : ''}">${esc(s)}</span>`).join('');
const csv = v => v.split(',').map(s => s.trim()).filter(Boolean);

function toast(msg, isErr) {
  const t = document.getElementById('toast');
  t.textContent = msg; t.className = 'show' + (isErr ? ' err' : '');
  clearTimeout(toast.t); toast.t = setTimeout(() => t.className = '', 3200);
}

async function api(path, { method = 'GET', body } = {}) {
  const res = await fetch('/api' + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(S.token ? { Authorization: 'Bearer ' + S.token } : {}) },
    body: body ? JSON.stringify(body) : undefined
  });
  const data = res.status === 204 ? null : await res.json().catch(() => null);
  if (!res.ok) {
    if (res.status === 401 && S.token && !path.startsWith('/auth')) { logout(); }
    const fields = data && data.fieldErrors ? Object.values(data.fieldErrors).join(', ') : null;
    const err = new Error(fields || (data && data.message) || 'Request failed');
    err.status = res.status; throw err;
  }
  return data;
}
const run = async fn => { try { await fn(); } catch (e) { toast(e.message, true); } };

function setSession(auth) {
  S.token = auth.token; S.user = { id: auth.id, fullName: auth.fullName, email: auth.email, role: auth.role };
  localStorage.setItem('cc_token', S.token); localStorage.setItem('cc_user', JSON.stringify(S.user));
}
function logout() {
  S.token = null; S.user = null; localStorage.removeItem('cc_token'); localStorage.removeItem('cc_user');
  location.hash = '#/login'; route();
}

/* ---------- navigation ---------- */
function renderNav() {
  const u = S.user, here = location.hash || '#/';
  const links = [['#/', 'Jobs']];
  if (u?.role === 'CANDIDATE') links.push(['#/applications', 'My applications'], ['#/profile', 'Profile']);
  if (u?.role === 'RECRUITER') links.push(['#/my-jobs', 'My jobs'], ['#/post-job', 'Post a job'], ['#/company', 'Company']);
  if (u?.role === 'ADMIN') links.push(['#/admin', 'Admin']);
  const a = links.map(([h, t]) => `<a href="${h}" class="${here === h ? 'on' : ''}">${t}</a>`).join('');
  document.getElementById('nav').innerHTML = a + (u
    ? `<span class="who">${esc(u.fullName)} · ${nice(u.role)}</span><button id="out">Log out</button>`
    : `<a href="#/login">Log in</a><a href="#/register" class="on">Sign up</a>`);
  const out = document.getElementById('out'); if (out) out.onclick = logout;
}

const routes = [
  [/^#\/?$/, pageJobs], [/^#\/login$/, pageLogin], [/^#\/register$/, pageRegister],
  [/^#\/applications$/, pageMyApplications], [/^#\/profile$/, pageProfile],
  [/^#\/company$/, pageCompany], [/^#\/post-job(?:\/(\d+))?$/, pagePostJob],
  [/^#\/my-jobs$/, pageMyJobs], [/^#\/applicants\/(\d+)$/, pageApplicants], [/^#\/admin$/, pageAdmin]
];
function route() {
  renderNav();
  const h = location.hash || '#/';
  for (const [re, fn] of routes) { const m = h.match(re); if (m) { run(() => fn(...m.slice(1))); return; } }
  app.innerHTML = '<p class="empty">Page not found.</p>';
}
window.addEventListener('hashchange', route);

const needRole = role => {
  if (!S.user || S.user.role !== role) { location.hash = '#/login'; return false; }
  return true;
};

/* ---------- public: job search ---------- */
const Q = { keyword: '', location: '', jobType: '', skill: '', minSalary: '', sortBy: 'postedAt', direction: 'desc', page: 0 };

async function pageJobs() {
  app.innerHTML = `
    <section class="hero">
      <h1>Find the job that fits how you work.</h1>
      <p>Search open roles from verified companies, apply in one click and follow every step of your application.</p>
      <div class="search">
        <input id="q" placeholder="Job title, company or keyword" value="${esc(Q.keyword)}">
        <input id="loc" placeholder="Location" value="${esc(Q.location)}">
        <select id="type"><option value="">Any type</option>${TYPES.map(t => `<option value="${t}" ${Q.jobType === t ? 'selected' : ''}>${nice(t)}</option>`).join('')}</select>
        <button class="btn" id="go">Search</button>
      </div>
      <div class="row">
        <input id="skill" placeholder="Skill, e.g. Java" value="${esc(Q.skill)}" style="width:170px">
        <select id="sal"><option value="">Any salary</option>${[300000, 600000, 1000000, 1500000].map(v => `<option value="${v}" ${Q.minSalary == v ? 'selected' : ''}>${lakh(v)}+</option>`).join('')}</select>
        <select id="sort">
          <option value="postedAt|desc">Newest first</option><option value="salaryMax|desc">Highest salary</option>
          <option value="title|asc">Title A–Z</option>
        </select>
      </div>
    </section>
    <div id="results"></div>`;
  document.getElementById('sort').value = Q.sortBy + '|' + Q.direction;
  const apply = () => {
    Object.assign(Q, { keyword: q.value, location: loc.value, jobType: type.value, skill: skill.value, minSalary: sal.value, page: 0 });
    [Q.sortBy, Q.direction] = sort.value.split('|'); loadJobs();
  };
  go.onclick = apply; sort.onchange = apply; sal.onchange = apply; type.onchange = apply;
  [q, loc, skill].forEach(i => i.onkeydown = e => { if (e.key === 'Enter') apply(); });
  loadJobs();
}

async function loadJobs() {
  const p = new URLSearchParams({ page: Q.page, size: 6, sortBy: Q.sortBy, direction: Q.direction });
  ['keyword', 'location', 'jobType', 'skill', 'minSalary'].forEach(k => Q[k] && p.set(k, Q[k]));
  const res = await api('/jobs?' + p);
  const box = document.getElementById('results');
  if (!res.content.length) { box.innerHTML = '<p class="empty">No jobs match your search. Try fewer filters.</p>'; return; }
  window.__jobs = Object.fromEntries(res.content.map(j => [j.id, j]));
  box.innerHTML = `<p class="meta">${res.totalElements} job${res.totalElements === 1 ? '' : 's'} found</p>` +
    res.content.map(j => `
      <article class="card job" onclick="openJob(${j.id})">
        <div><h3>${esc(j.title)}</h3>
          <div class="meta"><span>${esc(j.companyName)}</span><span>${esc(j.location)}</span><span>${nice(j.jobType)}</span>
          ${j.experienceRequired != null ? `<span>${j.experienceRequired}+ yrs</span>` : ''}</div>
          <div class="chips">${chips(j.skills)}</div></div>
        <div class="pay">${pay(j)}<div class="meta" style="justify-content:flex-end">${new Date(j.postedAt).toLocaleDateString()}</div></div>
      </article>`).join('') + `
    <div class="pager"><button class="btn ghost sm" ${res.page === 0 ? 'disabled' : ''} onclick="Q.page--;run(loadJobs)">Previous</button>
      Page ${res.page + 1} of ${res.totalPages}
      <button class="btn ghost sm" ${res.page + 1 >= res.totalPages ? 'disabled' : ''} onclick="Q.page++;run(loadJobs)">Next</button></div>`;
}

function openJob(id) {
  const j = window.__jobs[id], m = document.getElementById('modal');
  const role = S.user?.role;
  document.getElementById('modalBody').innerHTML = `
    <h2>${esc(j.title)}</h2>
    <div class="meta"><span>${esc(j.companyName)}</span><span>${esc(j.location)}</span><span>${nice(j.jobType)}</span><span>${pay(j)}</span></div>
    <div class="chips">${chips(j.skills)}</div>
    <p class="desc">${esc(j.description)}</p>
    ${role === 'CANDIDATE' ? `<label>Cover letter (optional)<textarea id="cover" maxlength="2000" placeholder="Why are you a good fit?"></textarea></label>
        <div class="row"><button class="btn" id="applyBtn">Apply now</button><button class="btn ghost" onclick="modal.close()">Close</button></div>`
      : role === 'ADMIN' ? `<div class="row"><button class="btn danger" id="rm">Remove this job</button><button class="btn ghost" onclick="modal.close()">Close</button></div>`
      : `<div class="row">${role ? '' : '<a class="btn" href="#/login" onclick="modal.close()">Log in to apply</a>'}<button class="btn ghost" onclick="modal.close()">Close</button></div>`}`;
  m.showModal();
  const ab = document.getElementById('applyBtn');
  if (ab) ab.onclick = () => run(async () => {
    await api(`/jobs/${id}/apply`, { method: 'POST', body: { coverLetter: cover.value } });
    m.close(); toast('Application sent. Track it under My applications.');
  });
  const rm = document.getElementById('rm');
  if (rm) rm.onclick = () => run(async () => { await api('/admin/jobs/' + id, { method: 'DELETE' }); m.close(); toast('Job removed'); loadJobs(); });
}

/* ---------- auth ---------- */
function pageLogin() {
  app.innerHTML = `<h2>Welcome back</h2><br>
    <form class="stack" id="f">
      <label>Email<input type="email" name="email" required></label>
      <label>Password<input type="password" name="password" required></label>
      <button class="btn">Log in</button>
    </form>
    <p class="demo">Demo accounts: <code>candidate@demo.com</code> / <code>Candidate@123</code> ·
      <code>recruiter@demo.com</code> / <code>Recruiter@123</code> · <code>admin@careerconnect.com</code> / <code>Admin@123</code></p>`;
  f.onsubmit = e => { e.preventDefault(); run(async () => {
    setSession(await api('/auth/login', { method: 'POST', body: Object.fromEntries(new FormData(f)) }));
    location.hash = '#/'; route();
  }); };
}
function pageRegister() {
  app.innerHTML = `<h2>Create your account</h2><br>
    <form class="stack" id="f">
      <label>Full name<input name="fullName" required></label>
      <label>Email<input type="email" name="email" required></label>
      <label>Password (min 6 characters)<input type="password" name="password" minlength="6" required></label>
      <label>I am a<select name="role"><option value="CANDIDATE">Candidate looking for a job</option><option value="RECRUITER">Recruiter hiring talent</option></select></label>
      <button class="btn">Sign up</button>
    </form>`;
  f.onsubmit = e => { e.preventDefault(); run(async () => {
    const auth = await api('/auth/register', { method: 'POST', body: Object.fromEntries(new FormData(f)) });
    setSession(auth); location.hash = auth.role === 'RECRUITER' ? '#/company' : '#/profile'; route();
  }); };
}

/* ---------- candidate ---------- */
async function pageProfile() {
  if (!needRole('CANDIDATE')) return;
  const u = await api('/users/me');
  app.innerHTML = `<h2>Your profile</h2><p class="meta">Recruiters see this when you apply. Your skills decide your match score.</p><br>
    <form class="stack" id="f">
      <label>Full name<input name="fullName" value="${esc(u.fullName)}" required></label>
      <label>Headline<input name="headline" value="${esc(u.headline)}" placeholder="Fresher Java developer | Spring Boot"></label>
      <div class="two"><label>Phone<input name="phone" value="${esc(u.phone)}"></label>
        <label>Location<input name="location" value="${esc(u.location)}"></label></div>
      <div class="two"><label>Experience (years)<input type="number" min="0" name="experienceYears" value="${u.experienceYears ?? 0}"></label>
        <label>Resume link<input name="resumeUrl" value="${esc(u.resumeUrl)}" placeholder="https://drive.google.com/..."></label></div>
      <label>Skills (comma separated)<input name="skills" value="${esc((u.skills || []).join(', '))}" placeholder="Java, Spring Boot, MySQL"></label>
      <button class="btn">Save profile</button>
    </form>`;
  f.onsubmit = e => { e.preventDefault(); run(async () => {
    const d = Object.fromEntries(new FormData(f));
    d.skills = csv(d.skills); d.experienceYears = d.experienceYears === '' ? null : Number(d.experienceYears);
    await api('/users/me', { method: 'PUT', body: d }); toast('Profile saved');
  }); };
}

const timeline = t => `<ul class="tl">${t.map(x => `<li><span class="pill ${x.status}">${nice(x.status)}</span> ${esc(x.note || '')}<small>${when(x.changedAt)}</small></li>`).join('')}</ul>`;

async function pageMyApplications() {
  if (!needRole('CANDIDATE')) return;
  const res = await api('/applications/my');
  app.innerHTML = `<h2>My applications</h2><br>` + (res.content.length ? res.content.map(a => `
    <article class="card"><div class="row" style="justify-content:space-between;margin:0">
      <div><h3>${esc(a.jobTitle)}</h3><div class="meta"><span>${esc(a.companyName)}</span><span>Applied ${when(a.appliedAt)}</span></div></div>
      <span class="pill ${a.status}">${nice(a.status)}</span></div>
      ${a.recruiterNote ? `<p class="meta">Recruiter note: ${esc(a.recruiterNote)}</p>` : ''}
      ${timeline(a.timeline)}
      ${a.status !== 'HIRED' ? `<button class="btn danger sm" onclick="withdraw(${a.id})">Withdraw</button>` : ''}
    </article>`).join('') : '<p class="empty">You have not applied to any job yet. <a href="#/">Browse jobs</a></p>');
}
function withdraw(id) {
  if (!confirm('Withdraw this application?')) return;
  run(async () => { await api('/applications/' + id, { method: 'DELETE' }); toast('Application withdrawn'); pageMyApplications(); });
}

/* ---------- recruiter ---------- */
async function pageCompany() {
  if (!needRole('RECRUITER')) return;
  let c = {};
  try { c = await api('/recruiter/company'); } catch (e) { if (e.status !== 404) throw e; }
  app.innerHTML = `<h2>Company profile</h2><p class="meta">Create this once, then you can post jobs.</p><br>
    <form class="stack" id="f">
      <label>Company name<input name="name" value="${esc(c.name)}" required></label>
      <label>About<textarea name="description">${esc(c.description)}</textarea></label>
      <div class="two"><label>Website<input name="website" value="${esc(c.website)}"></label>
        <label>Location<input name="location" value="${esc(c.location)}"></label></div>
      <button class="btn">Save company</button>
    </form>`;
  f.onsubmit = e => { e.preventDefault(); run(async () => {
    await api('/recruiter/company', { method: 'PUT', body: Object.fromEntries(new FormData(f)) }); toast('Company saved');
  }); };
}

async function pagePostJob(id) {
  if (!needRole('RECRUITER')) return;
  let j = {};
  if (id) j = await api('/jobs/' + id);
  app.innerHTML = `<h2>${id ? 'Edit job' : 'Post a job'}</h2><br>
    <form class="stack" id="f">
      <label>Title<input name="title" value="${esc(j.title)}" required></label>
      <div class="two"><label>Location<input name="location" value="${esc(j.location)}" required></label>
        <label>Type<select name="jobType">${TYPES.map(t => `<option value="${t}" ${j.jobType === t ? 'selected' : ''}>${nice(t)}</option>`).join('')}</select></label></div>
      <div class="two"><label>Minimum salary per year (₹)<input type="number" min="0" name="salaryMin" value="${j.salaryMin ?? ''}"></label>
        <label>Maximum salary per year (₹)<input type="number" min="0" name="salaryMax" value="${j.salaryMax ?? ''}"></label></div>
      <label>Experience required (years)<input type="number" min="0" name="experienceRequired" value="${j.experienceRequired ?? 0}"></label>
      <label>Skills (comma separated)<input name="skills" value="${esc((j.skills || []).join(', '))}" placeholder="Java, Spring Boot"></label>
      <label>Description<textarea name="description" required>${esc(j.description)}</textarea></label>
      <button class="btn">${id ? 'Save changes' : 'Publish job'}</button>
    </form>`;
  f.onsubmit = e => { e.preventDefault(); run(async () => {
    const d = Object.fromEntries(new FormData(f));
    d.skills = csv(d.skills);
    ['salaryMin', 'salaryMax', 'experienceRequired'].forEach(k => d[k] = d[k] === '' ? null : Number(d[k]));
    await api(id ? '/jobs/' + id : '/jobs', { method: id ? 'PUT' : 'POST', body: d });
    toast(id ? 'Job updated' : 'Job published'); location.hash = '#/my-jobs';
  }); };
}

async function pageMyJobs() {
  if (!needRole('RECRUITER')) return;
  const res = await api('/recruiter/jobs');
  app.innerHTML = `<h2>My jobs</h2><br>` + (res.content.length ? `<div class="tablewrap"><table>
    <tr><th>Title</th><th>Status</th><th>Posted</th><th></th></tr>` + res.content.map(j => `
    <tr><td><b>${esc(j.title)}</b><div class="meta">${esc(j.location)} · ${nice(j.jobType)}</div></td>
      <td><span class="pill ${j.status}">${j.status}</span></td><td>${new Date(j.postedAt).toLocaleDateString()}</td>
      <td style="white-space:nowrap">
        <a class="btn sm" href="#/applicants/${j.id}">Applicants</a>
        <a class="btn ghost sm" href="#/post-job/${j.id}">Edit</a>
        <button class="btn ghost sm" onclick="toggleJob(${j.id},'${j.status === 'OPEN' ? 'CLOSED' : 'OPEN'}')">${j.status === 'OPEN' ? 'Close' : 'Reopen'}</button>
        <button class="btn danger sm" onclick="deleteJob(${j.id})">Delete</button></td></tr>`).join('') + '</table></div>'
    : '<p class="empty">No jobs yet. <a href="#/post-job">Post your first job</a></p>');
}
const toggleJob = (id, st) => run(async () => { await api(`/jobs/${id}/status?status=${st}`, { method: 'PATCH' }); pageMyJobs(); });
const deleteJob = id => { if (confirm('Delete this job and all its applications?')) run(async () => { await api('/jobs/' + id, { method: 'DELETE' }); pageMyJobs(); }); };

async function pageApplicants(jobId) {
  if (!needRole('RECRUITER')) return;
  const [job, res] = await Promise.all([api('/jobs/' + jobId), api(`/recruiter/jobs/${jobId}/applications`)]);
  app.innerHTML = `<p><a href="#/my-jobs">← My jobs</a></p><h2>Applicants for ${esc(job.title)}</h2><br>` +
    (res.content.length ? res.content.map(a => `
    <article class="card">
      <div class="row" style="justify-content:space-between;margin:0">
        <div><h3>${esc(a.candidateName)}</h3><div class="meta"><span>${esc(a.candidateEmail)}</span>
          <span>${a.experienceYears ?? 0} yrs exp</span>
          ${a.resumeUrl ? `<a href="${esc(a.resumeUrl)}" target="_blank" rel="noopener">Resume</a>` : ''}</div>
          ${a.headline ? `<div class="meta">${esc(a.headline)}</div>` : ''}</div>
        <div style="text-align:right"><span class="pill ${a.status}">${nice(a.status)}</span>
          <div class="match">${a.skillMatchPercent}% skill match</div></div></div>
      <div class="chips">${chips(a.candidateSkills, job.skills.map(s => s.toLowerCase()))}</div>
      ${a.coverLetter ? `<p class="desc">${esc(a.coverLetter)}</p>` : ''}
      <div class="row"><select id="st${a.id}">${STATUSES.map(s => `<option value="${s}" ${a.status === s ? 'selected' : ''}>${nice(s)}</option>`).join('')}</select>
        <input id="nt${a.id}" placeholder="Note for the candidate (optional)" style="flex:1;min-width:180px">
        <button class="btn sm" onclick="setStatus(${a.id},${jobId})">Update &amp; notify</button></div>
      ${timeline(a.timeline)}
    </article>`).join('') : '<p class="empty">No applications yet.</p>');
}
const setStatus = (id, jobId) => run(async () => {
  await api(`/applications/${id}/status`, { method: 'PATCH', body: { status: document.getElementById('st' + id).value, note: document.getElementById('nt' + id).value || null } });
  toast('Status updated and candidate notified'); pageApplicants(jobId);
});

/* ---------- admin ---------- */
async function pageAdmin() {
  if (!needRole('ADMIN')) return;
  const [s, users, companies] = await Promise.all([api('/admin/stats'), api('/admin/users'), api('/admin/companies')]);
  const stat = (n, l) => `<div class="stat"><b>${n}</b><span>${l}</span></div>`;
  app.innerHTML = `<h2>Admin dashboard</h2>
    <div class="stats">${stat(s.totalUsers, 'Users')}${stat(s.candidates, 'Candidates')}${stat(s.recruiters, 'Recruiters')}
      ${stat(s.totalJobs, 'Jobs')}${stat(s.openJobs, 'Open jobs')}${stat(s.totalApplications, 'Applications')}${stat(s.hired, 'Hired')}</div>
    <h3>Users</h3><br><div class="tablewrap"><table><tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th></th></tr>` +
    users.content.map(u => `<tr><td>${esc(u.fullName)}</td><td>${esc(u.email)}</td><td>${nice(u.role)}</td>
      <td><span class="pill ${u.enabled ? 'OPEN' : 'CLOSED'}">${u.enabled ? 'Active' : 'Blocked'}</span></td>
      <td>${u.role === 'ADMIN' ? '' : `<button class="btn ghost sm" onclick="blockUser(${u.id},${!u.enabled})">${u.enabled ? 'Block' : 'Unblock'}</button>`}</td></tr>`).join('') +
    `</table></div><br><h3>Companies</h3><br><div class="tablewrap"><table><tr><th>Company</th><th>Owner</th><th>Status</th><th></th></tr>` +
    (companies.content.map(c => `<tr><td>${esc(c.name)}</td><td>${esc(c.ownerEmail)}</td>
      <td><span class="pill ${c.approved ? 'OPEN' : 'CLOSED'}">${c.approved ? 'Approved' : 'Hidden'}</span></td>
      <td><button class="btn ghost sm" onclick="approve(${c.id},${!c.approved})">${c.approved ? 'Hide jobs' : 'Approve'}</button></td></tr>`).join('') || '<tr><td colspan="4">No companies yet</td></tr>') +
    `</table></div><p class="meta" style="margin-top:1rem">To remove a job, open it from the Jobs page while logged in as admin.</p>`;
}
const blockUser = (id, enabled) => run(async () => { await api(`/admin/users/${id}/status?enabled=${enabled}`, { method: 'PATCH' }); pageAdmin(); });
const approve = (id, approved) => run(async () => { await api(`/admin/companies/${id}/approval?approved=${approved}`, { method: 'PATCH' }); pageAdmin(); });

route();
