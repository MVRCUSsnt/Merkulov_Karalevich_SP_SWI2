import React from "react";
import MainLayout from "./components/mainLayout/MainLayout";
import { AuthProvider } from "./components/authForm/AuthContext"; // Импортируем контекст
import { NotificationProvider } from "./components/common/NotificationContext";

function App() {
    return (
        <NotificationProvider>
            <AuthProvider>
                <MainLayout />
            </AuthProvider>
        </NotificationProvider>
    );
}

export default App;
