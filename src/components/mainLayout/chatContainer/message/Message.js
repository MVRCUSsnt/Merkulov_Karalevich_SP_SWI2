import React from "react";
import "./Message.css";

const Message = ({ messageId, content, sender, timestamp, attachmentUrl, isOwnMessage, onClick }) => {
    const formatDate = (dateString) => {
        if (!dateString) return "";
        const date = new Date(dateString);
        return date.toLocaleString(); // Форматируем дату в удобный вид
    };

    const isImageAttachment = (url) => /\.(png|jpe?g|gif|webp|bmp|svg)$/i.test(url || "");

    return (
        <div
            className={`message-container ${isOwnMessage ? "own-message" : "other-message"}`}
            onClick={onClick}
        >
            {!isOwnMessage && sender && (
                <div className="message-info">
                    <img
                        src={sender.avatarUrl || "/default-avatar.webp"}
                        alt="Avatar"
                        className="message-avatar"
                    />
                    <span className="message-sender">{sender.name}</span>
                </div>
            )}
            <div className={`message ${isOwnMessage ? "own" : "other"}`}>
                {content && <div className="message-content">{content}</div>}
                {attachmentUrl && (
                    <div className="message-attachment">
                        {isImageAttachment(attachmentUrl) ? (
                            <img src={attachmentUrl} alt="Attachment" className="message-attachment-image" />
                        ) : (
                            <a href={attachmentUrl} target="_blank" rel="noreferrer" className="message-attachment-link">
                                View attachment
                            </a>
                        )}
                    </div>
                )}
                <div className="message-footer">
                    <span className="message-timestamp">{formatDate(timestamp)}</span>
                </div>
            </div>
        </div>
    );
};

export default Message;
