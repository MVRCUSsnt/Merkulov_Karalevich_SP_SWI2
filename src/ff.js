import React, { useEffect, useState, useCallback } from "react";
import "./ChatContainer.css";
import Message from "./message/Message";

const ChatContainer = ({ activeChatId, chatInfo, onChangeChat, userId }) => {
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState("");
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [users, setUsers] = useState([]);
    const [isUsersModalOpen, setIsUsersModalOpen] = useState(false);

    // 🔹 Оптимизированный fetchMessages
    const fetchMessages = useCallback(() => {
        if (!activeChatId) return;

        fetch(http://localhost:8080/api/messages/${activeChatId}, {
        method: "GET",
            credentials: "include",
    })
        .then(response => response.json())
        .then(data => {
            console.log("📩 Полученные сообщения:", data);
            setMessages(data);
        })
        .catch(error => console.error("❌ Ошибка загрузки сообщений:", error));
}, [activeChatId]);

useEffect(() => {
    setMessages([]); // Очистка сообщений при смене чата
    fetchMessages();
}, [fetchMessages]);

// 🔹 Отправка сообщения
const sendMessage = () => {
    if (!newMessage.trim()) return;

    const messageData = {
        content: newMessage,
        roomId: activeChatId,
        userId: userId || 1,
        timestamp: new Date().toISOString(),
    };

    fetch("http://localhost:8080/api/messages/write", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(messageData),
    })
        .then(response => {
            if (!response.ok) throw new Error(Ошибка: ${response.status});

            // Проверяем, является ли ответ JSON
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.includes("application/json")) {
                return response.json(); // ✅ Если JSON — парсим
            } else {
                return response.text(); // ❗ Иначе читаем как текст
            }
        })
        .then(data => {
            console.log("📩 Ответ сервера на отправку сообщения:", data);
            setNewMessage("");
            fetchMessages(); // Обновляем чат после отправки
        })
        .catch(error => console.error("❌ Ошибка отправки сообщения:", error));
};


// 🔹 Получение участников чата
const fetchChatUsers = () => {
    fetch(http://localhost:8080/api/rooms/${activeChatId}/users, {
    method: "GET",
        credentials: "include",
})
.then(response => response.json())
    .then(data => setUsers(data))
    .catch(error => console.error("❌ Ошибка загрузки участников:", error));
};

// 🔹 Добавление участника
const handleAddUser = () => {
    const userName = prompt("Введите имя пользователя для добавления:");
    if (!userName) return;

    fetch(http://localhost:8080/api/rooms/addUser/${activeChatId}/${userName}, {
    method: "GET",
        credentials: "include",
})
.then(response => response.text())
    .then(() => {
        alert(✅ Пользователь ${userName} успешно добавлен в чат!);
        fetchChatUsers();
    })
    .catch(error => alert(❌ Ошибка: ${error.message}));
};

return (
    <div className="chat-container">
        {/* Хедер чата */}
        <div className="chat-header">
            <span className="chat-title">{chatInfo?.name || Чат ${activeChatId}}</span>
            <button className="chat-info-btn" onClick={() => setIsModalOpen(true)}>ℹ️</button>
        </div>

        {/* Список сообщений */}
        <div className="chat-messages">
            {messages.map((message, index) => (
                <Message
                    key={message.id || msg-${index}}
                    content={message.content}
                    sender={{
                        name: message.userDTO?.username || "Anonymous",
                        avatarUrl: message.userDTO?.avatarUrl || "/default-avatar.webp",
                    }}
                    isOwnMessage={message.userId === userId}
                />
            ))}
        </div>

        {/* Поле ввода */}
        <div className="chat-input">
            <input
                type="text"
                placeholder="Введите сообщение..."
                value={newMessage}
                onChange={(e) => setNewMessage(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && sendMessage()}
            />
            <button onClick={sendMessage}>Отправить</button>
        </div>

        {/* Модальное окно с информацией о чате */}
        {isModalOpen && (
            <div className="chat-modal-overlay" onClick={() => setIsModalOpen(false)}>
                <div className="chat-modal" onClick={(e) => e.stopPropagation()}>
                    <h2>{chatInfo?.name || Чат ${activeChatId}}</h2>
                    <p>{chatInfo?.description || "Описание отсутствует"}</p>
                    <button className="chat-modal-btn" onClick={() => {
                        fetchChatUsers();
                        setIsUsersModalOpen(true);
                    }}>Информация о участниках</button>
                    <button className="chat-modal-btn" onClick={handleAddUser}>Добавить участника</button>
                    <button className="chat-modal-close" onClick={() => setIsModalOpen(false)}>Закрыть</button>
                </div>
            </div>
        )}

        {/* Модальное окно с участниками */}
        {isUsersModalOpen && (
            <div className="chat-modal-overlay" onClick={() => setIsUsersModalOpen(false)}>
                <div className="chat-modal" onClick={(e) => e.stopPropagation()}>
                    <h2>Участники чата</h2>
                    {users.length > 0 ? (
                        <ul className="chat-users-list">
                            {users.map(user => (
                                <li key={user.id} className="chat-user">
                                    <img src={user.avatarUrl || "/default-avatar.webp"} alt="Аватар" className="user-avatar"/>
                                    <span>{user.username}</span>
                                </li>
                            ))}
                        </ul>
                    ) : <p>Нет участников</p>}
                    <button className="chat-modal-btn" onClick={fetchChatUsers}>Обновить список</button>
                    <button className="chat-modal-close" onClick={() => setIsUsersModalOpen(false)}>Закрыть</button>
                </div>
            </div>
        )}
    </div>
);
};

export default ChatContainer;