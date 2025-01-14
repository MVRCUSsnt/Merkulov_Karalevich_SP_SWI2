import React, { useState } from "react";
import "./AuthForm.css";

const AuthForm = ({ type, onSubmit }) => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit({ email, password });
    };

    return (
        <div className="auth-form-container">
            <h2>{type === "login" ? "Войти" : "Регистрация"}</h2>
            <form onSubmit={handleSubmit}>
                <input
                    type="email"
                    placeholder="Введите email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />
                <input
                    type="password"
                    placeholder="Введите пароль"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />
                <button type="submit">{type === "login" ? "Войти" : "Регистрация"}</button>
            </form>
        </div>
    );
};

export default AuthForm;
