function createHeader() {
  return `
        <header class="app-header">
            <nav class="navbar">
                <a class="navbar__brand" href="/">
                    User Management
                </a>

                <!-- Hiển thị khi chưa đăng nhập -->
                <div
                    class="navbar__links"
                    data-auth-guest
                >
                    <a href="/pages/login.html">
                        Đăng nhập
                    </a>

                    <a href="/pages/register.html">
                        Đăng ký
                    </a>
                </div>

                <!-- Hiển thị khi đã đăng nhập -->
                <div
                    class="navbar__user"
                    data-auth-user
                    hidden
                >

                    <a class="navbar__profile-link" href="/pages/profile.html">Hồ sơ</a>

                    <span class="navbar__welcome">
                        Xin chào,
                        <strong data-auth-name>
                            Người dùng
                        </strong>
                    </span>

                    <button
                        class="navbar__logout"
                        type="button"
                        data-logout-button
                    >
                        Đăng xuất
                    </button>
                </div>
            </nav>
        </header>
    `;
}

function createFooter() {
  const currentYear = new Date().getFullYear();

  return `
        <footer class="app-footer">
            User Management System © ${currentYear}
        </footer>
    `;
}

document.addEventListener("DOMContentLoaded", () => {
  const headerElement = document.querySelector("[data-app-header]");

  const footerElement = document.querySelector("[data-app-footer]");

  if (headerElement) {
    headerElement.innerHTML = createHeader();
  }

  if (footerElement) {
    footerElement.innerHTML = createFooter();
  }
});
