import React, { useState } from "react";
import Sidebar from "./components/sidebar/Sidebar";
import ChatWindow from "./components/chatWindow/ChatWindow";
import "./App.css";

const App = () => {
    const [activeChat, setActiveChat] = useState("Main Room"); // Текущий активный чат

    return (
        <div className="app-container">
            <Sidebar activeChat={activeChat} onChatChange={setActiveChat} />
            <ChatWindow activeChat={activeChat} />
        </div>
    );
};

export default App;
