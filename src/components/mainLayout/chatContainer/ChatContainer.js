import React, { useEffect, useState, useCallback } from "react";
import "./ChatContainer.css";
import Message from "./message/Message";
import WebSocketService from "./WebSocketService";
import { apiFetch } from "../../../api/client";
import { useNotify } from "../../common/NotificationContext";

const ChatContainer = ({ activeChatId, chatInfo, onChangeChat, userId }) => {
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState("");
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [users, setUsers] = useState([]);
    const [isUsersModalOpen, setIsUsersModalOpen] = useState(false);
    const [selectedMessage, setSelectedMessage] = useState(null);
    const [editedMessage, setEditedMessage] = useState("");
    const { notify } = useNotify();

    const fetchMessages = useCallback(() => {
        if (!activeChatId) return;

        apiFetch(`/api/messages/${activeChatId}`, { method: "GET" }, { parse: "json" })
            .then(data => {
                const messagesData = Array.isArray(data) ? data : [];
                console.log("Messages received:", messagesData);
                setMessages(messagesData.map(msg => ({
                    id: msg.messageId ?? msg.id,
                    roomId: msg.roomId,
                    content: msg.content ?? "Message unavailable",
                    timestamp: msg.timestamp,
                    userDTO: msg.userDTO ?? null,
                    userId: msg.userId ?? msg.userDTO?.id
                })));
            })
            .catch(() => {
                notify("Failed to load messages.", "error");
            });
    }, [activeChatId, notify]);

    // Fetch users function (fixed syntax error here)
    const fetchChatUsers = useCallback(() => {
        if (!activeChatId) return;

        apiFetch(`/api/rooms/${activeChatId}/users`, { method: "GET" }, { parse: "json" })
            .then(data => setUsers(Array.isArray(data) ? data : []))
            .catch(() => {
                notify("Failed to load users.", "error");
            });
    }, [activeChatId, notify]);

    useEffect(() => {
        setMessages([]);
        fetchMessages();
    }, [fetchMessages]);

    useEffect(() => {
        if (!activeChatId) return;

        WebSocketService.connect(() => {
            WebSocketService.subscribeToChat(activeChatId, (incomingMessage) => {
                setMessages(prev => [...prev, incomingMessage]);
            });
        });

        return () => {
            WebSocketService.unsubscribeFromChat(activeChatId);
        };
    }, [activeChatId]);

    useEffect(() => {
        if (selectedMessage) {
            setEditedMessage(selectedMessage.content);
        }
    }, [selectedMessage]);

    const sendMessage = () => {
        if (!newMessage.trim()) return;

        const messageData = {
            content: newMessage,
            roomId: activeChatId,
            timestamp: new Date().toISOString(),
            userDTO: {
                id: userId || parseInt(localStorage.getItem("userId"), 10),
            }
        };

        apiFetch("/api/messages/write", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(messageData),
        }, { parse: "text" })
            .then(data => {
                console.log("Message sent:", data);
                setNewMessage("");
            })
            .catch(() => {
                notify("Failed to send message.", "error");
            });
    };

    const deleteMessage = (messageId) => {
        if (!messageId) {
            console.error("Error: Message ID is missing");
            return;
        }

        apiFetch(`/api/messages/delete/${messageId}`, { method: "DELETE" }, { parse: "text" })
            .then(() => {
                console.log(`Message with ID ${messageId} deleted`);
                setSelectedMessage(null);
                fetchMessages();
            })
            .catch(() => {
                notify("Failed to delete message.", "error");
            });
    };

    const editMessage = () => {
        if (!selectedMessage || !selectedMessage.id) {
            console.error("Error: Message ID is missing");
            return;
        }

        if (!editedMessage.trim()) return;

        const updatedMessageData = {
            content: editedMessage,
            roomId: activeChatId,
            timestamp: new Date().toISOString(),
            userDTO: {
                id: userId || parseInt(localStorage.getItem("userId"), 10),
            }
        };

        apiFetch(`/api/messages/edit/${selectedMessage.id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(updatedMessageData),
        }, { parse: "json" })
            .then(() => {
                setSelectedMessage(null);
                fetchMessages();
            })
            .catch(() => {
                notify("Failed to edit message.", "error");
            });
    };

    const handleAddUser = () => {
        const userName = prompt("Enter username to add:");
        if (!userName) return;

        apiFetch(`/api/rooms/addUser/${activeChatId}/${userName}`, { method: "GET" }, { parse: "text" })
            .then(() => {
                notify(`User ${userName} added successfully.`, "success");
                fetchChatUsers();
            })
            .catch(error => notify(`Error adding user: ${error.message}`, "error"));
    };

    return (
        <div className="chat-container">
            <div className="chat-header">
                <span className="chat-title">{chatInfo?.name || `Chat ${activeChatId}`}</span>
                <button className="chat-info-btn" onClick={() => setIsModalOpen(true)}>Info</button>
            </div>

            <div className="chat-messages">
                {messages.length === 0 && (
                    <div className="chat-empty">No messages yet</div>
                )}
                {messages.map((message, index) => (
                    <Message
                        key={message.id || `msg-${index}`}
                        messageId={message.id}
                        content={message.content || "Message unavailable"}
                        sender={{
                            name: message.userDTO?.username || "Anonymous",
                            avatarUrl: message.userDTO?.avatarUrl || "/default-avatar.webp",
                        }}
                        timestamp={message.timestamp}
                        isOwnMessage={
                            message.userDTO?.id === (userId || parseInt(localStorage.getItem("userId"), 10))
                        }
                        onClick={() => {
                            console.log("Selected message:", message);
                            setSelectedMessage(message);
                        }}
                    />
                ))}
            </div>

            <div className="chat-input">
                <input
                    type="text"
                    placeholder="Enter a message..."
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && sendMessage()}
                />
                <button onClick={sendMessage}>Send</button>
            </div>

            {selectedMessage && (
                <div className="message-edit-modal">
                    <div className="message-edit-content">
                        <h3>Edit Message</h3>
                        <textarea
                            value={editedMessage}
                            onChange={(e) => setEditedMessage(e.target.value)}
                        />

                        <button onClick={editMessage}>Save</button>
                        <button onClick={() => {
                            if (selectedMessage && selectedMessage.id) {
                                deleteMessage(selectedMessage.id);
                            } else {
                                console.error("Error: selectedMessage or ID missing", selectedMessage);
                            }
                        }}>Delete
                        </button>
                        <button onClick={() => setSelectedMessage(null)}>Cancel</button>
                    </div>
                </div>
            )}

            {isModalOpen && (
                <div className="chat-modal-overlay" onClick={() => setIsModalOpen(false)}>
                    <div className="chat-modal" onClick={(e) => e.stopPropagation()}>
                        <h2>{chatInfo?.name || `Chat ${activeChatId}`}</h2>
                        <p>{chatInfo?.description || "No description available"}</p>
                        <button className="chat-modal-btn" onClick={() => {
                            fetchChatUsers();
                            setIsUsersModalOpen(true);
                        }}>Participants Info
                        </button>
                        <button className="chat-modal-btn" onClick={handleAddUser}>Add Participant</button>
                        <button className="chat-modal-close" onClick={() => setIsModalOpen(false)}>Close</button>
                    </div>
                </div>
            )}

            {isUsersModalOpen && (
                <div className="chat-modal-overlay" onClick={() => setIsUsersModalOpen(false)}>
                    <div className="chat-modal" onClick={(e) => e.stopPropagation()}>
                        <h2>Chat Participants</h2>
                        {users.length > 0 ? (
                            <ul className="chat-users-list">
                                {users.map(user => (
                                    <li key={user.id} className="chat-user">
                                        <img src={user.avatarUrl || "/default-avatar.webp"} alt="Avatar"
                                             className="user-avatar"/>
                                        <span>{user.username}</span>
                                    </li>
                                ))}
                            </ul>
                        ) : <p>No participants</p>}
                        <button className="chat-modal-btn" onClick={fetchChatUsers}>Refresh List</button>
                        <button className="chat-modal-close" onClick={() => setIsUsersModalOpen(false)}>Close</button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ChatContainer;