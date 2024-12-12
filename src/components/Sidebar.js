import React, { useState } from "react";
import "./Sidebar.css";

const Sidebar = ({ activeChat, onChatChange }) => {
    const chats = ["Main Room", "Chat 1", "Chat 2", "Chat 3"];
    const [profileOpen, setProfileOpen] = useState(false);

    const toggleProfile = () => {
        setProfileOpen(!profileOpen);
    };

    return (
        <div className="sidebar">
            <div className="sidebar-header">
                <h1>Messenger</h1>
            </div>
            <div className="chat-list">
                {chats.map((chat) => (
                    <div
                        key={chat}
                        className={`chat-item ${activeChat === chat ? "active" : ""}`}
                        onClick={() => onChatChange(chat)}
                    >
                        {chat}
                    </div>
                ))}
            </div>
            <div className="sidebar-footer">
                {!profileOpen && (
                    <div className="profile-container" onClick={toggleProfile}>
                        <img
                            src="https://via.placeholder.com/40"
                            alt="Avatar"
                            className="profile-avatar"
                        />
                        <span className="profile-name">Your Name</span>
                    </div>
                )}
            </div>
            {profileOpen && (
                <div className="profile-modal">
                    <div className="profile-details">
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
                        <button>Log Out</button>
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
