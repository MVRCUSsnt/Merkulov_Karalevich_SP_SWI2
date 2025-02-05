import React from "react";
import Sidebar from "./sidebar/Sidebar";
import ChatContainer from "./chatContainer/ChatContainer";
import "./MainLayout.css"; // CSS для макета

const MainLayout = () => {
    return (
        <div className="main-layout">
            <Sidebar />
            <ChatContainer />
        </div>
    );
};

export default MainLayout;
