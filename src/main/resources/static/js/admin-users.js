import { apiRequest } from "/js/api.js";

import { clearAuthSession, isAuthenticated } from "/js/auth.js";

const REQUEST_TIMEOUT_MS = 5000;

const MAX_KEYWORD_LENGTH = 100;

const ALLOWED_PAGE_SIZES = new Set([5, 10, 20, 50]);

const ALLOWED_ROLES = new Set(["", "USER", "ADMIN"]);

const ALLOWED_STATUSES = new Set(["", "ACTIVE", "LOCKED"]);

const ALLOWED_SORT_FIELDS = new Set([
  "id",
  "lastName",
  "email",
  "role",
  "status",
  "createdAt",
  "updatedAt",
]);

const ALLOWED_DIRECTIONS = new Set(["asc", "desc"]);

const DEFAULT_AVATAR_URL = "/images/default-avatar.svg";

const filterForm = document.getElementById("user-filter-form");

const keywordInput = document.getElementById("keyword");

const pageInput = document.getElementById("page-number");

const sizeInput = document.getElementById("page-size");

const roleInput = document.getElementById("role-filter");

const statusInput = document.getElementById("status-filter");

const sortByInput = document.getElementById("sort-by");

const directionInput = document.getElementById("sort-direction");

const resetButton = document.getElementById("filter-reset");

const submitButton = document.getElementById("filter-submit");

const loadingElement = document.getElementById("users-loading");

const emptyElement = document.getElementById("users-empty");

const contentElement = document.getElementById("users-content");

const pageMessageElement = document.getElementById("page-message");

const tableBody = document.getElementById("users-table-body");

const previousButton = document.getElementById("previous-page");

const nextButton = document.getElementById("next-page");

const paginationInfo = document.getElementById("pagination-info");

initializePage().catch(handleInitializationError);

async function initializePage() {
  if (!isAuthenticated()) {
    redirectToLogin();
    return;
  }

  validateRequiredElements();
  registerEventListeners();

  await loadUsers();
}

function validateRequiredElements() {
  const requiredElements = {
    filterForm,
    keywordInput,
    pageInput,
    sizeInput,
    roleInput,
    statusInput,
    sortByInput,
    directionInput,
    resetButton,
    submitButton,
    loadingElement,
    emptyElement,
    contentElement,
    pageMessageElement,
    tableBody,
    previousButton,
    nextButton,
    paginationInfo,
  };

  for (const [name, element] of Object.entries(requiredElements)) {
    if (!element) {
      throw new Error(`Không tìm thấy phần tử giao diện: ${name}`);
    }
  }
}

function registerEventListeners() {
  filterForm.addEventListener("submit", handleFilterSubmit);

  resetButton.addEventListener("click", resetFilters);

  previousButton.addEventListener("click", goToPreviousPage);

  nextButton.addEventListener("click", goToNextPage);
}

async function handleFilterSubmit(event) {
  event.preventDefault();

  /*
   * Khi thay đổi tìm kiếm hoặc bộ lọc,
   * luôn quay lại trang đầu tiên.
   */
  pageInput.value = "1";

  await loadUsers();
}

async function loadUsers() {
  clearFieldErrors();
  hideMessage();

  const filterData = readFilterData();

  const validationErrors = validateFilterData(filterData);

  if (Object.keys(validationErrors).length > 0) {
    renderFieldErrors(validationErrors);

    showMessage("Vui lòng kiểm tra lại điều kiện tìm kiếm và lọc.", "error");

    return;
  }

  showLoadingState(true);

  try {
    const queryString = buildQueryString(filterData);

    const pageData = await apiRequest(`/admin/users?${queryString}`, {
      method: "GET",
      timeoutMs: REQUEST_TIMEOUT_MS,
    });

    renderPage(pageData);
  } catch (error) {
    handleApiError(error);
  } finally {
    showLoadingState(false);
  }
}

function readFilterData() {
  return {
    keyword: normalizeKeyword(keywordInput.value),

    page: Number(pageInput.value),

    size: Number(sizeInput.value),

    role: roleInput.value,

    status: statusInput.value,

    sortBy: sortByInput.value,

    direction: directionInput.value,
  };
}

function normalizeKeyword(value) {
  return value.trim().replace(/\s+/g, " ");
}

