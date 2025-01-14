import React, { useState } from "react";
import "./RegestForm.css";

const RegestForm = ({ type, onSubmit }) => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [errorMessage, setErrorMessage] = useState("");  // Для отображения ошибок
    const [successMessage, setSuccessMessage] = useState("");  // Для успешного сообщения

    const handleSubmit = (e) => {
        e.preventDefault();

        // Отправка POST запроса на сервер
        fetch("http://localhost:8081/api/auth/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ username: email, password: password }),
        })
            .then((response) => response.json())
            .then((data) => {
                if (data.message) {
                    setSuccessMessage(data.message);  // Сообщение об успешной регистрации
                    setErrorMessage("");  // Очищаем ошибки
                    setEmail("");
                    setPassword("");
                }
            })
            .catch((error) => {
                setErrorMessage("Ошибка регистрации. Попробуйте еще раз.");  // Обработка ошибок
                setSuccessMessage("");  // Очищаем успешное сообщение
            });
    };

    return (
        <div className="auth-form-container">
            <h2>Регистрация</h2>
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
                <button type="submit">Регистрация</button>
            </form>

            {/* Отображаем сообщения об ошибке или успехе */}
            {successMessage && <div className="success-message">{successMessage}</div>}
            {errorMessage && <div className="error-message">{errorMessage}</div>}
        </div>
    );
};

export default RegestForm;
