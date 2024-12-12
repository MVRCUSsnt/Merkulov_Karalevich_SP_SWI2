import React, { useState } from "react";
import "./ChatWindow.css";

const ChatWindow = ({ activeChat }) => {
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState("");

    const sendMessage = () => {
        if (newMessage.trim()) {
            setMessages([...messages, { text: newMessage, sender: "You" }]);
            setNewMessage("");
        }
    };

    return (
        <div className="chat-window">
            <div className="chat-header">{activeChat}</div>
            <div className="chat-messages">
                {messages.map((message, index) => (
                    <div
                        key={index}
                        className={`chat-message ${
                            message.sender === "You" ? "sent" : "received"
                        }`}
                    >
                        {message.text}
                    </div>
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
