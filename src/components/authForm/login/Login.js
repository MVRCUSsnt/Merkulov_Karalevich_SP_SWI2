import React, { useState } from "react";
import "./Login.css";
import { apiFetch } from "../../../api/client";
import { useNotify } from "../../common/NotificationContext";

const Login = ({ onSubmit, onClose, onSwitch }) => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const { notify } = useNotify();

    const handleSubmit = (e) => {
        e.preventDefault();

        apiFetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password }),
        }, { parse: "json" })
            .then((data) => {
                localStorage.setItem("user", data.username);
                localStorage.setItem("userId", data.id);
                localStorage.setItem("email", data.email || "");
                localStorage.setItem("avatarUrl", data.avatarUrl || "/default-avatar.webp");
                onSubmit(data);
            })
            .catch(() => notify("Login failed. Please check your username and password.", "error"));
    };
    return (
        <div className="auth-form-container">
            <h2 className="auth-form-title">Login</h2>
            <form className="auth-form" onSubmit={handleSubmit}>
                <input
                    type="text"
                    placeholder="Username"
                    className="auth-input"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />
                <input
                    type="password"
                    placeholder="Password"
                    className="auth-input"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />
                <button type="submit" className="full-width-button">Login</button>
            </form>
            <div className="auth-buttons-container">
                <button type="button" className="half-width-button switch-button" onClick={onSwitch}>Go to Register</button>
                <button type="button" className="half-width-button close-button" onClick={onClose}>Close</button>
            </div>
        </div>
    );
};

export default Login;
