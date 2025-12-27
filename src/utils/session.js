export const clearSession = () => {
    localStorage.removeItem("user");
    localStorage.removeItem("userId");
    localStorage.removeItem("email");
    localStorage.removeItem("avatarUrl");
};