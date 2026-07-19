import { apiRequest } from "/js/api.js";

import { clearAuthSession, isAuthenticated } from "/js/auth.js";

const DEFAULT_AVATAR_URL = "/images/default-avatar.svg";

const NORMAL_REQUEST_TIMEOUT_MS = 3000;

const AVATAR_UPLOAD_TIMEOUT_MS = 30000;

const MAX_AVATAR_SIZE = 2 * 1024 * 1024;

const ALLOWED_AVATAR_TYPES = new Set(["image/jpeg", "image/png"]);

const ALLOWED_AVATAR_EXTENSIONS = new Set(["jpg", "jpeg", "png"]);

const loadingElement = document.getElementById("profile-loading");

const contentElement = document.getElementById("profile-content");

const pageMessageElement = document.getElementById("page-message");

const profileForm = document.getElementById("profile-form");

const profileFormMessage = document.getElementById("profile-form-message");

const profileSubmitButton = document.getElementById("profile-submit");

const profileResetButton = document.getElementById("profile-reset");

const profileEditButton = document.getElementById("profile-edit");

const avatarForm = document.getElementById("avatar-form");

const avatarInput = document.getElementById("avatar-file");

const avatarElement = document.getElementById("profile-avatar");

const avatarFileInfo = document.getElementById("avatar-file-info");

const avatarMessageElement = document.getElementById("avatar-message");

const avatarSubmitButton = document.getElementById("avatar-submit");

const dateOfBirthInput = document.getElementById("date-of-birth");

let currentProfile = null;
let previewObjectUrl = null;
let isEditing = false;

initializePage().catch(handleInitializationError);

async function initializePage() {
  try {
    if (!isAuthenticated()) {
      redirectToLogin();
      return;
    }

    validateRequiredElements();

    loadingElement.hidden = false;
    contentElement.hidden = true;

    hideMessage(pageMessageElement);

    setMaximumDate();
    registerEventListeners();

    console.log("Bắt đầu gọi GET /api/users/me");

    const profile = await apiRequest("/users/me", {
      method: "GET",
      timeoutMs: NORMAL_REQUEST_TIMEOUT_MS,
    });

    currentProfile = profile;

    renderProfile(profile);
    setProfileEditMode(false);

    loadingElement.hidden = true;
    contentElement.hidden = false;

    console.log("Tải hồ sơ thành công", profile);
  } catch (error) {
    console.error("Không thể tải trang hồ sơ:", error);

    loadingElement.hidden = true;
    contentElement.hidden = true;

    handleApiError(error, pageMessageElement);
  }
}

function validateRequiredElements() {
  const requiredElements = {
    loadingElement,
    contentElement,
    pageMessageElement,
    profileForm,
    profileFormMessage,
    profileSubmitButton,
    profileResetButton,
    profileEditButton,
    avatarForm,
    avatarInput,
    avatarElement,
    avatarFileInfo,
    avatarMessageElement,
    avatarSubmitButton,
    dateOfBirthInput,
  };

  for (const [elementName, element] of Object.entries(requiredElements)) {
    if (!element) {
      throw new Error(`Không tìm thấy phần tử giao diện: ${elementName}`);
    }
  }
}

function registerEventListeners() {
  profileForm.addEventListener("submit", handleProfileSubmit);

  profileResetButton.addEventListener("click", resetProfileForm);

  avatarInput.addEventListener("change", handleAvatarSelection);

  avatarForm.addEventListener("submit", handleAvatarSubmit);

  avatarElement.addEventListener("error", handleAvatarLoadError);

  profileEditButton.addEventListener("click", enableProfileEditing);
}

