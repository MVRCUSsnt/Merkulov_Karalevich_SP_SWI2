import React, { useEffect, useState } from 'react';
import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const ChatApp = () => {
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    let stompClient;

    useEffect(() => {
        const socket = new SockJS('http://localhost:8080/ws'); // URL к WebSocket endpoint
        stompClient = Stomp.over(socket);

        stompClient.connect({}, () => {
            stompClient.subscribe('/topic/messages', (message) => {
                setMessages((prevMessages) => [...prevMessages, message.body]);
            });
        });

        return () => {
            if (stompClient) stompClient.disconnect();
        };
    }, []);

    const sendMessage = () => {
        if (input && stompClient) {
            stompClient.send('/app/sendMessage', {}, input);
            setInput('');
        }
    };

    return (
        <div>
            <h1>Chat</h1>
            <div>
                {messages.map((msg, index) => (
                    <div key={index}>{msg}</div>
                ))}
            </div>
            <input
                value={input}
                onChange={(e) => setInput(e.target.value)}
                placeholder="Type a message..."
            />
            <button onClick={sendMessage}>Send</button>
        </div>
    );
};

export default ChatApp;
