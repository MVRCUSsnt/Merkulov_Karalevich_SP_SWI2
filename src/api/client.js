import { clearSession } from "../utils/session";

let unauthorizedHandler = null;

export const setUnauthorizedHandler = (handler) => {
    unauthorizedHandler = handler;
};

export const getApiBaseUrl = () =>
    window.__APP_CONFIG__?.API_URL || process.env.REACT_APP_API_URL || "http://localhost:8080";

export const getWsUrl = () =>
    window.__APP_CONFIG__?.WS_URL || process.env.REACT_APP_WS_URL || "http://localhost:8080/ws";

const buildUrl = (path) => {
    if (!path) return getApiBaseUrl();
    if (path.startsWith("http://") || path.startsWith("https://")) return path;
    return new URL(path, getApiBaseUrl()).toString();
};

export const apiFetch = async (path, options = {}, { parse = "auto" } = {}) => {
    const headers = new Headers(options.headers || {});
    const hasBody = options.body !== undefined && options.body !== null;
    const isFormData = hasBody && options.body instanceof FormData;

    if (hasBody && !isFormData && !headers.has("Content-Type")) {
        headers.set("Content-Type", "application/json");
    }

    const response = await fetch(buildUrl(path), {
        credentials: "include",
        ...options,
        headers,
    });

    if (!response.ok) {
        if (response.status === 401 || response.status === 403) {
            clearSession();
            if (unauthorizedHandler) unauthorizedHandler(response.status);
        }

        const errorBody = await response.text();
        const error = new Error(errorBody || `Request failed: ${response.status}`);
        error.status = response.status;
        throw error;
    }

    if (parse === "none") return response;
    if (parse === "text") return response.text();
    if (parse === "json") return response.json();

    const contentType = response.headers.get("content-type") || "";
    return contentType.includes("application/json") ? response.json() : response.text();
};
