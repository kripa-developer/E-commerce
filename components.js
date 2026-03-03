(function () {
  const headerTemplate = (activePage = '') => `
    <header class="site-header">
      <div class="site-header-inner">
        <a href="index.html" class="brand-link">NovaCart</a>
        <nav class="site-nav" aria-label="Primary">
          <a href="index.html" class="nav-link ${activePage === 'home' ? 'active' : ''}">Home</a>
          <a href="products.html" class="nav-link ${activePage === 'products' ? 'active' : ''}">Products</a>
          <a href="login.html" class="nav-link ${activePage === 'login' ? 'active' : ''}">Login</a>
          <a href="create-account.html" class="nav-link ${activePage === 'create' ? 'active' : ''}">Create Account</a>
        </nav>
      </div>
    </header>
  `;

  const footerTemplate = `
    <footer class="site-footer">
      <p>© 2026 NovaCart. Built for modern shopping experiences.</p>
    </footer>
  `;

  window.NovaComponents = {
    mountHeader: (activePage) => {
      const root = document.getElementById('siteHeader');
      if (!root) return;
      root.innerHTML = headerTemplate(activePage);
    },
    mountFooter: () => {
      const root = document.getElementById('siteFooter');
      if (!root) return;
      root.innerHTML = footerTemplate;
    },
  };
})();
