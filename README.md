# Merkulov Aleksandr R21004 - BackEnd 
# Karalevich Vitaly - FrontEnd

# **Chat API**

## **Project Overview**

This is a REST API for a chat application that allows users to send private messages, participate in group chats, and manage their accounts. The API is secured with JWT authentication, supports pagination, filtering, and follows RESTful principles.

## **Features**

- **User Authentication:** Register, login, and manage user accounts.
- **Private Messaging:** Send and receive private messages.
- **Group Chats:** Create, update, delete, and join chat rooms.
- **Filtering & Pagination:** Advanced filtering and paginated responses.
- **WebSockets & Kafka Integration:** Real-time messaging via WebSockets and message queuing with Kafka.
- **Swagger API Documentation:** Auto-generated API documentation.

## **Technologies Used**

- **Spring Boot** (Spring Security, Spring Data JPA, WebSockets)
- **MySQL** (Relational Database)
- **Kafka** (Message Queuing)
- **JWT** (Token-based authentication)
- **Swagger** (API Documentation)
- **Docker** (Optional for containerization)

## **Setup & Installation**

### **1. Clone the Repository**

```sh
git clone https://github.com/MVRCUSsnt/Merkulov_Karalevich_SP_SWI2.git
cd Merkulov_Karalevich_SP_SWI2
```

### **2. Configure Database**

Update `application.properties` with your MySQL database settings:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/chatdb
spring.datasource.username=root
spring.datasource.password=root
```

### **3. Run Kafka**

Start Kafka and Zookeeper:

```sh
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties
```

### **4. Build & Run the Project**

```sh
mvn clean install
mvn spring-boot:run
```

### **5. Access API Documentation**

Swagger UI: [http://127.0.0.1:8080/swagger-ui.html](http://127.0.0.1:8080/swagger-ui.html)

## **API Endpoints**

### **Authentication**

| Method | Endpoint             | Description                    |
| ------ | -------------------- | ------------------------------ |
| `POST` | `/api/auth/register` | Register a new user            |
| `POST` | `/api/auth/login`    | Authenticate and get JWT token |

### **Messages**

| Method   | Endpoint                | Description                 |
| -------- | ----------------------- | --------------------------- |
| `POST`   | `/api/messages`         | Send a message              |
| `GET`    | `/api/messages?filter=` | Get messages with filtering |
| `DELETE` | `/api/messages/{id}`    | Delete a message            |

### **Private Messages**

| Method   | Endpoint                            | Description              |
| -------- | ----------------------------------- | ------------------------ |
| `GET`    | `/api/private-messages/{senderId}`  | Get private messages     |
| `POST`   | `/api/private-messages`             | Send a private message   |
| `DELETE` | `/api/private-messages/{messageId}` | Delete a private message |

### **Chat Rooms**

| Method   | Endpoint          | Description           |
| -------- | ----------------- | --------------------- |
| `POST`   | `/api/chats`      | Create a chat room    |
| `GET`    | `/api/chats/{id}` | Get a chat room by ID |
| `PUT`    | `/api/chats/{id}` | Update a chat room    |
| `DELETE` | `/api/chats/{id}` | Delete a chat room    |

## **Security**

- All requests require authentication using a **Bearer Token**.
- To authenticate, first log in using `/api/auth/login` and obtain a JWT token.
- Use this token in the `Authorization` header for further API calls:
  ```sh
  curl -H "Authorization: Bearer YOUR_TOYOUR/127.0.0.1:8080/api/private-messages/1
  ```





