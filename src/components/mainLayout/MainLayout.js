import React, { useState } from "react";
import Sidebar from "./sidebar/Sidebar";
import ChatContainer from "./chatContainer/ChatContainer";
import "./MainLayout.css"; // CSS для макета

const defaultChat = { id: 1, name: "Main Room", description: "Main Room", type: "group" };

const MainLayout = () => {
    const [activeChat, setActiveChat] = useState(defaultChat);

    return (
        <div className="main-layout">
            <Sidebar activeChat={activeChat} onSelectChat={setActiveChat} defaultChat={defaultChat} />
            <ChatContainer activeChat={activeChat} />
        </div>
    );
};

export default MainLayout;
