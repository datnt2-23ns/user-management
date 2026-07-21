import { getCurrentUser, isAuthenticated, logout } from "/js/auth.js";

function renderAuthUi() {
  const guestSection = document.querySelector("[data-auth-guest]");

  const userSection = document.querySelector("[data-auth-user]");

  const adminOnlyElements = document.querySelectorAll("[data-admin-only]");

  if (!guestSection || !userSection) {
    return;
  }

  const authenticated = isAuthenticated();

  guestSection.hidden = authenticated;

  userSection.hidden = !authenticated;

  if (!authenticated) {
    adminOnlyElements.forEach((element) => {
      element.hidden = true;
    });

    return;
  }

  const currentUser = getCurrentUser();

  const userNameElement = document.querySelector("[data-auth-name]");

  if (userNameElement) {
    userNameElement.textContent =
      currentUser?.fullName || currentUser?.email || "Người dùng";
  }

  const normalizedRole = String(currentUser?.role || "")
    .replace("ROLE_", "")
    .toUpperCase();

  const isAdmin = normalizedRole === "ADMIN";

  adminOnlyElements.forEach((element) => {
    element.hidden = !isAdmin;
  });
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", renderAuthUi, {
    once: true,
  });
} else {
  renderAuthUi();
}

document.addEventListener("click", (event) => {
  const logoutButton = event.target.closest("[data-logout-button]");

  if (!logoutButton) {
    return;
  }

  logout();
});
