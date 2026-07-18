import { apiRequest } from "/js/api.js";

import {
    clearAuthSession,
    isAuthenticated
} from "/js/auth.js";

const DEFAULT_AVATAR_URL =
    "/images/default-avatar.svg";

const loadingElement =
    document.getElementById("profile-loading");

const errorElement =
    document.getElementById("profile-error");

const contentElement =
    document.getElementById("profile-content");

const avatarElement =
    document.getElementById("profile-avatar");

initializeProfilePage();

async function initializeProfilePage() {
    if (!isAuthenticated()) {
        redirectToLogin();
        return;
    }

    try {
        const profile = await apiRequest(
            "/users/me",
            {
                method: "GET"
            }
        );

        renderProfile(profile);
    } catch (error) {
        handleProfileError(error);
    }
}

function renderProfile(profile) {
    setText(
        "profile-full-name",
        profile.fullName
    );

    setText(
        "profile-email",
        profile.email
    );

    setText(
        "profile-first-name",
        profile.firstName
    );

    setText(
        "profile-last-name",
        profile.lastName
    );

    setText(
        "profile-date-of-birth",
        formatDate(profile.dateOfBirth)
    );

    setText(
        "profile-gender",
        formatGender(profile.gender)
    );

    setText(
        "profile-address",
        profile.address
    );

    setText(
        "profile-phone",
        profile.phone
    );

    setText(
        "profile-role",
        formatRole(profile.role)
    );

    setText(
        "profile-status",
        formatStatus(profile.status)
    );

    setText(
        "profile-created-at",
        formatDateTime(profile.createdAt)
    );

    renderAvatar(profile.avatarUrl);

    loadingElement.hidden = true;
    errorElement.hidden = true;
    contentElement.hidden = false;
}

function renderAvatar(avatarUrl) {
    avatarElement.onerror = () => {
        avatarElement.onerror = null;
        avatarElement.src = DEFAULT_AVATAR_URL;
    };

    avatarElement.src =
        normalizeAvatarUrl(avatarUrl);
}

function normalizeAvatarUrl(avatarUrl) {
    if (
        typeof avatarUrl !== "string"
        || !avatarUrl.trim()
    ) {
        return DEFAULT_AVATAR_URL;
    }

    const trimmedUrl = avatarUrl.trim();

    if (
        trimmedUrl.startsWith("http://")
        || trimmedUrl.startsWith("https://")
    ) {
        return trimmedUrl;
    }

    return trimmedUrl.startsWith("/")
        ? trimmedUrl
        : `/${trimmedUrl}`;
}

function handleProfileError(error) {
    loadingElement.hidden = true;
    contentElement.hidden = true;
    errorElement.hidden = false;

    if (error.status === 401) {
        clearAuthSession();

        errorElement.textContent =
            error.data?.message
            || "Phiên đăng nhập không hợp lệ hoặc đã hết hạn.";

        redirectToLoginAfterDelay();
        return;
    }

    if (error.status === 403) {
        clearAuthSession();

        errorElement.textContent =
            error.data?.message
            || "Bạn không có quyền xem thông tin này.";

        redirectToLoginAfterDelay();
        return;
    }

    if (error.status === 500) {
        errorElement.textContent =
            error.data?.message
            || "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.";

        return;
    }

    if (!error.status) {
        errorElement.textContent =
            "Không thể kết nối đến máy chủ. Hãy kiểm tra Backend đang chạy.";

        return;
    }

    errorElement.textContent =
        "Không thể tải thông tin cá nhân.";
}

function setText(elementId, value) {
    const element =
        document.getElementById(elementId);

    if (!element) {
        return;
    }

    element.textContent =
        value === null
        || value === undefined
        || value === ""
            ? "—"
            : value;
}

function formatDate(value) {
    if (!value) {
        return "—";
    }

    const parts = value.split("-");

    if (parts.length !== 3) {
        return value;
    }

    const [year, month, day] = parts;

    return `${day}/${month}/${year}`;
}

function formatDateTime(value) {
    if (!value) {
        return "—";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return new Intl.DateTimeFormat(
        "vi-VN",
        {
            dateStyle: "medium",
            timeStyle: "short"
        }
    ).format(date);
}

function formatGender(gender) {
    const genderLabels = {
        MALE: "Nam",
        FEMALE: "Nữ",
        OTHER: "Khác"
    };

    return genderLabels[gender] || "—";
}

function formatRole(role) {
    const roleLabels = {
        USER: "Người dùng",
        ADMIN: "Quản trị viên"
    };

    return roleLabels[role] || "—";
}

function formatStatus(status) {
    const statusLabels = {
        ACTIVE: "Đang hoạt động",
        LOCKED: "Đã khóa",
        DELETED: "Đã xóa"
    };

    return statusLabels[status] || "—";
}

function redirectToLogin() {
    window.location.replace(
        "/pages/login.html"
    );
}

function redirectToLoginAfterDelay() {
    window.setTimeout(
        redirectToLogin,
        1500
    );
}