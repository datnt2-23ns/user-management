import { apiRequest } from "/js/api.js";

import { clearAuthSession, isAuthenticated } from "/js/auth.js";

const REQUEST_TIMEOUT_MS = 5000;

const DEFAULT_AVATAR_URL = "/images/default-avatar.svg";

const loadingElement = document.getElementById("detail-loading");

const contentElement = document.getElementById("detail-content");

const messageElement = document.getElementById("detail-message");

const avatarElement = document.getElementById("user-avatar");

const fullNameElement = document.getElementById("user-full-name");

const emailSummaryElement = document.getElementById("user-email");

const roleBadgeElement = document.getElementById("user-role");

const statusBadgeElement = document.getElementById("user-status");

initializePage().catch(handleInitializationError);

async function initializePage() {
  if (!isAuthenticated()) {
    redirectToLogin();
    return;
  }

  validateRequiredElements();

  const userId = getUserIdFromUrl();

  if (!userId) {
    showLoading(false);

    showMessage("ID tài khoản không hợp lệ.", "error");

    return;
  }

  await loadUserDetail(userId);
}

function validateRequiredElements() {
  const requiredElements = {
    loadingElement,
    contentElement,
    messageElement,
    avatarElement,
    fullNameElement,
    emailSummaryElement,
    roleBadgeElement,
    statusBadgeElement,
  };

  for (const [name, element] of Object.entries(requiredElements)) {
    if (!element) {
      throw new Error(`Không tìm thấy phần tử giao diện: ${name}`);
    }
  }
}

function getUserIdFromUrl() {
  const params = new URLSearchParams(window.location.search);

  const rawUserId = params.get("id");

  if (!rawUserId || !/^\d+$/.test(rawUserId)) {
    return null;
  }

  const userId = Number(rawUserId);

  if (!Number.isSafeInteger(userId) || userId < 1) {
    return null;
  }

  return userId;
}

async function loadUserDetail(userId) {
  hideMessage();
  showLoading(true);

  try {
    const user = await apiRequest(`/admin/users/${userId}`, {
      method: "GET",
      timeoutMs: REQUEST_TIMEOUT_MS,
    });

    renderUserDetail(user);
  } catch (error) {
    handleApiError(error);
  } finally {
    showLoading(false);
  }
}

function renderUserDetail(user) {
  const fullName = user.fullName || buildFullName(user) || "Người dùng";

  avatarElement.src = normalizeAvatarUrl(user.avatarUrl);

  avatarElement.alt = `Ảnh đại diện của ${fullName}`;

  avatarElement.addEventListener("error", handleAvatarError, {
    once: true,
  });

  fullNameElement.textContent = fullName;

  emailSummaryElement.textContent = displayValue(user.email);

  renderRoleBadge(user.role);
  renderStatusBadge(user.status);

  setText("detail-id", user.id);

  setText("detail-first-name", user.firstName);

  setText("detail-last-name", user.lastName);

  setText("detail-email", user.email);

  setText("detail-phone", user.phone);

  setText("detail-date-of-birth", formatDate(user.dateOfBirth));

  setText("detail-gender", formatGender(user.gender));

  setText("detail-address", user.address);

  setText("detail-role", formatRole(user.role));

  setText("detail-status", formatStatus(user.status));

  setText("detail-created-at", formatDateTime(user.createdAt));

  setText("detail-updated-at", formatDateTime(user.updatedAt));

  setText("detail-updated-by", user.updatedBy);

  setText("detail-locked-by", user.lockedBy);

  setText("detail-locked-at", formatDateTime(user.lockedAt));

  contentElement.hidden = false;
}

function setText(elementId, value) {
  const element = document.getElementById(elementId);

  if (!element) {
    return;
  }

  element.textContent = displayValue(value);
}

function displayValue(value) {
  if (value === null || value === undefined || String(value).trim() === "") {
    return "Chưa cập nhật";
  }

  return String(value);
}

function buildFullName(user) {
  return `${user.lastName ?? ""} ${user.firstName ?? ""}`
    .trim()
    .replace(/\s+/g, " ");
}

