import React, { useEffect, useState } from "react";
import "./ChatWindow.css";
import Message from "../message/Message";


const ChatWindow = ({ activeChat }) => {
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState("");

    useEffect(() => {
        fetch(`http://localhost:8081/api/messages/main`)
            .then((response) => response.json())
            .then((data) => setMessages(data))
            .catch((error) => console.error("Error fetching messages:", error));
    }, [activeChat]);

    // Отправка сообщения
    const sendMessage = () => {
        if (newMessage.trim()) {
            const messageData = {
                content: newMessage,
                room: activeChat,
                sender: { id: 1 }, // надо заменить на ID текущего пользователя
            };

            fetch("http://localhost:8081/api/messages/main", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(messageData),
            })
                .then((response) => response.json())
                .then((newMessageFromServer) => {
                    setMessages([...messages, newMessageFromServer]);
                    setNewMessage("");
                })
                .catch((error) => console.error("Error sending message:", error));
        }
    };

    return (
        <div className="chat-window">
            <div className="chat-header">{activeChat}</div>
            <div className="chat-messages">
                {messages.map((message) => (
                    <Message
                        key={message.id}
                        content={message.content}
                        sender={message.sender}
                        isOwnMessage={message.sender.id === 1}
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

export default ChatWindow;
