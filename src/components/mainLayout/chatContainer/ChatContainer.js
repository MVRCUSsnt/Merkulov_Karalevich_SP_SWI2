import React, { useEffect, useState, useCallback } from "react";
import "./ChatContainer.css";
import Message from "./message/Message"; // Исправленный импорт

const ChatContainer = ({ activeChatId = 1, onChangeChat, userId, isSidebarOpen }) => {
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState("");
    const [currentChatId, setCurrentChatId] = useState(userId ? activeChatId : 1);

    // ✅ Обернули fetchMessages в useCallback, чтобы избежать проблем с зависимостями useEffect
    const fetchMessages = useCallback(() => {
        if (currentChatId) {
            fetch(`http://localhost:8080/api/messages/${currentChatId}`)
                .then((response) => response.json())
                .then((data) => setMessages(data))
                .catch((error) => console.error("Error fetching messages:", error));
        }
    }, [currentChatId]); // Зависимость только от `currentChatId`

    useEffect(() => {
        fetchMessages();
    }, [fetchMessages]); // Теперь useEffect корректно следит за зависимостью

    const sendMessage = () => {
        if (newMessage.trim()) {
            const messageData = {
                content: newMessage,
                roomId: currentChatId,
                userId: userId || 1,
                timestamp: new Date().toISOString(),
            };

            fetch(`http://localhost:8080/api/messages/write`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(messageData),
            })
                .then(() => {
                    setNewMessage("");
                    fetchMessages(); // Перезапрос сообщений после отправки
                })
                .catch((error) => console.error("Error sending message:", error));
        }
    };

    return (
        <div className="chat-container">
            <div className="chat-header">
                <span>Chat Room: {currentChatId}</span>
                {userId && (
                    <button onClick={() => setCurrentChatId(currentChatId === 1 ? 2 : 1)}>
                        Switch to Room {currentChatId === 1 ? 2 : 1}
                    </button>
                )}
            </div>
            <div className="chat-messages">
                {messages.map((message, index) => (
                    <Message
                        key={index}
                        content={message.content}
                        sender={{
                            name: message.userDTO?.username || "Guest",
                            avatarUrl: message.userDTO?.avatarUrl || "",
                        }}
                        isOwnMessage={message.userDTO?.id === (userId || 1)}
                    />
                ))}
            </div>
            <div className="chat-input">
                <input
                    type="text"
                    placeholder="Type a message..."
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && sendMessage()}
                />
                <button onClick={sendMessage}>Send</button>
            </div>
        </div>
    );
};

export default ChatContainer;
