import React from "react";
import "./ChatList.css";

const ChatList = ({
                      personalChats = [],
                      groupChats = [],
                      activeChat,
                      onSelectChat,
                      isPersonal,
                      setIsPersonal,
                      onAddGroupChat,
                      onAddDirectChat,
                      roomNotifications = {}
                  }) => {

    const currentList = isPersonal ? personalChats : groupChats;

    return (
        <div className="chat-list-container">
            <div className="chat-toggle-buttons">
                <button className={isPersonal ? "active" : ""} onClick={() => setIsPersonal(true)}>
                    Direct Messages
                </button>
                <button className={!isPersonal ? "active" : ""} onClick={() => setIsPersonal(false)}>
                    Groups
                </button>
            </div>

            <div className="group-section">
                <ul className="chat-list">
                    {currentList.length === 0 && (
                        <li className="empty-chat-list">No active chats</li>
                    )}
                    {currentList.map((chat) => {
                        const chatName = chat?.name || "Untitled";
                        const notificationCount = roomNotifications[chat.id] || 0;
                        return (
                            <li
                                key={chat.id ?? chatName}
                                className={chat.id === activeChat?.id ? "active" : ""}
                                onClick={() => {
                                    if (chat?.id) onSelectChat(chat);
                                }}
                            >
                                <div className="chat-item">
                                    {isPersonal && (
                                        <img
                                            src={chat.avatarUrl || "/default-avatar.webp"}
                                            alt={`${chatName} avatar`}
                                            className="chat-avatar"
                                        />
                                    )}
                                    <span className="chat-name">{chatName}</span>
                                    {!isPersonal && notificationCount > 0 && (
                                        <span className="chat-notification-badge">{notificationCount}</span>
                                    )}
                                </div>
                            </li>
                        );
                    })}
                </ul>

                {isPersonal && (
                    <button className="add-group-chat" onClick={onAddDirectChat}>+</button>
                )}

                {!isPersonal && (
                    <button className="add-group-chat" onClick={onAddGroupChat}>+</button>
                )}
            </div>
        </div>
    );
};

export default ChatList;