function renderRoleBadge(role) {
  roleBadgeElement.textContent = formatRole(role);

  roleBadgeElement.className =
    role === "ADMIN" ? "badge role-admin" : "badge role-user";
}

function renderStatusBadge(status) {
  statusBadgeElement.textContent = formatStatus(status);

  statusBadgeElement.className =
    status === "LOCKED" ? "badge status-locked" : "badge status-active";
}

function formatRole(role) {
  if (role === "ADMIN") {
    return "Admin";
  }

  if (role === "USER") {
    return "User";
  }

  return role || "Chưa cập nhật";
}

function formatStatus(status) {
  if (status === "ACTIVE") {
    return "Đang hoạt động";
  }

  if (status === "LOCKED") {
    return "Đã khóa";
  }

  return status || "Chưa cập nhật";
}

function formatGender(gender) {
  if (gender === "MALE") {
    return "Nam";
  }

  if (gender === "FEMALE") {
    return "Nữ";
  }

  if (gender === "OTHER") {
    return "Khác";
  }

  return gender || "Chưa cập nhật";
}

function formatDate(value) {
  if (!value) {
    return "Chưa cập nhật";
  }

  const parts = String(value).split("-");

  if (parts.length !== 3) {
    return "Chưa cập nhật";
  }

  const [year, month, day] = parts;

  return `${day}/${month}/${year}`;
}

function formatDateTime(value) {
  if (!value) {
    return "Chưa cập nhật";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "Chưa cập nhật";
  }

  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(date);
}

function normalizeAvatarUrl(url) {
  if (typeof url !== "string" || !url.trim()) {
    return DEFAULT_AVATAR_URL;
  }

  const normalized = url.trim();

  if (
    normalized.startsWith("/") ||
    normalized.startsWith("http://") ||
    normalized.startsWith("https://")
  ) {
    return normalized;
  }

  return `/${normalized}`;
}

function handleAvatarError() {
  avatarElement.src = DEFAULT_AVATAR_URL;
}

function showLoading(loading) {
  loadingElement.hidden = !loading;

  if (loading) {
    contentElement.hidden = true;
  }
}

function handleApiError(error) {
  console.error("Admin user detail error:", error);

  contentElement.hidden = true;

  if (error.code === "TIMEOUT") {
    showMessage("Máy chủ phản hồi quá lâu. Vui lòng thử lại.", "error");

    return;
  }

  if (error.code === "NETWORK_ERROR") {
    showMessage("Không thể kết nối đến máy chủ.", "error");

    return;
  }

  if (error.status === 400) {
    showMessage(error.data?.message || "ID tài khoản không hợp lệ.", "error");

    return;
  }

  if (error.status === 401) {
    clearAuthSession();

    showMessage("Phiên đăng nhập không hợp lệ hoặc đã hết hạn.", "error");

    window.setTimeout(redirectToLogin, 1500);

    return;
  }

  if (error.status === 403) {
    showMessage("Bạn không có quyền xem chi tiết tài khoản.", "error");

    return;
  }

  if (error.status === 404) {
    showMessage(error.data?.message || "Không tìm thấy tài khoản.", "error");

    return;
  }

  if (error.status === 500) {
    showMessage(
      error.data?.message || "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.",
      "error",
    );

    return;
  }

  showMessage(error.message || "Không thể tải thông tin tài khoản.", "error");
}

function showMessage(message, type) {
  messageElement.textContent = message;

  messageElement.classList.remove("error");

  messageElement.classList.add(type);

  messageElement.hidden = false;
}

function hideMessage() {
  messageElement.textContent = "";
  messageElement.hidden = true;

  messageElement.classList.remove("error");
}

function handleInitializationError(error) {
  console.error("Admin user detail initialization error:", error);

  if (loadingElement) {
    loadingElement.hidden = true;
  }

  if (contentElement) {
    contentElement.hidden = true;
  }

  if (messageElement) {
    showMessage(
      error.message || "Không thể khởi tạo trang chi tiết tài khoản.",
      "error",
    );
  }
}

function redirectToLogin() {
  window.location.replace("/pages/login.html");
}
