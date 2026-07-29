import { apiRequest } from "/js/api.js";

import { clearAuthSession, getCurrentUser, isAuthenticated } from "/js/auth.js";

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

const toggleStatusButton = document.getElementById("toggle-user-status");

const confirmDialog = document.getElementById("status-confirm-dialog");

const confirmDialogTitle = document.getElementById("confirm-dialog-title");

const confirmDialogMessage = document.getElementById("confirm-dialog-message");

const cancelStatusButton = document.getElementById("cancel-status-change");

const confirmStatusButton = document.getElementById("confirm-status-change");

const roleSelect = document.getElementById("role-select");

const changeRoleButton = document.getElementById("change-role-button");

const roleConfirmDialog = document.getElementById("role-confirm-dialog");

const roleDialogTitle = document.getElementById("role-dialog-title");

const roleDialogMessage = document.getElementById("role-dialog-message");

const cancelRoleButton = document.getElementById("cancel-role-change");

const confirmRoleButton = document.getElementById("confirm-role-change");

let pendingRole = null;

let displayedUser = null;

let pendingStatus = null;

initializePage().catch(handleInitializationError);

async function initializePage() {
  if (!isAuthenticated()) {
    redirectToLogin();
    return;
  }

  validateRequiredElements();
  registerStatusEventListeners();
  registerRoleEventListeners();

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
    toggleStatusButton,
    confirmDialog,
    confirmDialogTitle,
    confirmDialogMessage,
    cancelStatusButton,
    confirmStatusButton,
    roleSelect,
    changeRoleButton,
    roleConfirmDialog,
    roleDialogTitle,
    roleDialogMessage,
    cancelRoleButton,
    confirmRoleButton,
  };

  for (const [name, element] of Object.entries(requiredElements)) {
    if (!element) {
      throw new Error(`Không tìm thấy phần tử giao diện: ${name}`);
    }
  }
}

function registerStatusEventListeners() {
  toggleStatusButton.addEventListener("click", openStatusConfirmDialog);

  cancelStatusButton.addEventListener("click", closeStatusConfirmDialog);

  confirmStatusButton.addEventListener("click", confirmStatusChange);

  document.querySelectorAll("[data-dialog-close]").forEach((element) => {
    element.addEventListener("click", closeStatusConfirmDialog);
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && !confirmDialog.hidden) {
      closeStatusConfirmDialog();
    }
  });
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
  displayedUser = user;

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

  setText("detail-updated-by", user.updatedByName || user.updatedBy);

  setText("detail-locked-by", user.lockedByName || user.lockedBy);

  setText("detail-locked-at", formatDateTime(user.lockedAt));

  configureStatusButton(user);

  configureRoleControls(user);

  contentElement.hidden = false;
}

function configureStatusButton(user) {
  const currentUser = getCurrentUser();

  const isCurrentAccount = isSameAccount(currentUser, user);

  if (isCurrentAccount) {
    toggleStatusButton.disabled = true;
    toggleStatusButton.textContent = "Không thể thao tác trên chính mình";

    toggleStatusButton.className = "button secondary";

    toggleStatusButton.title =
      "Quản trị viên không thể tự khóa tài khoản của mình";

    return;
  }

  toggleStatusButton.title = "";

  if (user.status === "ACTIVE") {
    toggleStatusButton.disabled = false;
    toggleStatusButton.textContent = "Khóa tài khoản";

    toggleStatusButton.className = "button danger";

    return;
  }

  if (user.status === "LOCKED") {
    toggleStatusButton.disabled = false;
    toggleStatusButton.textContent = "Mở khóa tài khoản";

    toggleStatusButton.className = "button success";

    return;
  }

  toggleStatusButton.disabled = true;
  toggleStatusButton.textContent = "Không thể thay đổi trạng thái";

  toggleStatusButton.className = "button secondary";
}

function isSameAccount(currentUser, targetUser) {
  const currentUserId = Number(currentUser?.id);

  const targetUserId = Number(targetUser?.id);

  const sameId =
    Number.isSafeInteger(currentUserId) &&
    Number.isSafeInteger(targetUserId) &&
    currentUserId === targetUserId;

  const currentEmail = normalizeEmail(currentUser?.email);

  const targetEmail = normalizeEmail(targetUser?.email);

  const sameEmail = currentEmail !== "" && currentEmail === targetEmail;

  return sameId || sameEmail;
}

