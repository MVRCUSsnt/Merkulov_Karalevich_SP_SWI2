import React, { createContext, useCallback, useContext, useMemo, useState, useEffect } from "react";
import "./NotificationContext.css";
import { setUnauthorizedHandler } from "../../api/client";
import { clearSession } from "../../utils/session";

const NotificationContext = createContext(null);

export const NotificationProvider = ({ children }) => {
    const [message, setMessage] = useState(null);
    const [type, setType] = useState("error");

    const notify = useCallback((nextMessage, nextType = "error") => {
        setMessage(nextMessage);
        setType(nextType);
    }, []);

    const clear = useCallback(() => {
        setMessage(null);
        setType("error");
    }, []);

    const value = useMemo(() => ({ notify, clear }), [notify, clear]);

    useEffect(() => {
        const handler = () => {
            clearSession();
            notify("Session expired. Please log in again.", "warning");
        };
        setUnauthorizedHandler(handler);
        return () => setUnauthorizedHandler(null);
    }, [notify]);

    return (
        <NotificationContext.Provider value={value}>
            {message && (
                <div className={`notification-banner ${type}`}>
                    <span>{message}</span>
                    <button className="notification-close" onClick={clear} type="button">×</button>
                </div>
            )}
            {children}
        </NotificationContext.Provider>
    );
};

export const useNotify = () => {
    const context = useContext(NotificationContext);
    if (!context) {
        throw new Error("useNotify must be used within NotificationProvider");
    }
    return context;
};