async function handleProfileSubmit(event) {
  event.preventDefault();

  if (!isEditing) {
    return;
  }

  clearFieldErrors();

  hideMessage(profileFormMessage);

  const requestData = readProfileForm();

  const validationErrors = validateProfileData(requestData);

  if (Object.keys(validationErrors).length > 0) {
    renderFieldErrors(validationErrors);

    showMessage(
      profileFormMessage,
      "Vui lòng kiểm tra lại thông tin đã nhập.",
      "error",
    );

    return;
  }

  setButtonLoading(profileSubmitButton, true, "Đang lưu...");

  try {
    const updatedProfile = await apiRequest("/users/me", {
      method: "PUT",
      body: requestData,
      timeoutMs: NORMAL_REQUEST_TIMEOUT_MS,
    });

    currentProfile = updatedProfile;

    renderProfile(updatedProfile);

    syncCurrentUser(updatedProfile);

    setProfileEditMode(false);

    showMessage(
      profileFormMessage,
      "Cập nhật thông tin thành công.",
      "success",
    );
  } catch (error) {
    if (error.status === 400 && error.data?.errors) {
      renderFieldErrors(error.data.errors);
    }

    handleApiError(error, profileFormMessage);
  } finally {
    setButtonLoading(profileSubmitButton, false, "Lưu thông tin");
  }
}

async function handleAvatarSubmit(event) {
  event.preventDefault();

  hideMessage(avatarMessageElement);

  const file = avatarInput.files?.[0];

  const validationMessage = validateAvatar(file);

  if (validationMessage) {
    showMessage(avatarMessageElement, validationMessage, "error");

    return;
  }

  const formData = new FormData();

  formData.append("file", file);

  setButtonLoading(avatarSubmitButton, true, "Đang tải ảnh...");

  try {
    const updatedProfile = await apiRequest("/users/me/avatar", {
      method: "PUT",
      body: formData,
      timeoutMs: AVATAR_UPLOAD_TIMEOUT_MS,
    });

    currentProfile = updatedProfile;

    releasePreviewUrl();

    renderProfile(updatedProfile);

    syncCurrentUser(updatedProfile);

    avatarForm.reset();

    avatarFileInfo.textContent = "";

    showMessage(
      avatarMessageElement,
      "Cập nhật ảnh đại diện thành công.",
      "success",
    );
  } catch (error) {
    handleApiError(error, avatarMessageElement);
  } finally {
    setButtonLoading(avatarSubmitButton, false, "Cập nhật ảnh");
  }
}

function handleAvatarSelection() {
  hideMessage(avatarMessageElement);

  releasePreviewUrl();

  const file = avatarInput.files?.[0];

  if (!file) {
    avatarFileInfo.textContent = "";

    renderAvatar(currentProfile?.avatarUrl);

    return;
  }

  avatarFileInfo.textContent = `${file.name} — ${formatFileSize(file.size)}`;

  const validationMessage = validateAvatar(file);

  if (validationMessage) {
    showMessage(avatarMessageElement, validationMessage, "error");

    renderAvatar(currentProfile?.avatarUrl);

    return;
  }

  previewObjectUrl = URL.createObjectURL(file);

  avatarElement.src = previewObjectUrl;
}

function validateAvatar(file) {
  if (!file) {
    return "Vui lòng chọn ảnh đại diện.";
  }

  if (file.size === 0) {
    return "File ảnh đại diện không được để trống.";
  }

  const extension = getFileExtension(file.name);

  if (
    !ALLOWED_AVATAR_TYPES.has(file.type) ||
    !ALLOWED_AVATAR_EXTENSIONS.has(extension)
  ) {
    return "Chỉ chấp nhận ảnh JPG, JPEG hoặc PNG.";
  }

  if (file.size > MAX_AVATAR_SIZE) {
    return "Ảnh đại diện không được vượt quá 2 MB.";
  }

  return null;
}

function readProfileForm() {
  return {
    firstName: normalizeText(document.getElementById("first-name").value),

    lastName: normalizeText(document.getElementById("last-name").value),

    dateOfBirth: dateOfBirthInput.value,

    gender: document.getElementById("gender").value,

    address: normalizeText(document.getElementById("address").value),

    phone: document.getElementById("phone").value.trim(),
  };
}

