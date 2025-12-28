import React, { useState, useEffect, useCallback } from "react";
import "./Sidebar.css";
import Login from "../../authForm/login/Login";
import Registration from "../../authForm/registration/Registration";
import UserProfile from "./userProfile/UserProfile";
import ChatList from "./сhatList/ChatList";
import { apiFetch } from "../../../api/client";
import { useNotify } from "../../common/NotificationContext";
import { clearSession } from "../../../utils/session";

const Sidebar = ({ activeChat, onSelectChat, defaultChat, roomNotifications }) => {
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);
    const [isProfileOpen, setIsProfileOpen] = useState(false);
    const [formType, setFormType] = useState(null);
    const [privateChats, setPrivateChats] = useState([]);    const [groupChats, setGroupChats] = useState([{ id: 1, name: "Main Room", description: "Main Room", type: "group" }]);
    const [isGroupsLoaded, setIsGroupsLoaded] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [isPersonal, setIsPersonal] = useState(true);
    const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem("user"));
    const { notify } = useNotify();

    const fetchKafkaQueue = async () => {
        try {
            const data = await apiFetch("/api/queue/main?limit=50", { method: "GET" }, { parse: "json" });

            if (!Array.isArray(data) || data.length === 0) {
                // Uncomment the line below if you want an alert even when empty
                // alert("Kafka queue: no new messages");
                return;
            }

            alert("Kafka queue messages:\n\n" + data.join("\n"));
        } catch (e) {
            console.warn("Kafka queue fetch failed (service might be down)");
        }
    };

    const fetchPrivateChats = useCallback(() => {
        if (!isLoggedIn) return;

        apiFetch("/api/private-messages/conversations", { method: "GET" }, { parse: "json" })
            .then(data => {
                console.log("Loaded conversations:", data);
                const chats = Array.isArray(data) ? data : [];
                setPrivateChats(
                    chats.map((user) => ({
                        id: user.id,
                        recipientId: user.id,
                        recipientUsername: user.username,
                        name: user.username,
                        avatarUrl: user.avatarUrl || "/default-avatar.webp",
                        type: "private",
                    }))
                );
            })
            .catch(err => console.error("Failed to load private chats", err));
    }, [isLoggedIn]);

    useEffect(() => {
        const storedUser = localStorage.getItem("user");
        const storedUserId = localStorage.getItem("userId");

        if (!storedUser || storedUser === "null" || storedUserId === "null") {
            localStorage.removeItem("user");
            localStorage.removeItem("userId");
            localStorage.removeItem("email");
            localStorage.removeItem("avatarUrl");
            clearSession();
            setIsProfileOpen(false);
            setIsLoggedIn(false);
        } else {
            fetchGroupChats();
            fetchPrivateChats();

            // Call Kafka check slightly after load
            setTimeout(() => {
                fetchKafkaQueue();
            }, 300);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [fetchPrivateChats]);


    const fetchGroupChats = () => {
        if (isGroupsLoaded) return;

        setLoading(true);
        apiFetch("/api/rooms/my-rooms?page=0&size=10", { method: "GET" }, { parse: "json" })
            .then(data => {
                const rooms = Array.isArray(data) ? data : [];

                const uniqueChats = [...new Map(rooms.map(chat => [chat.id, chat])).values()];

                const updatedChats = uniqueChats.some(chat => chat.id === 1)
                    ? uniqueChats
            : [{ id: 1, name: "Main Room", description: "Main Room", type: "group" }, ...uniqueChats];

                setGroupChats(updatedChats.map((chat) => ({ ...chat, type: "group" })));
                setIsGroupsLoaded(true);
            })
            .catch(error => {
                setError(error.message);
                notify("Failed to load chat list.", "error");
            })
            .finally(() => setLoading(false));
    };

    const handleAddGroupChat = () => {
        const groupName = prompt("Enter new group name:");
        if (!groupName) return;

        const groupDescription = prompt("Enter new group description:");

        const newRoom = {
            name: groupName,
            description: groupDescription || "No description",
        };

        apiFetch("/api/rooms/create", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(newRoom),
    }, { parse: "json" })
    .then(createdRoom => {
                setGroupChats(prev => [...prev, { ...createdRoom, type: "group" }]);
            notify("Group chat created successfully.", "success");
        })
            .catch(error => notify(`Error creating chat: ${error.message}`, "error"));
    };

    const handleAddDirectChat = () => {
        const recipientUsername = prompt("Enter recipient username:");
        if (!recipientUsername) return;


        const newChat = {
            id: `dm-${recipientUsername}`,
            name: recipientUsername,
            type: "private",
            recipientUsername,
            recipientId: null,
            avatarUrl: "/default-avatar.webp",
        };

        setPrivateChats((prev) => {
            if (prev.some((chat) => chat.recipientUsername === newChat.recipientUsername)) return prev;
            return [...prev, newChat];
        });
        onSelectChat(newChat);
    };

    const handleLogout = async () => {
        try {
            await apiFetch("/api/auth/logout", { method: "POST" }, { parse: "none" });
        } catch (error) {
            console.error("Logout error:", error);
            notify("Failed to end session. Please try again.", "warning");
        }

        localStorage.removeItem("user");
        localStorage.removeItem("userId");
        localStorage.removeItem("email");
        localStorage.removeItem("avatarUrl");
        clearSession();
        setIsProfileOpen(false);
        setIsLoggedIn(false);

        setPrivateChats([]);
        setGroupChats([{ id: 1, name: "Main Room", description: "Main Room", type: "group" }]);
        setIsGroupsLoaded(false);

        onSelectChat(defaultChat);
    };

    const handleLogin = async (userData) => {
        localStorage.setItem("user", userData.username);
        localStorage.setItem("userId", userData.id);
        localStorage.setItem("email", userData.email || "");
        localStorage.setItem("avatarUrl", userData.avatarUrl || "/default-avatar.webp");

        setIsLoggedIn(true);
        setIsProfileOpen(false);
        setFormType(null);
        fetchGroupChats();
        fetchPrivateChats();

        // Check Kafka after login
        await fetchKafkaQueue();
    };

    return (
        <>
            <button className="toggle-sidebar" onClick={() => setIsSidebarOpen(!isSidebarOpen)}>☰</button>

            <div className={`sidebar ${isSidebarOpen ? "open" : ""}`}>
                <div
                    className="profile-container"
                    onClick={() => localStorage.getItem("user") ? setIsProfileOpen(!isProfileOpen) : setFormType("login")}
                >
                    <img
                        src={localStorage.getItem("avatarUrl") || "/default-avatar.webp"}
                        alt="Avatar"
                        className="profile-avatar"
                    />
                    <div className="profile-name">{localStorage.getItem("user") || "Login"}</div>
                </div>

                <ChatList
                personalChats={privateChats}
                groupChats={groupChats}
                activeChat={activeChat}
                    onSelectChat={onSelectChat}
                    isPersonal={isPersonal}
                    setIsPersonal={(value) => {
                        setIsPersonal(value);
                        if (!value) fetchGroupChats();
                    }}
                    onAddGroupChat={handleAddGroupChat}
                    onAddDirectChat={handleAddDirectChat}
                    roomNotifications={roomNotifications}
                />

                {loading && <p>Loading...</p>}
                {error && <p className="error">{error}</p>}
            </div>

            {isProfileOpen && localStorage.getItem("user") && (
                <UserProfile
                    onLogout={handleLogout}
                    onClose={() => setIsProfileOpen(false)}
                    username={localStorage.getItem("user")}
                />
            )}

            {formType && (
                <div className="auth-overlay">
                    {formType === "login" && (
                        <Login
                            onSubmit={handleLogin}
                            onClose={() => setFormType(null)}
                            onSwitch={() => setFormType("register")}
                        />
                    )}
                    {formType === "register" && (
                        <Registration
                            onSubmit={handleLogin}
                            onClose={() => setFormType(null)}
                            onSwitch={() => setFormType("login")}
                        />
                    )}
                </div>
            )}
        </>
    );
};

export default Sidebar;