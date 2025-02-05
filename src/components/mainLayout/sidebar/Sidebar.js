import React, { useState, useEffect } from "react";
import "./Sidebar.css";
import Login from "../../authForm/login/Login";
import Registration from "../../authForm/registration/Registration";
import UserProfile from "./userProfile/UserProfile";
import ChatList from "./сhatList/ChatList";

const Sidebar = () => {
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);
    const [isProfileOpen, setIsProfileOpen] = useState(false);
    const [formType, setFormType] = useState(null);
    const [personalChats, setPersonalChats] = useState([]);
    const [groupChats, setGroupChats] = useState([{ id: 1, name: "Main Room", description: "Основная комната" }]);
    const [isGroupsLoaded, setIsGroupsLoaded] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [isPersonal, setIsPersonal] = useState(true);

    useEffect(() => {
        const storedUser = localStorage.getItem("user");
        const storedUserId = localStorage.getItem("userId");

        if (!storedUser || storedUser === "null" || storedUserId === "null") {
            localStorage.removeItem("user");
            localStorage.removeItem("userId");
            localStorage.removeItem("avatarUrl");
            setIsProfileOpen(false);
        }
    }, []);

    const fetchGroupChats = () => {
        if (isGroupsLoaded) return; // Не загружать повторно, если уже загружены

        setLoading(true);
        fetch("http://localhost:8080/my-rooms?page=0&size=10", {
            method: "GET",
            credentials: "include", // ВАЖНО: позволяет браузеру отправлять куки с запросом
        })
            .then(response => {
                if (!response.ok) throw new Error("Ошибка загрузки чатов");
                return response.json();
            })
            .then(data => {
                setGroupChats(prev => [...prev, ...data]); // Добавляем к Main Room загруженные чаты
                setIsGroupsLoaded(true);
            })
            .catch(error => setError(error.message))
            .finally(() => setLoading(false));

    };

    const handleLogin = (userData) => {
        localStorage.setItem("user", userData.username);
        localStorage.setItem("userId", userData.id);
        localStorage.setItem("avatarUrl", userData.avatarUrl || "https://via.placeholder.com/50");
        setIsProfileOpen(true);
    };

    const handleLogout = () => {
        localStorage.removeItem("user");
        localStorage.removeItem("userId");
        localStorage.removeItem("avatarUrl");
        setIsProfileOpen(false);
    };

    return (
        <>
            <button className="toggle-sidebar" onClick={() => setIsSidebarOpen(!isSidebarOpen)}>☰</button>

            <div className={`sidebar ${isSidebarOpen ? "open" : ""}`}>
                <div className="profile-container" onClick={() => localStorage.getItem("user") ? setIsProfileOpen(!isProfileOpen) : setFormType("login")}>
                    <img src={localStorage.getItem("avatarUrl") || "https://via.placeholder.com/50"} alt="Avatar" className="profile-avatar" />
                    <div className="profile-name">{localStorage.getItem("user") || "Login"}</div>
                </div>

                <ChatList
                    personalChats={personalChats}
                    groupChats={groupChats}
                    activeChatId={null}
                    onSelectChat={() => {}}
                    isPersonal={isPersonal}
                    setIsPersonal={(value) => {
                        setIsPersonal(value);
                        if (!value) fetchGroupChats(); // Загружаем группы при переключении на вкладку "Группы"
                    }}
                />

                {loading && <p>Загрузка...</p>}
                {error && <p className="error">{error}</p>}
            </div>

            {isProfileOpen && localStorage.getItem("user") && (
                <UserProfile onLogout={handleLogout} onClose={() => setIsProfileOpen(false)} username={localStorage.getItem("user")} />
            )}

            {formType && (
                <div className="auth-overlay">
                    {formType === "login" && <Login onSubmit={handleLogin} onClose={() => setFormType(null)} onSwitch={() => setFormType("register")} />}
                    {formType === "register" && <Registration onSubmit={handleLogin} onClose={() => setFormType(null)} onSwitch={() => setFormType("login")} />}
                </div>
            )}
        </>
    );
};

export default Sidebar;
