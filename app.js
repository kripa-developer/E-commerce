const loginForm = document.getElementById('loginForm');
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const confirmPasswordInput = document.getElementById('confirmPassword');
const recoveryEmailInput = document.getElementById('recoveryEmail');
const rememberMeInput = document.getElementById('rememberMe');
const togglePasswordButton = document.getElementById('togglePassword');
const statusMessage = document.getElementById('statusMessage');
const usernameError = document.getElementById('usernameError');
const passwordError = document.getElementById('passwordError');
const confirmPasswordError = document.getElementById('confirmPasswordError');
const recoveryEmailError = document.getElementById('recoveryEmailError');
const loginCard = document.querySelector('.login-card');
const loginButton = document.getElementById('loginButton');
const forgotPasswordLink = document.getElementById('forgotPasswordLink');
const createAccountLink = document.getElementById('createAccountLink');
const signupRow = document.getElementById('signupRow');
const confirmPasswordGroup = document.getElementById('confirmPasswordGroup');
const recoveryGroup = document.getElementById('recoveryGroup');

let authMode = 'signin';

const setStatus = (message, type) => {
  statusMessage.textContent = message;
  statusMessage.className = `status ${type}`;
};

const clearErrors = () => {
  usernameError.textContent = '';
  passwordError.textContent = '';
  confirmPasswordError.textContent = '';
  recoveryEmailError.textContent = '';
};

const triggerSubmitAnimation = () => {
  loginCard.classList.remove('is-submitting');
  void loginCard.offsetWidth;
  loginCard.classList.add('is-submitting');
  setTimeout(() => loginCard.classList.remove('is-submitting'), 500);
};

const resetFormModeState = () => {
  clearErrors();
  loginForm.reset();
  passwordInput.type = 'password';
  confirmPasswordInput.type = 'password';
  togglePasswordButton.textContent = 'Show';
  togglePasswordButton.setAttribute('aria-label', 'Show password');
};

const setMode = (mode) => {
  authMode = mode;
  loginForm.dataset.mode = mode;

  const title = document.getElementById('login-title');
  const subtitle = document.querySelector('.card-subtitle');

  if (mode === 'create') {
    title.textContent = 'Create account';
    subtitle.textContent = 'Set up your NovaCart account in seconds.';
    loginButton.textContent = 'Create account';
    confirmPasswordGroup.classList.remove('hidden');
    recoveryGroup.classList.add('hidden');
    passwordInput.autocomplete = 'new-password';
    passwordInput.placeholder = 'Create a password';
    signupRow.innerHTML = 'Already have an account? <a href="#" class="link-btn" id="backToSignInFromCreate">Sign in</a>';
    document.getElementById('backToSignInFromCreate').addEventListener('click', (event) => {
      event.preventDefault();
      setMode('signin');
    });
  } else if (mode === 'forgot') {
    title.textContent = 'Forgot password';
    subtitle.textContent = 'Enter your details and we will send reset instructions.';
    loginButton.textContent = 'Send reset link';
    confirmPasswordGroup.classList.add('hidden');
    recoveryGroup.classList.remove('hidden');
    passwordInput.autocomplete = 'off';
    passwordInput.placeholder = 'Last remembered password (optional)';
    signupRow.innerHTML = 'Remembered it? <a href="#" class="link-btn" id="backToSignInFromForgot">Sign in</a>';
    document.getElementById('backToSignInFromForgot').addEventListener('click', (event) => {
      event.preventDefault();
      setMode('signin');
    });
  } else {
    title.textContent = 'Sign in';
    subtitle.textContent = 'Access your account to continue shopping.';
    loginButton.textContent = 'Sign in securely';
    confirmPasswordGroup.classList.add('hidden');
    recoveryGroup.classList.add('hidden');
    passwordInput.autocomplete = 'current-password';
    passwordInput.placeholder = 'Enter your password';
    signupRow.innerHTML = 'Don\'t have an account? <a href="#" class="link-btn" id="createAccountLink">Create one</a>';
    document.getElementById('createAccountLink').addEventListener('click', (event) => {
      event.preventDefault();
      setMode('create');
    });
  }

  setStatus('', '');
  resetFormModeState();
};

const validate = () => {
  clearErrors();
  let isValid = true;

  if (usernameInput.value.trim().length < 3) {
    usernameError.textContent = 'Username must be at least 3 characters long.';
    isValid = false;
  }

  if (authMode !== 'forgot' && passwordInput.value.length < 8) {
    passwordError.textContent = 'Password must be at least 8 characters long.';
    isValid = false;
  }

  if (authMode === 'create' && confirmPasswordInput.value !== passwordInput.value) {
    confirmPasswordError.textContent = 'Passwords do not match.';
    isValid = false;
  }

  if (authMode === 'forgot') {
    const email = recoveryEmailInput.value.trim();
    const validEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    if (!validEmail) {
      recoveryEmailError.textContent = 'Enter a valid recovery email address.';
      isValid = false;
    }
  }

  return isValid;
};

togglePasswordButton.addEventListener('click', () => {
  const showPassword = passwordInput.type === 'password';
  passwordInput.type = showPassword ? 'text' : 'password';
  if (!confirmPasswordGroup.classList.contains('hidden')) {
    confirmPasswordInput.type = showPassword ? 'text' : 'password';
  }
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

  const username = usernameInput.value.trim();

  if (authMode === 'create') {
    localStorage.setItem(
      'novacart.account',
      JSON.stringify({ username, createdAt: new Date().toISOString() })
    );
    setStatus(`Account created for ${username}. You can sign in now.`, 'success');
    triggerSubmitAnimation();
    setTimeout(() => setMode('signin'), 700);
    return;
  }

  if (authMode === 'forgot') {
    setStatus(`Reset link sent to ${recoveryEmailInput.value.trim()}. Check your inbox.`, 'success');
    triggerSubmitAnimation();
    setTimeout(() => setMode('signin'), 900);
    return;
  }

  const userSession = {
    username,
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

forgotPasswordLink.addEventListener('click', (event) => {
  event.preventDefault();
  setMode('forgot');
});

createAccountLink.addEventListener('click', (event) => {
  event.preventDefault();
  setMode('create');
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
