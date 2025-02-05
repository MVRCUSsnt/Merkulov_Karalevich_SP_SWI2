import React from "react";
import "./ChatList.css";

const ChatList = ({
                      personalChats = [],
                      groupChats = [],
                      activeChatId,
                      onSelectChat,
                      isPersonal,
                      setIsPersonal
                  }) => {
    return (
        <div className="chat-list-container">
            <div className="chat-toggle-buttons">
                <button className={isPersonal ? "active" : ""} onClick={() => setIsPersonal(true)}>
                    Личные сообщения
                </button>
                <button className={!isPersonal ? "active" : ""} onClick={() => setIsPersonal(false)}>
                    Группы
                </button>
            </div>

            <ul className="chat-list">
                {(isPersonal ? personalChats : groupChats).map((chat) => (
                    <li
                        key={chat.id}
                        className={chat.id === activeChatId ? "active" : ""}
                        onClick={() => onSelectChat(chat.id)}
                    >
                        <div className="chat-item">
                            <span className="chat-name">{chat.name}</span>
                            <p className="chat-description">{chat.description}</p>
                        </div>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default ChatList;