function normalizeEmail(email) {
  return typeof email === "string" ? email.trim().toLowerCase() : "";
}

function openStatusConfirmDialog() {
  if (!displayedUser || toggleStatusButton.disabled) {
    return;
  }

  pendingStatus = displayedUser.status === "ACTIVE" ? "LOCKED" : "ACTIVE";

  const fullName =
    displayedUser.fullName ||
    buildFullName(displayedUser) ||
    displayedUser.email ||
    "tài khoản này";

  if (pendingStatus === "LOCKED") {
    confirmDialogTitle.textContent = "Xác nhận khóa tài khoản";

    confirmDialogMessage.textContent =
      `Bạn có chắc chắn muốn khóa tài khoản “${fullName}”? ` +
      "Tài khoản này sẽ không thể tiếp tục sử dụng token cũ.";

    confirmStatusButton.textContent = "Khóa tài khoản";

    confirmStatusButton.className = "button danger";
  } else {
    confirmDialogTitle.textContent = "Xác nhận mở khóa tài khoản";

    confirmDialogMessage.textContent = `Bạn có chắc chắn muốn mở khóa tài khoản “${fullName}”?`;

    confirmStatusButton.textContent = "Mở khóa tài khoản";

    confirmStatusButton.className = "button success";
  }

  confirmDialog.hidden = false;

  document.body.classList.add("dialog-open");

  confirmStatusButton.focus();
}

function closeStatusConfirmDialog() {
  confirmDialog.hidden = true;

  document.body.classList.remove("dialog-open");

  pendingStatus = null;

  toggleStatusButton.focus();
}

async function confirmStatusChange() {
  if (!displayedUser || !pendingStatus) {
    return;
  }

  const requestedStatus = pendingStatus;

  setStatusActionLoading(true);

  try {
    const updatedUser = await apiRequest(
      `/admin/users/${displayedUser.id}/status`,
      {
        method: "PATCH",

        headers: {
          "Content-Type": "application/json",
        },

        body: JSON.stringify({
          status: requestedStatus,
        }),

        timeoutMs: REQUEST_TIMEOUT_MS,
      },
    );

    displayedUser = {
      ...displayedUser,
      ...updatedUser,
    };

    closeStatusConfirmDialog();

    renderUserDetail(displayedUser);

    showMessage(
      requestedStatus === "LOCKED"
        ? "Khóa tài khoản thành công."
        : "Mở khóa tài khoản thành công.",
      "success",
    );
  } catch (error) {
    closeStatusConfirmDialog();
    handleApiError(error);
  } finally {
    setStatusActionLoading(false);
  }
}

