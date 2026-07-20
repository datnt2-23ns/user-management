import {
    apiRequest
} from "/js/api.js";

import {
    clearAuthSession,
    isAuthenticated
} from "/js/auth.js";

const REQUEST_TIMEOUT_MS =
    3000;

const form =
    document.getElementById(
        "change-password-form"
    );

const currentPasswordInput =
    document.getElementById(
        "current-password"
    );

const newPasswordInput =
    document.getElementById(
        "new-password"
    );

const confirmPasswordInput =
    document.getElementById(
        "confirm-password"
    );

const messageElement =
    document.getElementById(
        "change-password-message"
    );

const submitButton =
    document.getElementById(
        "change-password-submit"
    );

const passwordToggleButtons =
    document.querySelectorAll(
        "[data-password-toggle]"
    );

initializePage();

function initializePage() {
    if (!isAuthenticated()) {
        redirectToLogin();
        return;
    }

    validateRequiredElements();

    form.addEventListener(
        "submit",
        handleSubmit
    );

    registerPasswordToggleButtons();
}

function validateRequiredElements() {
    const requiredElements = {
        form,
        currentPasswordInput,
        newPasswordInput,
        confirmPasswordInput,
        messageElement,
        submitButton
    };

    for (
        const [elementName, element]
        of Object.entries(requiredElements)
    ) {
        if (!element) {
            throw new Error(
                `Không tìm thấy phần tử giao diện: ${elementName}`
            );
        }
    }
}

async function handleSubmit(event) {
    event.preventDefault();

    clearFieldErrors();
    hideMessage();

    const requestData =
        readFormData();

    const validationErrors =
        validateFormData(
            requestData
        );

    if (
        Object.keys(validationErrors)
            .length > 0
    ) {
        renderFieldErrors(
            validationErrors
        );

        showMessage(
            "Vui lòng kiểm tra lại thông tin đã nhập.",
            "error"
        );

        return;
    }

    setButtonLoading(true);

    try {
        await apiRequest(
            "/users/me/password",
            {
                method: "PUT",
                body: requestData,
                timeoutMs:
                    REQUEST_TIMEOUT_MS
            }
        );

        form.reset();

        showMessage(
            "Đổi mật khẩu thành công. Bạn sẽ được chuyển đến trang đăng nhập.",
            "success"
        );

        window.setTimeout(
            () => {
                clearAuthSession();
                redirectToLogin();
            },
            1500
        );
    } catch (error) {
        if (
            error.status === 400
            && error.data?.errors
        ) {
            renderFieldErrors(
                error.data.errors
            );
        }

        handleApiError(error);
    } finally {
        setButtonLoading(false);
    }
}

function readFormData() {
    return {
        currentPassword:
            currentPasswordInput.value,

        newPassword:
            newPasswordInput.value,

        confirmPassword:
            confirmPasswordInput.value
    };
}

function validateFormData(data) {
    const errors = {};

    if (!data.currentPassword) {
        errors.currentPassword =
            "Mật khẩu hiện tại không được để trống.";
    }

    if (!data.newPassword) {
        errors.newPassword =
            "Mật khẩu mới không được để trống.";
    } else if (
        data.newPassword.length < 8
    ) {
        errors.newPassword =
            "Mật khẩu mới phải có ít nhất 8 ký tự.";
    } else if (
        data.newPassword.length > 100
    ) {
        errors.newPassword =
            "Mật khẩu mới không được vượt quá 100 ký tự.";
    }

    if (!data.confirmPassword) {
        errors.confirmPassword =
            "Xác nhận mật khẩu không được để trống.";
    } else if (
        data.confirmPassword
        !== data.newPassword
    ) {
        errors.confirmPassword =
            "Xác nhận mật khẩu không khớp.";
    }

    return errors;
}

