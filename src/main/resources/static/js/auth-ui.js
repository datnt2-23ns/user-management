import {
    getCurrentUser,
    isAuthenticated,
    logout
} from "/js/auth.js";

function renderAuthUi() {
    const guestSection =
        document.querySelector("[data-auth-guest]");

    const userSection =
        document.querySelector("[data-auth-user]");

    if (!guestSection || !userSection) {
        return;
    }

    const authenticated = isAuthenticated();

    guestSection.hidden = authenticated;
    userSection.hidden = !authenticated;

    if (!authenticated) {
        return;
    }

    const currentUser = getCurrentUser();

    const userNameElement =
        document.querySelector("[data-auth-name]");

    if (userNameElement) {
        userNameElement.textContent =
            currentUser?.fullName
            || currentUser?.email
            || "Người dùng";
    }
}

document.addEventListener(
    "DOMContentLoaded",
    renderAuthUi
);

document.addEventListener("click", (event) => {
    const logoutButton =
        event.target.closest("[data-logout-button]");

    if (!logoutButton) {
        return;
    }

    logout();
});