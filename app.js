const loginCard = document.querySelector('.login-card');
const views = {
  login: document.getElementById('loginView'),
  register: document.getElementById('registerView'),
  forgot: document.getElementById('forgotView'),
  welcome: document.getElementById('welcomeView'),
};

const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const forgotForm = document.getElementById('forgotForm');
const rememberMeInput = document.getElementById('rememberMe');
const welcomeMessage = document.getElementById('welcomeMessage');
const logoutButton = document.getElementById('logoutButton');

const openButtons = document.querySelectorAll('[data-open]');
const togglePasswordButtons = document.querySelectorAll('.toggle-password');

const loadUsers = () => JSON.parse(localStorage.getItem('novacart.users') || '{}');
const saveUsers = (users) => localStorage.setItem('novacart.users', JSON.stringify(users));

const setStatus = (id, msg, type) => {
  const el = document.getElementById(id);
  el.textContent = msg;
  el.className = `status ${type}`;
};

const setError = (id, msg = '') => {
  document.getElementById(id).textContent = msg;
};

const clearStatuses = () => ['loginStatus', 'registerStatus', 'forgotStatus'].forEach((id) => setStatus(id, '', ''));

const showView = (name) => {
  Object.values(views).forEach((view) => view.classList.add('hidden'));
  views[name].classList.remove('hidden');
};

const runCardAnimation = () => {
  loginCard.classList.remove('is-submitting');
  void loginCard.offsetWidth;
  loginCard.classList.add('is-submitting');
  setTimeout(() => loginCard.classList.remove('is-submitting'), 500);
};

togglePasswordButtons.forEach((button) => {
  button.addEventListener('click', () => {
    const target = document.getElementById(button.dataset.target);
    const isHidden = target.type === 'password';
    target.type = isHidden ? 'text' : 'password';
    button.textContent = isHidden ? 'Hide' : 'Show';
  });
});

openButtons.forEach((button) => {
  button.addEventListener('click', () => {
    clearStatuses();
    showView(button.dataset.open);
  });
});

loginForm.addEventListener('submit', (event) => {
  event.preventDefault();
  clearStatuses();
  setError('loginUsernameError');
  setError('loginPasswordError');

  const username = document.getElementById('loginUsername').value.trim();
  const password = document.getElementById('loginPassword').value;

  if (username.length < 3) {
    setError('loginUsernameError', 'Username must be at least 3 characters long.');
    setStatus('loginStatus', 'Please fix highlighted fields.', 'error');
    runCardAnimation();
    return;
  }

  const users = loadUsers();
  if (!users[username] || users[username].password !== password) {
    setError('loginPasswordError', 'Invalid username or password.');
    setStatus('loginStatus', 'Login failed. Try again or reset password.', 'error');
    runCardAnimation();
    return;
  }

  const session = { username, rememberMe: rememberMeInput.checked, loginAt: new Date().toISOString() };
  localStorage.setItem('novacart.session', JSON.stringify(session));
  welcomeMessage.textContent = `Hi ${username}, your account is ready. Continue shopping now.`;
  showView('welcome');
  runCardAnimation();
});

registerForm.addEventListener('submit', (event) => {
  event.preventDefault();
  clearStatuses();
  setError('registerUsernameError');
  setError('registerPasswordError');

  const username = document.getElementById('registerUsername').value.trim();
  const password = document.getElementById('registerPassword').value;

  if (username.length < 3) {
    setError('registerUsernameError', 'Use at least 3 characters.');
    setStatus('registerStatus', 'Username is too short.', 'error');
    runCardAnimation();
    return;
  }
  if (password.length < 8) {
    setError('registerPasswordError', 'Password must be at least 8 characters long.');
    setStatus('registerStatus', 'Password is too short.', 'error');
    runCardAnimation();
    return;
  }

  const users = loadUsers();
  if (users[username]) {
    setError('registerUsernameError', 'This username already exists.');
    setStatus('registerStatus', 'Choose another username.', 'error');
    runCardAnimation();
    return;
  }

  users[username] = { password, createdAt: new Date().toISOString() };
  saveUsers(users);
  localStorage.setItem('novacart.session', JSON.stringify({ username, rememberMe: true, loginAt: new Date().toISOString() }));
  welcomeMessage.textContent = `Account created for ${username}. Welcome to NovaCart!`;
  showView('welcome');
  runCardAnimation();
});

forgotForm.addEventListener('submit', (event) => {
  event.preventDefault();
  clearStatuses();
  setError('forgotUsernameError');
  setError('forgotPasswordError');

  const username = document.getElementById('forgotUsername').value.trim();
  const newPassword = document.getElementById('forgotPassword').value;

  if (!username) {
    setError('forgotUsernameError', 'Username is required.');
    setStatus('forgotStatus', 'Please enter username.', 'error');
    runCardAnimation();
    return;
  }
  if (newPassword.length < 8) {
    setError('forgotPasswordError', 'Password must be at least 8 characters long.');
    setStatus('forgotStatus', 'Please enter a stronger password.', 'error');
    runCardAnimation();
    return;
  }

  const users = loadUsers();
  if (!users[username]) {
    setError('forgotUsernameError', 'No account found for this username.');
    setStatus('forgotStatus', 'Register first if you are a new user.', 'error');
    runCardAnimation();
    return;
  }

  users[username].password = newPassword;
  users[username].updatedAt = new Date().toISOString();
  saveUsers(users);
  setStatus('forgotStatus', 'Password updated. You can now sign in.', 'success');
  runCardAnimation();
  setTimeout(() => showView('login'), 700);
});

logoutButton.addEventListener('click', () => {
  localStorage.removeItem('novacart.session');
  showView('login');
});

window.addEventListener('DOMContentLoaded', () => {
  const session = JSON.parse(localStorage.getItem('novacart.session') || 'null');
  if (session?.rememberMe && session?.username) {
    welcomeMessage.textContent = `Welcome back, ${session.username}. Continue shopping.`;
    showView('welcome');
    return;
  }
  showView('login');
});
