import { clearAuthSession, getAccessToken } from "/js/auth.js";

const API_BASE_URL = "/api";

export async function apiRequest(path, options = {}) {
  const {
    body,
    headers: customHeaders,
    timeoutMs = 8000,
    ...otherOptions
  } = options;

  const headers = new Headers(customHeaders || {});

  const accessToken = getAccessToken();

  if (accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  const preparedBody = prepareRequestBody(body, headers);

  const controller = new AbortController();

  const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs);

  let response;

  try {
    response = await fetch(buildApiUrl(path), {
      ...otherOptions,
      headers,
      body: preparedBody,
      signal: controller.signal,
    });
  } catch (networkError) {
    const error = new Error();

    error.status = null;
    error.data = null;

    if (networkError.name === "AbortError") {
      error.code = "TIMEOUT";
      error.message = "Máy chủ phản hồi quá lâu";
    } else {
      error.code = "NETWORK_ERROR";
      error.message = "Không thể kết nối đến máy chủ";
    }

    throw error;
  } finally {
    window.clearTimeout(timeoutId);
  }

  const responseData = await parseResponse(response);

  if (!response.ok) {
    if (response.status === 401 && path !== "/auth/login") {
      clearAuthSession();
    }

    const error = new Error(
      responseData?.message || "Yêu cầu không thành công",
    );

    error.status = response.status;
    error.data = responseData;

    throw error;
  }

  return responseData;
}

function prepareRequestBody(body, headers) {
  if (body === undefined || body === null) {
    return undefined;
  }

  if (body instanceof FormData) {
    headers.delete("Content-Type");
    return body;
  }

  if (body instanceof Blob) {
    return body;
  }

  if (typeof body === "string") {
    if (!headers.has("Content-Type")) {
      headers.set("Content-Type", "application/json");
    }

    return body;
  }

  if (!headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  return JSON.stringify(body);
}

async function parseResponse(response) {
  if (response.status === 204) {
    return null;
  }

  const text = await response.text();

  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function buildApiUrl(path) {
  if (path.startsWith("/")) {
    return API_BASE_URL + path;
  }

  return `${API_BASE_URL}/${path}`;
}