function validateFilterData(data) {
  const errors = {};

  if (data.keyword.length > MAX_KEYWORD_LENGTH) {
    errors.keyword = "Từ khóa tìm kiếm không được vượt quá 100 ký tự.";
  }

  if (!Number.isInteger(data.page) || data.page < 1) {
    errors.page = "Số trang phải là số nguyên từ 1 trở lên.";
  }

  if (!ALLOWED_PAGE_SIZES.has(data.size)) {
    errors.size = "Số dòng mỗi trang không hợp lệ.";
  }

  if (!ALLOWED_ROLES.has(data.role)) {
    errors.role = "Vai trò không hợp lệ.";
  }

  if (!ALLOWED_STATUSES.has(data.status)) {
    errors.status = "Trạng thái không hợp lệ.";
  }

  if (!ALLOWED_SORT_FIELDS.has(data.sortBy)) {
    errors.sortBy = "Trường sắp xếp không hợp lệ.";
  }

  if (!ALLOWED_DIRECTIONS.has(data.direction)) {
    errors.direction = "Chiều sắp xếp không hợp lệ.";
  }

  return errors;
}

function buildQueryString(data) {
  const params = new URLSearchParams();

  /*
   * Giao diện tính trang từ 1,
   * Spring Data tính trang từ 0.
   */
  params.set("page", String(data.page - 1));

  params.set("size", String(data.size));

  params.set("sortBy", data.sortBy);

  params.set("direction", data.direction);

  if (data.keyword) {
    params.set("keyword", data.keyword);
  }

  if (data.role) {
    params.set("role", data.role);
  }

  if (data.status) {
    params.set("status", data.status);
  }

  return params.toString();
}

function renderPage(pageData) {
  const users = Array.isArray(pageData?.content) ? pageData.content : [];

  const currentPage = pageData?.page ?? pageData?.number ?? 0;

  const totalPages = pageData?.totalPages ?? 0;

  const totalElements = pageData?.totalElements ?? users.length;

  pageInput.value = String(currentPage + 1);

  paginationInfo.textContent =
    `Trang ${currentPage + 1}/${Math.max(totalPages, 1)}` +
    ` — Tổng ${totalElements} tài khoản`;

  previousButton.disabled = pageData?.first ?? currentPage <= 0;

  nextButton.disabled = pageData?.last ?? currentPage + 1 >= totalPages;

  tableBody.replaceChildren();

  if (users.length === 0) {
    emptyElement.hidden = false;
    contentElement.hidden = true;

    return;
  }

  users.forEach((user) => {
    tableBody.appendChild(createUserRow(user));
  });

  emptyElement.hidden = true;
  contentElement.hidden = false;
}

function createUserRow(user) {
  const row = document.createElement("tr");

  row.appendChild(createTextCell(user.id ?? "—"));

  const accountCell = document.createElement("td");

  const accountWrapper = document.createElement("div");

  accountWrapper.className = "user-cell";

  const avatar = document.createElement("img");

  avatar.className = "user-avatar";

  avatar.alt = `Ảnh đại diện của ${
    user.fullName || user.email || "người dùng"
  }`;

  avatar.src = normalizeAvatarUrl(user.avatarUrl);

  avatar.addEventListener("error", () => {
    if (avatar.getAttribute("src") === DEFAULT_AVATAR_URL) {
      return;
    }

    avatar.src = DEFAULT_AVATAR_URL;
  });

  const name = document.createElement("span");

  name.className = "user-name";

  name.textContent = user.fullName || buildFullName(user) || "Người dùng";

  accountWrapper.append(avatar, name);

  accountCell.appendChild(accountWrapper);

  row.appendChild(accountCell);

  row.appendChild(createTextCell(user.email || "—"));

  row.appendChild(
    createBadgeCell(
      formatRole(user.role),
      user.role === "ADMIN" ? "role-badge role-admin" : "role-badge role-user",
    ),
  );

  row.appendChild(
    createBadgeCell(
      formatStatus(user.status),
      user.status === "LOCKED"
        ? "status-badge status-locked"
        : "status-badge status-active",
    ),
  );

  row.appendChild(createTextCell(formatDateTime(user.createdAt)));

  row.appendChild(createActionCell(user.id));

  return row;
}

function createActionCell(userId) {
  const cell = document.createElement("td");

  const detailLink = document.createElement("a");

  detailLink.className = "detail-link";

  detailLink.href = `/pages/admin-user-detail.html?id=${encodeURIComponent(userId)}`;

  detailLink.textContent = "Xem chi tiết";

  cell.appendChild(detailLink);

  return cell;
}

function buildFullName(user) {
  return `${user.lastName ?? ""} ${user.firstName ?? ""}`
    .trim()
    .replace(/\s+/g, " ");
}

function createTextCell(value) {
  const cell = document.createElement("td");

  cell.textContent = String(value);

  return cell;
}

function createBadgeCell(text, className) {
  const cell = document.createElement("td");

  const badge = document.createElement("span");

  badge.className = className;

  badge.textContent = text;

  cell.appendChild(badge);

  return cell;
}

