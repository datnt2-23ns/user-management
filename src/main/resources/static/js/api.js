import {
    clearAuthSession,
    getAccessToken
} from "/js/auth.js";

const API_BASE_URL = "/api";

export async function apiRequest(
    endpoint,
    options = {}
) {
    const token = getAccessToken();

    const isFormData =
        options.body instanceof FormData;

    const headers = {
        ...(options.headers || {})
    };

    if (!isFormData) {
        headers["Content-Type"] =
            "application/json";
    }

    if (token) {
        headers.Authorization =
            `Bearer ${token}`;
    }

    let response;

    try {
        response = await fetch(
            `${API_BASE_URL}${endpoint}`,
            {
                ...options,
                headers
            }
        );
    } catch (error) {
        const networkError = new Error(
            "Không thể kết nối đến máy chủ"
        );

        networkError.cause = error;
        throw networkError;
    }

    const contentType =
        response.headers.get("content-type")
        || "";

    let responseBody = null;

    if (response.status !== 204) {
        if (
            contentType.includes(
                "application/json"
            )
        ) {
            responseBody =
                await response.json();
        } else {
            responseBody =
                await response.text();
        }
    }

    if (!response.ok) {
        if (
            response.status === 401
            && endpoint !== "/auth/login"
        ) {
            clearAuthSession();
        }

        const apiError = new Error(
            responseBody?.message
            || "API request failed"
        );

        apiError.status = response.status;
        apiError.data = responseBody;

        throw apiError;
    }

    return responseBody;
}