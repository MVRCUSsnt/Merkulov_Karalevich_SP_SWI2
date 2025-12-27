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
    const { notify } = useNotify();

    useEffect(() => {
        const handleStorageChange = () => {
            setUser({
                username: localStorage.getItem("user") || "Guest",
                avatarUrl: localStorage.getItem("avatarUrl") || "/default-avatar.webp",
                email: localStorage.getItem("email") || "No email available"
            });
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

    return (
        <div className="user-profile">
            <button className="close-btn" onClick={onClose}>x</button>
            <div className="profile-header">
                <img src={user.avatarUrl} alt="User Avatar" className="profile-avatar" />
                <h2>{user.username}</h2>
                <p>{user.email}</p>
            </div>
            <div className="profile-actions">
                <button className="profile-btn">Settings</button>
                <button className="logout-btn" onClick={handleLogout}>Logout</button>
            </div>
        </div>
    );
};

export default UserProfile;