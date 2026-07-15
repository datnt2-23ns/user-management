function createHeader() {
    return `
        <header class="app-header">
            <nav class="navbar">
                <a class="navbar__brand" href="/">
                    User Management
                </a>

                <div class="navbar__links">
                    <a href="/pages/login.html">Đăng nhập</a>
                    <a href="/pages/register.html">Đăng ký</a>
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