function validateProfileData(data) {
  const errors = {};

  if (!data.firstName) {
    errors.firstName = "Tên không được để trống.";
  } else if (data.firstName.length > 100) {
    errors.firstName = "Tên không được vượt quá 100 ký tự.";
  }

  if (!data.lastName) {
    errors.lastName = "Họ và tên đệm không được để trống.";
  } else if (data.lastName.length > 150) {
    errors.lastName = "Họ và tên đệm không được vượt quá 150 ký tự.";
  }

  if (!data.dateOfBirth) {
    errors.dateOfBirth = "Ngày sinh không được để trống.";
  } else if (data.dateOfBirth > getTodayValue()) {
    errors.dateOfBirth = "Ngày sinh không được lớn hơn ngày hiện tại.";
  }

  if (!["MALE", "FEMALE", "OTHER"].includes(data.gender)) {
    errors.gender = "Vui lòng chọn giới tính.";
  }

  if (!data.address) {
    errors.address = "Địa chỉ không được để trống.";
  } else if (data.address.length > 255) {
    errors.address = "Địa chỉ không được vượt quá 255 ký tự.";
  }

  if (!data.phone) {
    errors.phone = "Số điện thoại không được để trống.";
  } else if (!/^\d{10}$/.test(data.phone)) {
    errors.phone = "Số điện thoại phải gồm đúng 10 chữ số.";
  }

  return errors;
}

function renderProfile(profile) {
  document.getElementById("profile-full-name").textContent =
    profile.fullName || "Người dùng";

  document.getElementById("profile-email").textContent = profile.email || "—";

  document.getElementById("first-name").value = profile.firstName || "";

  document.getElementById("last-name").value = profile.lastName || "";

  document.getElementById("email").value = profile.email || "";

  dateOfBirthInput.value = profile.dateOfBirth || "";

  document.getElementById("gender").value = profile.gender || "";

  document.getElementById("address").value = profile.address || "";

  document.getElementById("phone").value = profile.phone || "";

  renderAvatar(profile.avatarUrl);
}

function renderAvatar(avatarUrl) {
  avatarElement.src = normalizeAvatarUrl(avatarUrl);
}

function normalizeAvatarUrl(avatarUrl) {
  if (typeof avatarUrl !== "string" || !avatarUrl.trim()) {
    return DEFAULT_AVATAR_URL;
  }

  const normalized = avatarUrl.trim();

  if (
    normalized.startsWith("http://") ||
    normalized.startsWith("https://") ||
    normalized.startsWith("/")
  ) {
    return normalized;
  }

  return `/${normalized}`;
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

function handleApiError(error, messageElement) {
  console.error("API error:", error);

  if (error.code === "TIMEOUT") {
    showMessage(
      messageElement,
      "Máy chủ phản hồi quá lâu. Vui lòng thử lại.",
      "error",
    );

    return;
  }

  if (error.status === 400) {
    showMessage(
      messageElement,
      error.data?.message || "Dữ liệu gửi lên không hợp lệ.",
      "error",
    );

    return;
  }

  if (error.status === 401) {
    clearAuthSession();

    showMessage(
      messageElement,
      error.data?.message || "Phiên đăng nhập không hợp lệ hoặc đã hết hạn.",
      "error",
    );

    redirectToLoginAfterDelay();

    return;
  }

  if (error.status === 403) {
    clearAuthSession();

    showMessage(
      messageElement,
      error.data?.message ||
        "Tài khoản không có quyền thực hiện chức năng này.",
      "error",
    );

    redirectToLoginAfterDelay();

    return;
  }

  if (error.status === 404) {
    showMessage(
      messageElement,
      error.data?.message || "Không tìm thấy tài khoản cần cập nhật.",
      "error",
    );

    return;
  }

  if (error.status === 500) {
    showMessage(
      messageElement,
      error.data?.message || "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.",
      "error",
    );

    return;
  }

  if (error.code === "NETWORK_ERROR") {
    showMessage(
      messageElement,
      "Không thể kết nối đến máy chủ. Hãy kiểm tra Backend đang chạy.",
      "error",
    );

    return;
  }

  if (!error.status) {
    showMessage(
      messageElement,
      error.message || "Không thể khởi tạo hoặc tải trang hồ sơ.",
      "error",
    );

    return;
  }

  showMessage(messageElement, "Không thể thực hiện yêu cầu.", "error");
}

function showMessage(element, message, type) {
  if (!element) {
    console.error("Không tìm thấy phần tử hiển thị thông báo");

    return;
  }

  element.textContent = message;

  element.classList.remove("success", "error");

  element.classList.add(type);

  element.hidden = false;
}

function hideMessage(element) {
  if (!element) {
    return;
  }

  element.textContent = "";

  element.hidden = true;

  element.classList.remove("success", "error");
}

function setButtonLoading(button, loading, text) {
  button.disabled = loading;

  button.textContent = text;
}

function setMaximumDate() {
  dateOfBirthInput.max = getTodayValue();
}

function getTodayValue() {
  const now = new Date();

  const localDate = new Date(now.getTime() - now.getTimezoneOffset() * 60_000);

  return localDate.toISOString().slice(0, 10);
}

function normalizeText(value) {
  return value.trim().replace(/\s+/g, " ");
}

function getFileExtension(filename) {
  const dotIndex = filename.lastIndexOf(".");

  if (dotIndex === -1) {
    return "";
  }

  return filename.slice(dotIndex + 1).toLowerCase();
}

function formatFileSize(size) {
  if (size < 1024) {
    return `${size} B`;
  }

  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  }

  return `${(size / 1024 / 1024).toFixed(2)} MB`;
}

