import React from "react";
import "./ProfileMenu.css";

const ProfileMenu = ({
                         username,
                         avatar,
                         menuOpen,
                         setMenuOpen,
                         onLogout,
                         theme,
                         toggleTheme,
                     }) => {
    if (!menuOpen) return null;

    return (
        <div className="modal-overlay" onClick={() => setMenuOpen(false)}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <img src={avatar} alt="Avatar" className="avatar" />
                <p className="username">{username}</p>
                <button onClick={toggleTheme}>
                    Текущая тема: {theme === "dark" ? "Темная" : "Светлая"}
                </button>
                <button onClick={onLogout}>Выйти</button>
            </div>
        </div>
    );
};

export default ProfileMenu;
