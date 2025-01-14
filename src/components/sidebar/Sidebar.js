import React, { useState } from "react";
import "./Sidebar.css";
import AuthForm from "../authForm/AuthForm"; // Импортируем компонент формы авторизации
import RegestForm from "../regestForm/RegestForm"; // Импортируем компонент формы регистрации

const Sidebar = ({ onLogin, onRegister }) => {
    const [formType, setFormType] = useState(null); // Состояние для типа формы (login или register)

    const chats = [
        { name: "TETL1", isFavorite: true },
        { name: "AGI3", isFavorite: true },
        { name: "VBAP", isFavorite: false },
        { name: "PRCS", isFavorite: false },
        { name: "AKI2", isFavorite: false },
    ];

    const handleChatClick = (chat) => {
        console.log(`Chat selected: ${chat.name}`);
    };

    const handleLoginClick = () => {
        setFormType("login"); // Устанавливаем тип формы как "login"
    };

    const handleRegestClick = () => {
        setFormType("register"); // Устанавливаем тип формы как "register"
    };

    const handleCloseForm = () => {
        setFormType(null); // Закрываем форму
    };

    return (
        <div className="sidebar">
            <div className="chat-list">
                {chats.map((chat, index) => (
                    <div
                        key={index}
                        className="chat-item"
                        onClick={() => handleChatClick(chat)}
                    >
                        <div className="chat-avatar">
                            <img
                                src="#"
                                alt={chat.name}
                                className="avatar"
                            />
                        </div>
                        <div className="chat-name">{chat.name}</div>
                        {chat.isFavorite && <span className="favorite-star">★</span>}
                    </div>
                ))}
                <div className="more-chats">▼</div>
            </div>

            {/* Кнопки Войти и Регистрация */}
            <div className="login-actions">
                <button className="login-button" onClick={handleLoginClick}>
                    Войти
                </button>
                <button className="register-button" onClick={handleRegestClick}>
                    Регистрация
                </button>
            </div>

            {/* Отображаем форму в зависимости от formType */}
            {formType === "login" && (
                <div className="auth-form-overlay">
                    <AuthForm
                        type="login"
                        onSubmit={(data) => {
                            console.log(data); // Обработка данных из формы
                            setFormType(null); // Закрываем форму после отправки
                        }}
                    />
                    <button className="close-form" onClick={handleCloseForm}>Закрыть</button>
                </div>
            )}
            {formType === "register" && (
                <div className="auth-form-overlay">
                    <RegestForm
                        type="register"
                        onSubmit={(data) => {
                            console.log(data); // Обработка данных из формы
                            setFormType(null); // Закрываем форму после отправки
                        }}
                    />
                    <button className="close-form" onClick={handleCloseForm}>Закрыть</button>
                </div>
            )}
        </div>
    );
};

export default Sidebar;
