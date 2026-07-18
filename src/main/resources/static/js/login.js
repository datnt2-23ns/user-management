import { apiRequest } from "/js/api.js";

import {
    isAuthenticated,
    saveAuthSession
} from "/js/auth.js";

const form =
    document.getElementById("login-form");

const emailInput =
    document.getElementById("email");

const passwordInput =
    document.getElementById("password");

const loginButton =
    document.getElementById("login-button");

const togglePasswordButton =
    document.getElementById("toggle-password");

const messageElement =
    document.getElementById("form-message");

initialize();

function initialize() {
    if (isAuthenticated()) {
        window.location.replace("/index.html");
        return;
    }

    emailInput.addEventListener(
        "input",
        () => {
            clearFieldError("email");
            clearMessage();
        }
    );

    passwordInput.addEventListener(
        "input",
        () => {
            clearFieldError("password");
            clearMessage();
        }
    );

    togglePasswordButton.addEventListener(
        "click",
        togglePasswordVisibility
    );

    form.addEventListener(
        "submit",
        handleSubmit
    );
}

async function handleSubmit(event) {
    event.preventDefault();

    clearAllErrors();
    clearMessage();

    const payload = {
        email: emailInput.value
            .trim()
            .toLowerCase(),
        password: passwordInput.value
    };

    const validationErrors =
        validate(payload);

    if (
        Object.keys(validationErrors).length > 0
    ) {
        showFieldErrors(validationErrors);

        showMessage(
            "error",
            "Vui lòng kiểm tra lại email và mật khẩu."
        );

        focusFirstInvalidField(
            validationErrors
        );

        return;
    }

    setSubmitting(true);

    try {
        const response = await apiRequest(
            "/auth/login",
            {
                method: "POST",
                body: JSON.stringify(payload)
            }
        );

        saveAuthSession(response);

        showMessage(
            "success",
            `Đăng nhập thành công. Xin chào ${
                response.user?.fullName
                || response.user?.email
                || "bạn"
            }.`
        );

        window.setTimeout(() => {
            window.location.replace(
                "/index.html"
            );
        }, 700);
    } catch (error) {
        handleLoginError(error);
    } finally {
        setSubmitting(false);
    }
}

function validate(payload) {
    const errors = {};

    if (!payload.email) {
        errors.email =
            "Email không được để trống.";
    } else if (!isValidEmail(payload.email)) {
        errors.email =
            "Email không đúng định dạng.";
    } else if (payload.email.length > 320) {
        errors.email =
            "Email không được vượt quá 320 ký tự.";
    }

    if (!payload.password) {
        errors.password =
            "Mật khẩu không được để trống.";
    } else if (payload.password.length < 8) {
        errors.password =
            "Mật khẩu phải có ít nhất 8 ký tự.";
    } else if (payload.password.length > 64) {
        errors.password =
            "Mật khẩu không được vượt quá 64 ký tự.";
    }

    return errors;
}

function handleLoginError(error) {
    if (error.status === 400) {
        const backendErrors =
            error.data?.errors || {};

        showFieldErrors(backendErrors);

        showMessage(
            "error",
            error.data?.message
            || "Dữ liệu đăng nhập không hợp lệ."
        );

        focusFirstInvalidField(
            backendErrors
        );

        return;
    }

    if (error.status === 401) {
        showMessage(
            "error",
            error.data?.message
            || "Email hoặc mật khẩu không đúng."
        );

        passwordInput.focus();
        passwordInput.select();
        return;
    }

    if (error.status === 403) {
        showMessage(
            "error",
            error.data?.message
            || "Tài khoản đã bị khóa hoặc không còn hoạt động."
        );

        return;
    }

    if (error.status === 500) {
        showMessage(
            "error",
            error.data?.message
            || "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."
        );

        return;
    }

    if (!error.status) {
        showMessage(
            "error",
            "Không thể kết nối đến máy chủ. Hãy kiểm tra Backend đang chạy."
        );

        return;
    }

    showMessage(
        "error",
        "Đăng nhập thất bại. Vui lòng thử lại."
    );
}

function togglePasswordVisibility() {
    const isHidden =
        passwordInput.type === "password";

    passwordInput.type =
        isHidden ? "text" : "password";

    togglePasswordButton.textContent =
        isHidden ? "Ẩn" : "Hiện";

    togglePasswordButton.setAttribute(
        "aria-label",
        isHidden
            ? "Ẩn mật khẩu"
            : "Hiển thị mật khẩu"
    );

    passwordInput.focus();
}

function showFieldErrors(errors) {
    Object.entries(errors).forEach(
        ([fieldName, message]) => {
            showFieldError(
                fieldName,
                message
            );
        }
    );
}

function showFieldError(
    fieldName,
    message
) {
    const input =
        document.getElementById(fieldName);

    const errorElement =
        document.getElementById(
            `${fieldName}-error`
        );

    if (!input || !errorElement) {
        return;
    }

    input.classList.add("is-invalid");
    errorElement.textContent = message;
}

function clearFieldError(fieldName) {
    const input =
        document.getElementById(fieldName);

    const errorElement =
        document.getElementById(
            `${fieldName}-error`
        );

    if (!input || !errorElement) {
        return;
    }

    input.classList.remove("is-invalid");
    errorElement.textContent = "";
}

function clearAllErrors() {
    clearFieldError("email");
    clearFieldError("password");
}

function focusFirstInvalidField(errors) {
    const firstFieldName =
        Object.keys(errors)[0];

    document
        .getElementById(firstFieldName)
        ?.focus();
}

function showMessage(type, message) {
    messageElement.className =
        `form-message is-visible ${type}`;

    messageElement.textContent = message;
}

function clearMessage() {
    messageElement.className =
        "form-message";

    messageElement.textContent = "";
}

function setSubmitting(isSubmitting) {
    loginButton.disabled = isSubmitting;

    loginButton.textContent = isSubmitting
        ? "Đang đăng nhập..."
        : "Đăng nhập";
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        .test(email);
}