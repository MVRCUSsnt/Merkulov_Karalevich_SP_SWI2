import React, { useState } from "react";
import "./UserProfile.css";

const UserProfile = ({ onLogout, username }) => {
    const [menuOpen, setMenuOpen] = useState(false);
    const [confirmLogout, setConfirmLogout] = useState(false);
    const [settingsOpen, setSettingsOpen] = useState(false);

    const toggleMenu = () => {
        setMenuOpen(!menuOpen);
    };

    const closeMenu = () => {
        setMenuOpen(false);
        setConfirmLogout(false);
        setSettingsOpen(false);
    };

    const openConfirmLogout = () => {
        setConfirmLogout(true);
        setMenuOpen(false);
    };

    const openSettings = () => {
        setSettingsOpen(true);
        setMenuOpen(false);
    };

    const handleLogout = () => {
        onLogout();
        closeMenu();
    };

    return (
        <div>
            <div className="user-profile">
                <img
                    src="https://via.placeholder.com/50"
                    alt="Avatar"
                    className="avatar"
                    onClick={toggleMenu}
                />
                <div className="username">{username}</div>
            </div>

            {menuOpen && (
                <div className="modal-overlay" onClick={closeMenu}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="menu-item" onClick={openSettings}>
                            Настройки
                        </div>
                        <div className="menu-item" onClick={openConfirmLogout}>
                            Выйти
                        </div>
                    </div>
                </div>
            )}

            {confirmLogout && (
                <div className="modal-overlay" onClick={closeMenu}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <p>Вы действительно хотите выйти?</p>
                        <div className="modal-buttons">
                            <button className="modal-button" onClick={handleLogout}>
                                Да
                            </button>
                            <button className="modal-button" onClick={closeMenu}>
                                Нет
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {settingsOpen && (
                <div className="modal-overlay" onClick={closeMenu}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <h2>Настройки</h2>
                        <p>Здесь можно добавить опции настройки.</p>
                        <button className="modal-button" onClick={closeMenu}>
                            Закрыть
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UserProfile;
