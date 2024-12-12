import React from "react";
import "./Sidebar.css";

const Sidebar = ({ activeChat, onChatChange, username, avatar, setMenuOpen }) => {
    const chats = ["Main Room", "SWI2", "TETL1", "AGI3", "VBAP", "PRCS", "AKI2"];

    return (
        <div className="sidebar">
            <div className="contacts">
                {chats.map((chat) => (
                    <div
                        key={chat}
                        className={`contact ${activeChat === chat ? "active" : ""}`}
                        onClick={() => onChatChange(chat)}
                    >
                        {chat}
                    </div>
                ))}
            </div>
            <div className="user-profile" onClick={() => setMenuOpen(true)}>
                <img src={avatar} alt="Avatar" className="avatar" />
                <div className="username">{username}</div>
            </div>
        </div>
    );
};

export default Sidebar;
