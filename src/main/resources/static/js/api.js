const API_BASE_URL = "/api";

export async function apiRequest(endpoint, options = {}) {
    const token = localStorage.getItem("accessToken");
    const isFormData = options.body instanceof FormData;

    const headers = {
        ...(options.headers || {})
    };

    if (!isFormData) {
        headers["Content-Type"] = "application/json";
    }

    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        ...options,
        headers
    });

    const contentType = response.headers.get("content-type") || "";

    let responseBody;

    if (contentType.includes("application/json")) {
        responseBody = await response.json();
    } else {
        responseBody = await response.text();
    }

    if (!response.ok) {
        const error = new Error("API request failed");
        error.status = response.status;
        error.data = responseBody;
        throw error;
    }

    return responseBody;
}