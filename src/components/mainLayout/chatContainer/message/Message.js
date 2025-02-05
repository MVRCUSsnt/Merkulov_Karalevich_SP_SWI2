import React from "react";
import "./Message.css";

const Message = ({ content, sender, isOwnMessage }) => {
    return (
        <div className={`message ${isOwnMessage ? "own" : "other"}`}>
            {!isOwnMessage && <div className="message-sender">{sender.name}</div>}
            <div className="message-content">{content}</div>
        </div>
    );
};

export default Message;
