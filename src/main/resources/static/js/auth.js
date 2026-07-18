const ACCESS_TOKEN_KEY = "accessToken";
const TOKEN_TYPE_KEY = "tokenType";
const TOKEN_EXPIRES_AT_KEY = "tokenExpiresAt";
const CURRENT_USER_KEY = "currentUser";

export function saveAuthSession(loginResponse) {
    const {
        accessToken,
        tokenType = "Bearer",
        expiresIn,
        user
    } = loginResponse;

    if (!accessToken) {
        throw new Error(
            "Response đăng nhập không chứa accessToken"
        );
    }

    const expiresInSeconds = Number(expiresIn);

    const expiresAt =
        Number.isFinite(expiresInSeconds)
        && expiresInSeconds > 0
            ? Date.now() + expiresInSeconds * 1000
            : 0;

    localStorage.setItem(
        ACCESS_TOKEN_KEY,
        accessToken
    );

    localStorage.setItem(
        TOKEN_TYPE_KEY,
        tokenType
    );

    localStorage.setItem(
        TOKEN_EXPIRES_AT_KEY,
        String(expiresAt)
    );

    if (user) {
        localStorage.setItem(
            CURRENT_USER_KEY,
            JSON.stringify(user)
        );
    }
}

export function getAccessToken() {
    const accessToken =
        localStorage.getItem(ACCESS_TOKEN_KEY);

    if (!accessToken) {
        return null;
    }

    const expiresAt = Number(
        localStorage.getItem(
            TOKEN_EXPIRES_AT_KEY
        )
    );

    if (
        Number.isFinite(expiresAt)
        && expiresAt > 0
        && Date.now() >= expiresAt
    ) {
        clearAuthSession();
        return null;
    }

    return accessToken;
}

export function getCurrentUser() {
    const storedUser =
        localStorage.getItem(CURRENT_USER_KEY);

    if (!storedUser) {
        return null;
    }

    try {
        return JSON.parse(storedUser);
    } catch (error) {
        localStorage.removeItem(CURRENT_USER_KEY);
        return null;
    }
}

export function isAuthenticated() {
    return Boolean(getAccessToken());
}

export function clearAuthSession() {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(TOKEN_TYPE_KEY);
    localStorage.removeItem(
        TOKEN_EXPIRES_AT_KEY
    );
    localStorage.removeItem(CURRENT_USER_KEY);
}

export function logout() {
    clearAuthSession();

    window.location.replace(
        "/pages/login.html"
    );
}