function syncCurrentUser(profile) {
  let storedUser = {};

  try {
    storedUser = JSON.parse(localStorage.getItem("currentUser") || "{}");
  } catch {
    storedUser = {};
  }

  const updatedStoredUser = {
    ...storedUser,
    id: profile.id,
    firstName: profile.firstName,
    lastName: profile.lastName,
    fullName: profile.fullName,
    email: profile.email,
    gender: profile.gender,
    avatarUrl: profile.avatarUrl,
    role: profile.role,
    status: profile.status,
  };

  localStorage.setItem("currentUser", JSON.stringify(updatedStoredUser));

  const headerNameElement = document.querySelector("[data-auth-name]");

  if (headerNameElement) {
    headerNameElement.textContent =
      profile.fullName || profile.email || "Người dùng";
  }
}

function handleAvatarLoadError() {
  const currentSource = avatarElement.getAttribute("src");

  if (currentSource === DEFAULT_AVATAR_URL) {
    return;
  }

  avatarElement.src = DEFAULT_AVATAR_URL;
}

function releasePreviewUrl() {
  if (!previewObjectUrl) {
    return;
  }

  URL.revokeObjectURL(previewObjectUrl);

  previewObjectUrl = null;
}

function handleInitializationError(error) {
  console.error("Profile initialization error:", error);

  if (loadingElement) {
    loadingElement.hidden = true;
  }

  if (contentElement) {
    contentElement.hidden = true;
  }

  if (pageMessageElement) {
    showMessage(
      pageMessageElement,
      error.message ||
        "Không thể khởi tạo trang hồ sơ. Vui lòng tải lại trang.",
      "error",
    );
  }
}

function enableProfileEditing() {
  clearFieldErrors();

  hideMessage(profileFormMessage);

  setProfileEditMode(true);

  document.getElementById("last-name").focus();
}

function setProfileEditMode(editing) {
  isEditing = editing;

  const textInputs = [
    document.getElementById("last-name"),
    document.getElementById("first-name"),
    document.getElementById("date-of-birth"),
    document.getElementById("address"),
    document.getElementById("phone"),
  ];

  textInputs.forEach((input) => {
    input.readOnly = !editing;
  });

  document.getElementById("gender").disabled = !editing;

  profileEditButton.hidden = editing;

  profileResetButton.hidden = !editing;

  profileSubmitButton.hidden = !editing;

  profileForm.classList.toggle("editing", editing);
}

function resetProfileForm() {
  clearFieldErrors();

  hideMessage(profileFormMessage);

  if (currentProfile) {
    renderProfile(currentProfile);
  }

  setProfileEditMode(false);
}

function redirectToLogin() {
  window.location.replace("/pages/login.html");
}

function redirectToLoginAfterDelay() {
  window.setTimeout(redirectToLogin, 1500);
}
