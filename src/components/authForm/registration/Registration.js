import React, { useState } from "react";
import "./Registration.css";
import { apiFetch } from "../../../api/client";
import { useNotify } from "../../common/NotificationContext";

const Registration = ({ onSubmit, onClose, onSwitch }) => {
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const { notify } = useNotify();

    const handleSubmit = (e) => {
        e.preventDefault();

        apiFetch("/api/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, email, password }),
        }, { parse: "json" })
            .then((data) => {
                localStorage.setItem("user", data.username);
                localStorage.setItem("userId", data.id);
                onSubmit(data);
            })
            .catch(() => notify("Register failed! Please try again", "error"));

    };

    return (
        <div className="auth-form-container auth-form-large">
            <h2 className="auth-form-title">Register</h2>
            <form className="auth-form" onSubmit={handleSubmit}>
                <input
                    type="text"
                    placeholder="Username"
                    className="auth-input auth-input-large"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />
                <input
                    type="email"
                    placeholder="Email"
                    className="auth-input auth-input-large"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />
                <input
                    type="password"
                    placeholder="Password"
                    className="auth-input auth-input-large"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />
                <button type="submit" className="full-width-button">Register</button>
            </form>
            <div className="auth-buttons-container">
                <button type="button" className="half-width-button switch-button" onClick={onSwitch}>Go to Login</button>
                <button type="button" className="half-width-button close-button" onClick={onClose}>Close</button>
            </div>
        </div>
    );
};

export default Registration;
