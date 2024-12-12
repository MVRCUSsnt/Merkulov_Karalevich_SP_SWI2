import React, { useState } from "react";
import "./Sidebar.css";

const Sidebar = () => {
    const chats = [
        { name: "TETL1", isFavorite: true },
        { name: "AGI3", isFavorite: true },
        { name: "VBAP", isFavorite: false },
        { name: "PRCS", isFavorite: false },
        { name: "AKI2", isFavorite: false },
    ];

    const [profileOpen, setProfileOpen] = useState(false);

    const handleChatClick = (chat) => {
        console.log(`Chat selected: ${chat.name}`);
    };

    const toggleProfile = () => {
        setProfileOpen(!profileOpen);
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
                                src="https://via.placeholder.com/50"
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
            <div className="calendar">
                <div className="calendar-header">May</div>
                <div className="calendar-grid">
                    {Array.from({ length: 31 }, (_, i) => (
                        <div
                            key={i + 1}
                            className="calendar-date"
                            onClick={() => alert(`Заметка для ${i + 1}`)}
                        >
                            {i + 1}
                        </div>
                    ))}
                </div>
            </div>
            <div className="profile-container" onClick={toggleProfile}>
                <img
                    src="https://via.placeholder.com/50"
                    alt="User"
                    className="profile-avatar"
                />
                <div className="profile-details">
                    <p className="profile-name">Your Name</p>
                    <p className="profile-status">Online</p>
                </div>
            </div>
            {profileOpen && (
                <div className="profile-modal">
                    <div className="profile-details-modal">
                        <img
                            src="https://via.placeholder.com/80"
                            alt="Avatar"
                            className="profile-modal-avatar"
                        />
                        <h3>Your Name</h3>
                        <p>Email: your.email@example.com</p>
                    </div>
                    <div className="profile-actions">
                        <button>Настройки</button>
                        <button>Выйти</button>
                    </div>
                    <button className="close-modal" onClick={toggleProfile}>
                        Закрыть
                    </button>
                </div>
            )}
        </div>
    );
};

export default Sidebar;
