import React, { useEffect, useState, useCallback, useRef } from "react";
import "./ChatContainer.css";
import Message from "./message/Message";
import WebSocketService from "./WebSocketService";
import { apiFetch } from "../../../api/client";
import { useNotify } from "../../common/NotificationContext";

const ChatContainer = ({ activeChat, userId }) => {
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState("");
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [users, setUsers] = useState([]);
    const [isUsersModalOpen, setIsUsersModalOpen] = useState(false);
    const [selectedMessage, setSelectedMessage] = useState(null);
    const [editedMessage, setEditedMessage] = useState("");
    const [attachmentFile, setAttachmentFile] = useState(null);
    const [isUploading, setIsUploading] = useState(false);
    const { notify } = useNotify();
    const fileInputRef = useRef(null);

    const currentUserId = userId || parseInt(localStorage.getItem("userId"), 10);
    const currentUsername = localStorage.getItem("user");
    const isAuthenticated = Boolean(currentUsername);

    const normalizeGroupMessage = useCallback((message) => ({
        id: message.messageId ?? message.id,
        roomId: message.roomId,
        content: message.content ?? "",
        attachmentUrl: message.attachmentUrl ?? null,
        timestamp: message.timestamp,
        userDTO: message.userDTO ?? null,
        userId: message.userId ?? message.userDTO?.id
    }), []);

    const normalizePrivateMessage = useCallback((message) => {
        const isOwn = message.senderUsername === currentUsername;
        return {
            id: message.id ?? `${message.senderUsername}-${message.timestamp}`,
            content: message.content ?? "Message unavailable",
            timestamp: message.timestamp,
            userDTO: {
                id: isOwn ? currentUserId : undefined,
                username: message.senderUsername,
                avatarUrl: "/default-avatar.webp",
            },
            isPrivate: true,
            senderUsername: message.senderUsername,
            recipientUsername: message.recipientUsername
        };
    }, [currentUserId, currentUsername]);

    const fetchMessages = useCallback(() => {
        if (!activeChat?.id || activeChat?.type !== "group") return;

        if (!isAuthenticated) {
            if (activeChat.id !== 1) {
                notify("Login required to view messages in this room.", "warning");
            }
            return;
        }

        apiFetch(`/api/messages/${activeChat.id}`, { method: "GET" }, { parse: "json" })
            .then(data => {
                const messagesData = Array.isArray(data) ? data : [];
                console.log("Messages received:", messagesData);
                setMessages(messagesData.map(normalizeGroupMessage));
            })
            .catch(() => {
                notify("Failed to load messages.", "error");
            });
    }, [activeChat?.id, activeChat?.type, isAuthenticated, notify, normalizeGroupMessage]);

    // Fetch users function (fixed syntax error here)
    const fetchChatUsers = useCallback(() => {
        if (!activeChat?.id || activeChat?.type !== "group") return;

        apiFetch(`/api/rooms/${activeChat.id}/users`, { method: "GET" }, { parse: "json" })
            .then(data => setUsers(Array.isArray(data) ? data : []))
            .catch(() => {
                notify("Failed to load users.", "error");
            });
    }, [activeChat?.id, activeChat?.type, notify]);

    useEffect(() => {
        setMessages([]);
        fetchMessages();
    }, [fetchMessages]);

    useEffect(() => {
        if (!activeChat?.id) return;

        WebSocketService.connect(() => {
            if (activeChat.type === "group") {
                WebSocketService.subscribeToChat(activeChat.id, (incomingMessage) => {
                    setMessages((prev) => [...prev, normalizeGroupMessage(incomingMessage)]);
                });
                return;
            }

            if (activeChat.type === "private") {
                WebSocketService.subscribeToPrivateMessages((incomingMessage) => {
                    const message = normalizePrivateMessage(incomingMessage);
                    const matchesChat = message.senderUsername === activeChat.recipientUsername
                        || message.recipientUsername === activeChat.recipientUsername;
                    if (matchesChat) {
                        setMessages((prev) => [...prev, message]);
                    }
                });
            }
        });

        return () => {
            if (activeChat.type === "group") {
                WebSocketService.unsubscribeFromChat(activeChat.id);
            }

            if (activeChat.type === "private") {
                WebSocketService.unsubscribeFromPrivateMessages();
            }
        };
    }, [activeChat, normalizeGroupMessage, normalizePrivateMessage]);

    useEffect(() => {
        if (selectedMessage) {
            setEditedMessage(selectedMessage.content);
        }
    }, [selectedMessage]);

    const clearAttachment = useCallback(() => {
        setAttachmentFile(null);
        if (fileInputRef.current) {
            fileInputRef.current.value = "";
        }
    }, []);

    useEffect(() => {
        setSelectedMessage(null);
        setEditedMessage("");
        clearAttachment();
    }, [activeChat?.id, activeChat?.type, clearAttachment]);

    const uploadAttachment = async () => {
        if (!attachmentFile) return null;

        const formData = new FormData();
        formData.append("file", attachmentFile);
        const response = await apiFetch("/api/files/upload", {
            method: "POST",
            body: formData
        }, { parse: "json" });

        return response?.url;
    };

    const sendMessage = async () => {
        if (isUploading) return;
        const hasContent = Boolean(newMessage.trim());
        const hasAttachment = Boolean(attachmentFile);
        if (!hasContent && !hasAttachment) return;

        if (activeChat.type === "private") {
            if (hasAttachment) {
                notify("Attachments are only supported in group chats.", "warning");
                return;
            }
            if (!isAuthenticated) {
                notify("Login required to send private messages.", "warning");
                return;
            }
            const messageData = {
                recipientId: activeChat.recipientId,
                content: newMessage,
            };

            apiFetch("/api/private-messages/send", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(messageData),
            }, { parse: "json" })
                .then((data) => {
                    setMessages((prev) => [...prev, normalizePrivateMessage(data)]);
                    setNewMessage("");
                })
                .catch(() => {
                    notify("Failed to send private message.", "error");
                });
            return;
        }

        if (!isAuthenticated && activeChat.id !== 1) {
            notify("Login required to send messages in this room.", "warning");
            return;
        }

        let attachmentUrl = null;
        if (hasAttachment) {
            setIsUploading(true);
            try {
                attachmentUrl = await uploadAttachment();
            } catch (error) {
                notify("Failed to upload attachment.", "error");
                setIsUploading(false);
                return;
            }
            setIsUploading(false);
        }

        const messageData = {
            content: newMessage,
            roomId: activeChat.id,
            attachmentUrl
        };

        apiFetch("/api/messages/write", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(messageData),
        }, { parse: "text" })
            .then(data => {
                console.log("Message sent:", data);
                setNewMessage("");
                clearAttachment();
            })
            .catch(() => {
                notify("Failed to send message.", "error");
            });
    };

    const deleteMessage = (messageId) => {
        if (activeChat?.type !== "group") return;
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
        if (activeChat?.type !== "group") return;
        if (!selectedMessage || !selectedMessage.id) {
            console.error("Error: Message ID is missing");
            return;
        }

        if (!editedMessage.trim()) return;

        const updatedMessageData = {
            content: editedMessage,
            roomId: activeChat.id,
            timestamp: new Date().toISOString(),
            userDTO: {
                id: currentUserId,
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

        apiFetch(`/api/rooms/addUser/${activeChat.id}/${userName}`, { method: "GET" }, { parse: "text" })
            .then(() => {
                notify(`User ${userName} added successfully.`, "success");
                fetchChatUsers();
            })
            .catch(error => notify(`Error adding user: ${error.message}`, "error"));
    };

    return (
        <div className="chat-container">
            <div className="chat-header">
                <span className="chat-title">{activeChat?.name || `Chat ${activeChat?.id}`}</span>
                {activeChat?.type === "group" && (
                    <button className="chat-info-btn" onClick={() => setIsModalOpen(true)}>Info</button>
                )}
            </div>

            <div className="chat-messages">
                {messages.length === 0 && (
                    <div className="chat-empty">No messages yet</div>
                )}
                {messages.map((message, index) => (
                    <Message
                        key={message.id || `msg-${index}`}
                        messageId={message.id}
                        content={message.content ?? ""}
                        sender={{
                            name: message.userDTO?.username || "Anonymous",
                            avatarUrl: message.userDTO?.avatarUrl || "/default-avatar.webp",
                        }}
                        timestamp={message.timestamp}
                        attachmentUrl={message.attachmentUrl}
                        isOwnMessage={
                            message.userDTO?.id === currentUserId
                            || message.userDTO?.username === currentUsername
                        }
                        onClick={() => {
                            if (activeChat?.type !== "group") return;
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
                <div className="chat-input-actions">
                    <input
                        type="file"
                        ref={fileInputRef}
                        className="chat-file-input"
                        onChange={(event) => setAttachmentFile(event.target.files?.[0] || null)}
                    />
                    {attachmentFile && (
                        <button
                            type="button"
                            className="chat-attachment-clear"
                            onClick={clearAttachment}
                        >
                            Clear
                        </button>
                    )}
                    <button onClick={sendMessage} disabled={isUploading}>
                        {isUploading ? "Uploading..." : "Send"}
                    </button>
                </div>
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

            {isModalOpen && activeChat?.type === "group" && (
                <div className="chat-modal-overlay" onClick={() => setIsModalOpen(false)}>␊
                    <div className="chat-modal" onClick={(e) => e.stopPropagation()}>
                        <h2>{activeChat?.name || `Chat ${activeChat?.id}`}</h2>
                        <p>{activeChat?.description || "No description available"}</p>
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