function setStatusActionLoading(loading) {
  confirmStatusButton.disabled = loading;

  cancelStatusButton.disabled = loading;

  toggleStatusButton.disabled = loading;

  if (loading) {
    confirmStatusButton.textContent = "Đang xử lý...";
  }
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
    showMessage(
      error.data?.message || "Yêu cầu thay đổi tài khoản không hợp lệ.",
      "error",
    );

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

  if (error.status === 409) {
    showMessage(
      error.data?.message ||
        "Không thể hạ vai trò của Admin đang hoạt động cuối cùng.",
      "error",
    );

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

  messageElement.classList.remove("error", "success");

  messageElement.classList.add(type);

  messageElement.hidden = false;
}

function hideMessage() {
  messageElement.textContent = "";
  messageElement.hidden = true;

  messageElement.classList.remove("error", "success");
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

function registerRoleEventListeners() {
  roleSelect.addEventListener("change", updateRoleButtonState);

  changeRoleButton.addEventListener("click", openRoleConfirmDialog);

  cancelRoleButton.addEventListener("click", closeRoleConfirmDialog);

  confirmRoleButton.addEventListener("click", confirmRoleChange);

  document.querySelectorAll("[data-role-dialog-close]").forEach((element) => {
    element.addEventListener("click", closeRoleConfirmDialog);
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && !roleConfirmDialog.hidden) {
      closeRoleConfirmDialog();
    }
  });
}

function configureRoleControls(user) {
  const supportedRole = user.role === "USER" || user.role === "ADMIN";

  if (!supportedRole) {
    roleSelect.disabled = true;
    changeRoleButton.disabled = true;
    return;
  }

  roleSelect.disabled = false;
  roleSelect.value = user.role;

  updateRoleButtonState();
}

function updateRoleButtonState() {
  if (!displayedUser || roleSelect.disabled) {
    changeRoleButton.disabled = true;
    return;
  }

  changeRoleButton.disabled = roleSelect.value === displayedUser.role;
}

function openRoleConfirmDialog() {
  if (!displayedUser || changeRoleButton.disabled) {
    return;
  }

  pendingRole = roleSelect.value;

  if (pendingRole !== "USER" && pendingRole !== "ADMIN") {
    showMessage("Vai trò được chọn không hợp lệ.", "error");

    return;
  }

  const displayName =
    displayedUser.fullName ||
    buildFullName(displayedUser) ||
    displayedUser.email ||
    "tài khoản này";

  const oldRole = formatRole(displayedUser.role);

  const newRole = formatRole(pendingRole);

  roleDialogTitle.textContent = "Xác nhận thay đổi vai trò";

  roleDialogMessage.textContent =
    `Bạn có chắc chắn muốn thay đổi vai trò của tài khoản ` +
    `“${displayName}” từ ${oldRole} thành ${newRole}?`;

  const currentUser = getCurrentUser();

  if (isSameAccount(currentUser, displayedUser) && pendingRole === "USER") {
    roleDialogMessage.textContent +=
      " Sau khi thực hiện, bạn sẽ mất quyền quản trị.";
  }

  roleConfirmDialog.hidden = false;

  document.body.classList.add("dialog-open");

  confirmRoleButton.focus();
}

function closeRoleConfirmDialog() {
  roleConfirmDialog.hidden = true;

  document.body.classList.remove("dialog-open");

  pendingRole = null;

  if (!changeRoleButton.disabled) {
    changeRoleButton.focus();
  }
}

async function confirmRoleChange() {
  if (!displayedUser || !pendingRole) {
    return;
  }

  const requestedRole = pendingRole;

  const currentUser = getCurrentUser();

  const changingOwnAccount = isSameAccount(currentUser, displayedUser);

  setRoleActionLoading(true);

  try {
    const updatedUser = await apiRequest(
      `/admin/users/${encodeURIComponent(displayedUser.id)}/role`,
      {
        method: "PATCH",

        headers: {
          "Content-Type": "application/json",
        },

        body: JSON.stringify({
          role: requestedRole,
        }),

        timeoutMs: REQUEST_TIMEOUT_MS,
      },
    );

    displayedUser = {
      ...displayedUser,
      ...updatedUser,
    };

    closeRoleConfirmDialog();

    renderUserDetail(displayedUser);

    if (changingOwnAccount && requestedRole === "USER") {
      showMessage(
        "Thay đổi vai trò thành công. Bạn đã mất quyền quản trị và cần đăng nhập lại.",
        "success",
      );

      clearAuthSession();

      window.setTimeout(redirectToLogin, 1800);

      return;
    }

    showMessage(
      requestedRole === "ADMIN"
        ? "Nâng tài khoản thành Admin thành công."
        : "Hạ tài khoản thành User thành công.",
      "success",
    );
  } catch (error) {
    closeRoleConfirmDialog();
    handleApiError(error);
  } finally {
    setRoleActionLoading(false);
  }
}

function setRoleActionLoading(loading) {
  roleSelect.disabled = loading;

  confirmRoleButton.disabled = loading;

  cancelRoleButton.disabled = loading;

  if (loading) {
    changeRoleButton.disabled = true;

    confirmRoleButton.textContent = "Đang xử lý...";

    return;
  }

  confirmRoleButton.textContent = "Xác nhận";

  configureRoleControls(displayedUser);
}

function redirectToLogin() {
  window.location.replace("/pages/login.html");
}
