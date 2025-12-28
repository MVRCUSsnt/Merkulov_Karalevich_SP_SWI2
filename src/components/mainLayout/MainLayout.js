import React, { useState, useEffect, useRef } from "react";
import Sidebar from "./sidebar/Sidebar";
import ChatContainer from "./chatContainer/ChatContainer";
import WebSocketService from "./chatContainer/WebSocketService";
import { useNotify } from "../common/NotificationContext";
import "./MainLayout.css"; // CSS для макета

const defaultChat = { id: 1, name: "Main Room", description: "Main Room", type: "group" };

const MainLayout = () => {
    const [activeChat, setActiveChat] = useState(defaultChat);
    const [roomNotifications, setRoomNotifications] = useState({});
    const activeChatRef = useRef(activeChat);
    const { notify } = useNotify();

    useEffect(() => {
        activeChatRef.current = activeChat;
        if (activeChat?.type !== "group") return;
        setRoomNotifications((prev) => {
            if (!prev[activeChat.id]) return prev;
            const next = { ...prev };
            delete next[activeChat.id];
            return next;
        });
    }, [activeChat]);

    useEffect(() => {
        WebSocketService.connect(() => {
            WebSocketService.subscribeToRoomNotifications((notification) => {
                if (!notification?.roomId) return;
                const currentActive = activeChatRef.current;
                if (currentActive?.type === "group" && currentActive.id === notification.roomId) {
                    return;
                }

                setRoomNotifications((prev) => ({
                    ...prev,
                    [notification.roomId]: (prev[notification.roomId] || 0) + 1,
                }));
                notify(
                    `New message in room ${notification.roomId} from ${notification.senderUsername}: ${notification.content}`,
                    "success"
                );
            });
        });

        return () => {
            WebSocketService.unsubscribeFromRoomNotifications();
        };
    }, [notify]);

    return (
        <div className="main-layout">
            <Sidebar
                activeChat={activeChat}
                onSelectChat={setActiveChat}
                defaultChat={defaultChat}
                roomNotifications={roomNotifications}
            />
            <ChatContainer activeChat={activeChat} />
        </div>
    );
};

export default MainLayout;
