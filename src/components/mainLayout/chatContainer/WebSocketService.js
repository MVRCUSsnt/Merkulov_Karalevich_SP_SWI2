import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { getWsUrl } from "../../../api/client";

const SOCKET_URL = getWsUrl();

class WebSocketService {
    constructor() {
        this.client = null;
        this.subscriptions = {};
        this.isConnected = false;
        this.pendingSubscriptions = [];
    }

    connect(onConnectCallback, onErrorCallback) {
        if (this.client && this.isConnected) {
            console.log("WebSocket already connected");
            if (onConnectCallback) onConnectCallback();
            return;
        }

        console.log("Opening Web Socket...");
        const socket = new SockJS(SOCKET_URL);
        this.client = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            debug: (str) => console.log(str),
            onConnect: () => {
                console.log("WebSocket connected");
                this.isConnected = true;

                // Process pending subscriptions
                this.pendingSubscriptions.forEach(({ chatId, callback }) => {
                    this.subscribeToChat(chatId, callback);
                });
                this.pendingSubscriptions = [];

                if (onConnectCallback) onConnectCallback();
            },
            onDisconnect: () => {
                console.warn("WebSocket disconnected");
                this.isConnected = false;
            },
            onStompError: (frame) => {
                console.error("STOMP Error:", frame);
                this.isConnected = false;
                if (onErrorCallback) onErrorCallback(frame);
            }
        });

        this.client.activate();
    }

    subscribeToChat(chatId, onMessageReceived) {
        if (!this.client || !this.isConnected) {
            console.warn(`WebSocket not connected yet, subscription deferred: ${chatId}`);
            this.pendingSubscriptions.push({ chatId, callback: onMessageReceived });
            return;
        }

        if (this.subscriptions[chatId]) {
            console.warn(`Already subscribed to ${chatId}`);
            return;
        }

        this.subscriptions[chatId] = this.client.subscribe(`/topic/messages/${chatId}`, (message) => {
            const newMessage = JSON.parse(message.body);
            console.log("New message:", newMessage);
            onMessageReceived(newMessage);
        });
        console.log(`Subscribed to chat ${chatId}`);
    }

    unsubscribeFromChat(chatId) {
        if (this.subscriptions[chatId]) {
            this.subscriptions[chatId].unsubscribe();
            delete this.subscriptions[chatId];
            console.log(`Unsubscribed from chat ${chatId}`);
        }
    }

    disconnect() {
        if (this.client) {
            Object.keys(this.subscriptions).forEach(chatId => {
                this.unsubscribeFromChat(chatId);
            });

            this.client.deactivate();
            this.isConnected = false;
            console.log("WebSocket disconnected");
        }
    }
}

export default new WebSocketService();