function renderFieldErrors(errors) {
    for (
        const [fieldName, message]
        of Object.entries(errors)
    ) {
        const errorElement =
            document.querySelector(
                `[data-field-error="${fieldName}"]`
            );

        const inputElement =
            document.querySelector(
                `[name="${fieldName}"]`
            );

        if (errorElement) {
            errorElement.textContent =
                message;
        }

        if (inputElement) {
            inputElement.classList.add(
                "input-invalid"
            );
        }
    }
}

function clearFieldErrors() {
    document
        .querySelectorAll(
            "[data-field-error]"
        )
        .forEach(element => {
            element.textContent =
                "";
        });

    document
        .querySelectorAll(
            ".input-invalid"
        )
        .forEach(element => {
            element.classList.remove(
                "input-invalid"
            );
        });
}

function handleApiError(error) {
    console.error(
        "Change password error:",
        error
    );

    if (error.code === "TIMEOUT") {
        showMessage(
            "Máy chủ phản hồi quá lâu. Vui lòng thử lại.",
            "error"
        );

        return;
    }

    if (
        error.code === "NETWORK_ERROR"
    ) {
        showMessage(
            "Không thể kết nối đến máy chủ. Hãy kiểm tra Backend đang chạy.",
            "error"
        );

        return;
    }

    if (error.status === 400) {
        showMessage(
            error.data?.message
            || "Thông tin đổi mật khẩu không hợp lệ.",
            "error"
        );

        return;
    }

    if (error.status === 401) {
        clearAuthSession();

        showMessage(
            error.data?.message
            || "Phiên đăng nhập không hợp lệ hoặc đã hết hạn.",
            "error"
        );

        window.setTimeout(
            redirectToLogin,
            1500
        );

        return;
    }

    if (error.status === 403) {
        clearAuthSession();

        showMessage(
            error.data?.message
            || "Tài khoản không có quyền thực hiện chức năng này.",
            "error"
        );

        window.setTimeout(
            redirectToLogin,
            1500
        );

        return;
    }

    if (error.status === 404) {
        showMessage(
            error.data?.message
            || "Không tìm thấy tài khoản đang đăng nhập.",
            "error"
        );

        return;
    }

    if (error.status === 500) {
        showMessage(
            error.data?.message
            || "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.",
            "error"
        );

        return;
    }

    showMessage(
        error.message
        || "Không thể đổi mật khẩu.",
        "error"
    );
}

function showMessage(
    message,
    type
) {
    messageElement.textContent =
        message;

    messageElement.classList.remove(
        "success",
        "error"
    );

    messageElement.classList.add(
        type
    );

    messageElement.hidden =
        false;
}

function hideMessage() {
    messageElement.textContent =
        "";

    messageElement.hidden =
        true;

    messageElement.classList.remove(
        "success",
        "error"
    );
}

function registerPasswordToggleButtons() {
    passwordToggleButtons.forEach(button => {
        button.addEventListener(
            "click",
            () => togglePasswordVisibility(button)
        );
    });
}

function togglePasswordVisibility(button) {
    const targetId =
        button.dataset.target;

    const passwordInput =
        document.getElementById(
            targetId
        );

    if (!passwordInput) {
        console.error(
            `Không tìm thấy ô mật khẩu: ${targetId}`
        );

        return;
    }

    const isPasswordHidden =
        passwordInput.type === "password";

    passwordInput.type =
        isPasswordHidden
            ? "text"
            : "password";

    button.textContent =
        isPasswordHidden
            ? "Ẩn"
            : "Hiện";

    button.setAttribute(
        "aria-pressed",
        String(isPasswordHidden)
    );

    button.setAttribute(
        "aria-label",
        isPasswordHidden
            ? "Ẩn mật khẩu"
            : "Hiện mật khẩu"
    );
}

function setButtonLoading(loading) {
    submitButton.disabled =
        loading;

    submitButton.textContent =
        loading
        ? "Đang đổi mật khẩu..."
        : "Đổi mật khẩu";
}

function redirectToLogin() {
    window.location.replace(
        "/pages/login.html"
    );
}