async function goToPreviousPage() {
  const currentPage = Number(pageInput.value);

  if (!Number.isInteger(currentPage) || currentPage <= 1) {
    return;
  }

  pageInput.value = String(currentPage - 1);

  await loadUsers();
}

async function goToNextPage() {
  const currentPage = Number(pageInput.value);

  if (!Number.isInteger(currentPage) || nextButton.disabled) {
    return;
  }

  pageInput.value = String(currentPage + 1);

  await loadUsers();
}

async function resetFilters() {
  keywordInput.value = "";
  pageInput.value = "1";
  sizeInput.value = "10";
  roleInput.value = "";
  statusInput.value = "";
  sortByInput.value = "createdAt";
  directionInput.value = "desc";

  clearFieldErrors();
  hideMessage();

  await loadUsers();
}

function renderFieldErrors(errors) {
  for (const [fieldName, message] of Object.entries(errors)) {
    const errorElement = document.querySelector(
      `[data-field-error="${fieldName}"]`,
    );

    const inputElement = document.querySelector(`[name="${fieldName}"]`);

    if (errorElement) {
      errorElement.textContent = message;
    }

    if (inputElement) {
      inputElement.classList.add("input-invalid");
    }
  }
}

function clearFieldErrors() {
  document.querySelectorAll("[data-field-error]").forEach((element) => {
    element.textContent = "";
  });

  document.querySelectorAll(".input-invalid").forEach((element) => {
    element.classList.remove("input-invalid");
  });
}

function showLoadingState(loading) {
  loadingElement.hidden = !loading;

  if (loading) {
    emptyElement.hidden = true;
    contentElement.hidden = true;

    previousButton.disabled = true;
    nextButton.disabled = true;
  }

  submitButton.disabled = loading;

  resetButton.disabled = loading;

  submitButton.textContent = loading ? "Đang tải..." : "Áp dụng";
}

function handleApiError(error) {
  console.error("Admin user list error:", error);

  contentElement.hidden = true;
  emptyElement.hidden = true;

  if (error.code === "TIMEOUT") {
    showMessage("Máy chủ phản hồi quá lâu. Vui lòng thử lại.", "error");

    return;
  }

  if (error.code === "NETWORK_ERROR") {
    showMessage(
      "Không thể kết nối đến máy chủ. Hãy kiểm tra Backend đang chạy.",
      "error",
    );

    return;
  }

  if (error.status === 400) {
    if (error.data?.errors) {
      renderFieldErrors(error.data.errors);
    }

    showMessage(
      error.data?.message || "Tham số danh sách không hợp lệ.",
      "error",
    );

    return;
  }

  if (error.status === 401) {
    clearAuthSession();

    showMessage(
      error.data?.message || "Phiên đăng nhập không hợp lệ hoặc đã hết hạn.",
      "error",
    );

    window.setTimeout(redirectToLogin, 1500);

    return;
  }

  if (error.status === 403) {
    showMessage(
      error.data?.message || "Bạn không có quyền xem danh sách tài khoản.",
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

  showMessage(error.message || "Không thể tải danh sách tài khoản.", "error");
}

function showMessage(message, type) {
  if (!pageMessageElement) {
    return;
  }

  pageMessageElement.textContent = message;

  pageMessageElement.classList.remove("success", "error");

  pageMessageElement.classList.add(type);

  pageMessageElement.hidden = false;
}

function hideMessage() {
  if (!pageMessageElement) {
    return;
  }

  pageMessageElement.textContent = "";
  pageMessageElement.hidden = true;

  pageMessageElement.classList.remove("success", "error");
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

function formatRole(role) {
  if (role === "ADMIN") {
    return "Admin";
  }

  if (role === "USER") {
    return "User";
  }

  return role || "—";
}

function formatStatus(status) {
  if (status === "ACTIVE") {
    return "Đang hoạt động";
  }

  if (status === "LOCKED") {
    return "Đã khóa";
  }

  return status || "—";
}

function formatDateTime(value) {
  if (!value) {
    return "—";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "—";
  }

  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(date);
}

function handleInitializationError(error) {
  console.error("Admin user page initialization error:", error);

  if (loadingElement) {
    loadingElement.hidden = true;
  }

  if (contentElement) {
    contentElement.hidden = true;
  }

  if (emptyElement) {
    emptyElement.hidden = true;
  }

  showMessage(
    error.message || "Không thể khởi tạo trang danh sách tài khoản.",
    "error",
  );
}

function redirectToLogin() {
  window.location.replace("/pages/login.html");
}
