const loginForm = document.getElementById('loginForm');
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const rememberMeInput = document.getElementById('rememberMe');
const togglePasswordButton = document.getElementById('togglePassword');
const statusMessage = document.getElementById('statusMessage');
const usernameError = document.getElementById('usernameError');
const passwordError = document.getElementById('passwordError');
const loginCard = document.querySelector('.login-card');
const loginButton = document.getElementById('loginButton');

const setStatus = (message, type) => {
  statusMessage.textContent = message;
  statusMessage.className = `status ${type}`;
};

const clearErrors = () => {
  usernameError.textContent = '';
  passwordError.textContent = '';
};

const triggerSubmitAnimation = () => {
  loginCard.classList.remove('is-submitting');
  void loginCard.offsetWidth;
  loginCard.classList.add('is-submitting');
  setTimeout(() => loginCard.classList.remove('is-submitting'), 500);
};

const validate = () => {
  clearErrors();
  let isValid = true;

  if (usernameInput.value.trim().length < 3) {
    usernameError.textContent = 'Username must be at least 3 characters long.';
    isValid = false;
  }

  if (passwordInput.value.length < 8) {
    passwordError.textContent = 'Password must be at least 8 characters long.';
    isValid = false;
  }

  return isValid;
};

togglePasswordButton.addEventListener('click', () => {
  const showPassword = passwordInput.type === 'password';
  passwordInput.type = showPassword ? 'text' : 'password';
  togglePasswordButton.textContent = showPassword ? 'Hide' : 'Show';
  togglePasswordButton.setAttribute('aria-label', showPassword ? 'Hide password' : 'Show password');
});

loginButton.addEventListener('pointerdown', (event) => {
  const rect = loginButton.getBoundingClientRect();
  loginButton.style.setProperty('--rx', `${event.clientX - rect.left}px`);
  loginButton.style.setProperty('--ry', `${event.clientY - rect.top}px`);
  loginButton.classList.remove('ripple');
  void loginButton.offsetWidth;
  loginButton.classList.add('ripple');
  setTimeout(() => loginButton.classList.remove('ripple'), 330);
});

loginCard.addEventListener('pointermove', (event) => {
  const rect = loginCard.getBoundingClientRect();
  const px = (event.clientX - rect.left) / rect.width;
  const py = (event.clientY - rect.top) / rect.height;

  const rotateY = (px - 0.5) * 8;
  const rotateX = (0.5 - py) * 8;

  loginCard.style.transform = `translateY(0) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`;
  loginCard.style.boxShadow = `${-rotateY * 1.5}px ${rotateX * 1.5 + 16}px 30px rgba(15, 23, 51, 0.55)`;
  loginCard.style.setProperty('--mx', `${px * 100}%`);
  loginCard.style.setProperty('--my', `${py * 100}%`);
});

loginCard.addEventListener('pointerleave', () => {
  loginCard.style.transform = 'translateY(0) rotateX(0deg) rotateY(0deg)';
  loginCard.style.boxShadow = '0 10px 25px rgba(12, 21, 44, 0.3)';
  loginCard.style.setProperty('--mx', '50%');
  loginCard.style.setProperty('--my', '50%');
});

loginForm.addEventListener('submit', (event) => {
  event.preventDefault();

  if (!validate()) {
    setStatus('Please correct the highlighted fields.', 'error');
    triggerSubmitAnimation();
    return;
  }

  const userSession = {
    username: usernameInput.value.trim(),
    rememberMe: rememberMeInput.checked,
    loginAt: new Date().toISOString(),
  };

  localStorage.setItem('novacart.session', JSON.stringify(userSession));
  setStatus(`Login successful! Welcome back, ${userSession.username}.`, 'success');
  triggerSubmitAnimation();
  loginForm.reset();
  passwordInput.type = 'password';
  togglePasswordButton.textContent = 'Show';
});

window.addEventListener('DOMContentLoaded', () => {
  const previousSession = localStorage.getItem('novacart.session');

  if (!previousSession) {
    return;
  }

  const { username, rememberMe } = JSON.parse(previousSession);

  if (rememberMe && username) {
    usernameInput.value = username;
    rememberMeInput.checked = true;
    setStatus(`Welcome back, ${username}. Continue where you left off.`, 'success');
  }
});
