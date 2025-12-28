import React, { useState, useEffect } from "react";
import "./UserProfile.css";
import WebSocketService from "../../chatContainer/WebSocketService";
import { apiFetch } from "../../../../api/client";
import { useNotify } from "../../../common/NotificationContext";
import { clearSession } from "../../../../utils/session";

const UserProfile = ({ onLogout, onClose }) => {
    const [user, setUser] = useState({
        username: localStorage.getItem("user") || "Guest",
        avatarUrl: localStorage.getItem("avatarUrl") || "/default-avatar.webp",
        email: localStorage.getItem("email") || "No email available"
    });
    const [avatarUrlInput, setAvatarUrlInput] = useState(user.avatarUrl);
    const { notify } = useNotify();

    const refreshProfile = async () => {
        const data = await apiFetch("/api/auth/me", { method: "GET" }, { parse: "json" });
        if (!data) return;
        localStorage.setItem("user", data.username || "Guest");
        localStorage.setItem("userId", data.id ?? "");
        localStorage.setItem("email", data.email || "");
        localStorage.setItem("avatarUrl", data.avatarUrl || "/default-avatar.webp");
        setUser({
            username: data.username || "Guest",
            avatarUrl: data.avatarUrl || "/default-avatar.webp",
            email: data.email || "No email available"
        });
        setAvatarUrlInput(data.avatarUrl || "/default-avatar.webp");
    };

    useEffect(() => {
        const handleStorageChange = () => {
            setUser({
                username: localStorage.getItem("user") || "Guest",
                avatarUrl: localStorage.getItem("avatarUrl") || "/default-avatar.webp",
                email: localStorage.getItem("email") || "No email available"
            });
            setAvatarUrlInput(localStorage.getItem("avatarUrl") || "/default-avatar.webp");
        };

        window.addEventListener("storage", handleStorageChange);
        return () => window.removeEventListener("storage", handleStorageChange);
    }, []);

    const handleLogout = async () => {
        try {
            await apiFetch("/api/auth/logout", { method: "POST" }, { parse: "none" });
        } catch (error) {
            notify("Failed to end session. Please try again.", "warning");
        }
        WebSocketService.disconnect();
        clearSession();
        setUser({ username: "Guest", avatarUrl: "/default-avatar.webp", email: "No email available" });
        onLogout();
    };

    const handleAvatarUpdate = async () => {
        if (!avatarUrlInput.trim()) {
            notify("Avatar URL cannot be empty.", "warning");
            return;
        }

        try {
            await apiFetch("/api/auth/avatar", {
                method: "PUT",
                body: JSON.stringify({ avatarUrl: avatarUrlInput.trim() })
            }, { parse: "none" });
            await refreshProfile();
            notify("Avatar updated successfully.", "success");
        } catch (error) {
            notify("Failed to update avatar.", "error");
        }
    };

    return (
        <div className="user-profile">
            <button className="close-btn" onClick={onClose}>x</button>
            <div className="profile-header">
                <img src={user.avatarUrl} alt="User Avatar" className="profile-avatar" />
                <h2>{user.username}</h2>
                <p>{user.email}</p>
            </div>
            <div className="profile-avatar-update">
                <label htmlFor="avatar-url-input">Update avatar URL</label>
                <input
                    id="avatar-url-input"
                    type="text"
                    value={avatarUrlInput}
                    onChange={(event) => setAvatarUrlInput(event.target.value)}
                    placeholder="https://example.com/avatar.png"
                />
                <button className="profile-btn" onClick={handleAvatarUpdate}>Save Avatar</button>
            </div>
            <div className="profile-actions">
                <button className="profile-btn">Settings</button>
                <button className="logout-btn" onClick={handleLogout}>Logout</button>
            </div>
        </div>
    );
};

export default UserProfile;