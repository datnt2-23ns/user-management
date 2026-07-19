import { apiRequest } from "/js/api.js";

const form = document.getElementById("register-form");
const registerButton = document.getElementById("register-button");

const messageElement = document.getElementById("form-message");

const lastNameInput = document.getElementById("lastName");

const firstNameInput = document.getElementById("firstName");

const emailInput = document.getElementById("email");

const passwordInput = document.getElementById("password");

const dateOfBirthInput = document.getElementById("dateOfBirth");

const genderInput = document.getElementById("gender");

const addressInput = document.getElementById("address");

const phoneInput = document.getElementById("phone");

const fieldNames = [
  "lastName",
  "firstName",
  "email",
  "password",
  "dateOfBirth",
  "gender",
  "address",
  "phone",
];

initializeForm();

function initializeForm() {
  dateOfBirthInput.max = getToday();

  phoneInput.addEventListener("input", () => {
    phoneInput.value = phoneInput.value.replace(/\D/g, "").slice(0, 10);

    clearFieldError("phone");
  });

  fieldNames.forEach((fieldName) => {
    const input = document.getElementById(fieldName);

    input.addEventListener("input", () => {
      clearFieldError(fieldName);
      clearMessage();
    });

    input.addEventListener("change", () => {
      clearFieldError(fieldName);
      clearMessage();
    });
  });

  form.addEventListener("submit", handleSubmit);
}

async function handleSubmit(event) {
  event.preventDefault();

  clearAllFieldErrors();
  clearMessage();

  const payload = getFormPayload();
  const validationErrors = validate(payload);

  if (Object.keys(validationErrors).length > 0) {
    showFieldErrors(validationErrors);

    showMessage("error", "Vui lòng kiểm tra lại các thông tin đã nhập.");

    focusFirstInvalidField(validationErrors);
    return;
  }

  setSubmitting(true);

  try {
    const response = await apiRequest("/auth/register", {
      method: "POST",
      body: JSON.stringify(payload),
    });

    showMessage("success", `Đăng ký thành công tài khoản ${response.email}.`);

    form.reset();
    dateOfBirthInput.max = getToday();

    lastNameInput.focus();
  } catch (error) {
    handleApiError(error);
  } finally {
    setSubmitting(false);
  }
}

function getFormPayload() {
  return {
    lastName: normalizeText(lastNameInput.value),
    firstName: normalizeText(firstNameInput.value),

    email: emailInput.value.trim().toLowerCase(),

    /*
     * Không trim mật khẩu vì khoảng trắng
     * có thể là một phần của mật khẩu.
     */
    password: passwordInput.value,

    dateOfBirth: dateOfBirthInput.value,
    gender: genderInput.value,

    address: normalizeText(addressInput.value),

    phone: phoneInput.value.trim(),
  };
}

function validate(payload) {
  const errors = {};

  if (!payload.lastName) {
    errors.lastName = "Họ và tên đệm không được để trống.";
  } else if (payload.lastName.length > 150) {
    errors.lastName = "Họ và tên đệm không được vượt quá 150 ký tự.";
  }

  if (!payload.firstName) {
    errors.firstName = "Tên không được để trống.";
  } else if (payload.firstName.length > 100) {
    errors.firstName = "Tên không được vượt quá 100 ký tự.";
  }

  if (!payload.email) {
    errors.email = "Email không được để trống.";
  } else if (!isValidEmail(payload.email)) {
    errors.email = "Email không đúng định dạng.";
  } else if (payload.email.length > 320) {
    errors.email = "Email không được vượt quá 320 ký tự.";
  }

  if (!payload.password) {
    errors.password = "Mật khẩu không được để trống.";
  } else if (payload.password.length < 8 || payload.password.length > 64) {
    errors.password = "Mật khẩu phải có từ 8 đến 64 ký tự.";
  }

  if (!payload.dateOfBirth) {
    errors.dateOfBirth = "Ngày sinh không được để trống.";
  } else if (payload.dateOfBirth > getToday()) {
    errors.dateOfBirth = "Ngày sinh không được lớn hơn ngày hiện tại.";
  }

  const validGenders = ["MALE", "FEMALE", "OTHER"];

  if (!payload.gender) {
    errors.gender = "Giới tính không được để trống.";
  } else if (!validGenders.includes(payload.gender)) {
    errors.gender = "Giới tính không hợp lệ.";
  }

  if (!payload.address) {
    errors.address = "Địa chỉ không được để trống.";
  } else if (payload.address.length > 255) {
    errors.address = "Địa chỉ không được vượt quá 255 ký tự.";
  }

  if (!payload.phone) {
    errors.phone = "Số điện thoại không được để trống.";
  } else if (!/^\d{10}$/.test(payload.phone)) {
    errors.phone = "Số điện thoại phải gồm đúng 10 chữ số.";
  }

  return errors;
}

function handleApiError(error) {
  if (error.status === 400) {
    const backendErrors = error.data?.errors || {};

    showFieldErrors(backendErrors);

    showMessage(
      "error",
      error.data?.message || "Dữ liệu đăng ký không hợp lệ.",
    );

    focusFirstInvalidField(backendErrors);
    return;
  }

  if (error.status === 409) {
    showFieldError(
      "email",
      error.data?.message || "Email đã tồn tại trong hệ thống.",
    );

    showMessage(
      "error",
      error.data?.message || "Email đã tồn tại trong hệ thống.",
    );

    emailInput.focus();
    return;
  }

  if (error.status === 500) {
    showMessage(
      "error",
      error.data?.message || "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.",
    );

    return;
  }

  if (!error.status) {
    showMessage(
      "error",
      "Không thể kết nối đến máy chủ. Hãy kiểm tra Backend đang chạy.",
    );

    return;
  }

  showMessage("error", "Đăng ký thất bại. Vui lòng thử lại.");
}

function showFieldErrors(errors) {
  Object.entries(errors).forEach(([fieldName, message]) => {
    showFieldError(fieldName, message);
  });
}

function showFieldError(fieldName, message) {
  const input = document.getElementById(fieldName);

  const errorElement = document.getElementById(`${fieldName}-error`);

  if (!input || !errorElement) {
    return;
  }

  input.classList.add("is-invalid");
  errorElement.textContent = message;
}

function clearFieldError(fieldName) {
  const input = document.getElementById(fieldName);

  const errorElement = document.getElementById(`${fieldName}-error`);

  if (!input || !errorElement) {
    return;
  }

  input.classList.remove("is-invalid");
  errorElement.textContent = "";
}

function clearAllFieldErrors() {
  fieldNames.forEach(clearFieldError);
}

function focusFirstInvalidField(errors) {
  const firstFieldName = Object.keys(errors)[0];

  if (!firstFieldName) {
    return;
  }

  const input = document.getElementById(firstFieldName);

  input?.focus();
}

function showMessage(type, message) {
  messageElement.className = `form-message is-visible ${type}`;

  messageElement.textContent = message;
}

function clearMessage() {
  messageElement.className = "form-message";
  messageElement.textContent = "";
}

function setSubmitting(isSubmitting) {
  registerButton.disabled = isSubmitting;

  registerButton.textContent = isSubmitting ? "Đang đăng ký..." : "Đăng ký";
}

function normalizeText(value) {
  return value.trim().replace(/\s+/g, " ");
}

function isValidEmail(email) {
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  return emailPattern.test(email);
}

function getToday() {
  const today = new Date();

  const year = today.getFullYear();

  const month = String(today.getMonth() + 1).padStart(2, "0");

  const day = String(today.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}
