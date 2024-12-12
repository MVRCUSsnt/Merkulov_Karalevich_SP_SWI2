import React from "react";
import "./ChatWindow.css";

const ChatWindow = ({ activeChat }) => {
    return (
        <div className="chat-window">
            <h2>{activeChat}</h2>
            <div className="messages"></div>
            <div className="message-input">
                <input type="text" placeholder="Type a message..." />
                <button>Send</button>
            </div>
        </div>
    );
};

export default ChatWindow;
