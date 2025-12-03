# 🏃‍♂️ RunnaLog

**RunnaLog** is a personal running log application that helps users track runs, monitor progress, and explore subscription-based features with an engaging social component.

---

## ✨ Features

### 🔐 **Account & Security**
- **User Registration & Authentication** – Secure sign-up and login
- **Role-Based Access** – Admin, Elite, and Standard user roles
- **In-App Currency System** – Earn and spend **STRIDES (STR)** for upgrades

### 🏃 **Running Features**
- **Log Runs** – Track distance, time, pace, and routes
- **View History** – Personal running log with statistics
- **Social Feed** – See other runners' activities
- **Like & Comment** – Engage with the community
- **Running Statistics** – Visualize progress over time

### 💎 **Subscription Tiers**
- **Free Tier** – Basic features
- **Elite Subscription** – Unlock premium features with STR
- **Admin Features** – Full system access
- **AI Personal Trainer** – *(Elite/Admin only)* Get custom training plans based on your preferences

### 🤖 **AI Integration**
- **Personalized Plans** – AI-generated training schedules
- **Smart Recommendations** – Based on your running history
- **Progress Analysis** – Get insights on your performance

---

## 🛠️ Tech Stack

### **Core Framework**
<p align="left">
  <img src="https://img.shields.io/badge/Spring_Boot-3.4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/Apache_Maven-4.0.0-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven">
</p>

### **Security & Authentication**
<p align="left">
  <img src="https://img.shields.io/badge/Spring_Security-6.x-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security">
  <img src="https://img.shields.io/badge/Thymeleaf_Security-3.x-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white" alt="Thymeleaf Security">
</p>

### **Database & Persistence**
<p align="left">
  <img src="https://img.shields.io/badge/MySQL-8.x-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/Spring_Data_JPA-3.x-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Data JPA">
  <img src="https://img.shields.io/badge/H2_Testing_DB-2.x-004088?style=for-the-badge&logo=h2&logoColor=white" alt="H2 Database">
</p>

### **Web & Frontend**
<p align="left">
  <img src="https://img.shields.io/badge/Thymeleaf-3.x-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white" alt="Thymeleaf">
  <img src="https://img.shields.io/badge/Spring_Web-6.x-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Web">
  <img src="https://img.shields.io/badge/Spring_Cloud_Feign-4.2.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Feign Client">
  <img src="https://img.shields.io/badge/Bean_Validation-3.x-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Bean Validation">
</p>

### **Performance & Caching**
<p align="left">
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" alt="Redis">
  <img src="https://img.shields.io/badge/Spring_Cache-6.x-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Cache">
  <img src="https://img.shields.io/badge/Apache_Commons_Pool-2.12.0-7F2B7B?style=for-the-badge&logo=apache&logoColor=white" alt="Apache Commons Pool">
</p>

### **Development Tools**
<p align="left">
  <img src="https://img.shields.io/badge/Lombok-1.18.28-FF5722?style=for-the-badge&logo=lombok&logoColor=white" alt="Lombok">
  <img src="https://img.shields.io/badge/Spring_DevTools-3.x-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring DevTools">
  <img src="https://img.shields.io/badge/JUnit_Testing-5.x-25A162?style=for-the-badge&logo=junit5&logoColor=white" alt="JUnit">
</p>

---

## 🚀 Quick Start

### **Initial Setup**
1. **Ensure MySQL is running** on your local machine
2. **Clone and run the application**
3. **Database auto-initializes** with demo data when empty

### **Pre-loaded Accounts**
| Role | Username | Password | Features |
|------|----------|----------|----------|
| **Admin** | `Admin` | `admin1` | Full system access, AI trainer |
| **Normal User** | `User1` | `user01` | 6425 STR for subscription testing |

### **Demo Data**
- ✅ **Both users** get sample runs
- ✅ **User1 starts with 6425 STR** – test subscription upgrades
- ✅ **Social features enabled** – likes and comments

---

## 💡 Testing Highlights

### **Currency & Subscriptions**
```text
User1's Balance: 6425 STR
→ Test upgrading to Elite tier
→ Experience premium features
→ Validate currency deduction logic
```

### **Role-Based Features**
```text
Admin Account → Full control panel
Elite Subscription → AI Trainer access
Standard User → Basic features + social
```

### **Social Features**
```text
✅ Like and comment on runs
✅ View community activities  
✅ Track engagement metrics
```

---

## ⚠️ Important Notes

| Requirement | Status | Notes |
|------------|--------|-------|
| **MySQL Database** | 🔴 **Required** | Must be running before app start |
| **Auto-Initialization** | ✅ **Automatic** | Only runs on empty database |
| **Redis Cache** | ⚡ **Optional** | Enhances performance |
| **Testing Database** | 🧪 **H2** | Used for unit tests only |

---

## 📊 Architecture Overview

```mermaid
graph TB
    A[Client Browser] --> B[Spring MVC]
    B --> C[Spring Security]
    B --> D[Thymeleaf Templates]
    C --> E[User Authentication]
    B --> F[Business Logic]
    F --> G[Data Persistence]
    G --> H[(MySQL Database)]
    F --> I[Redis Cache]
    F --> J[Feign Client]
    J --> K[External APIs]
    K --> L[AI Trainer Service]
    
    style A fill:#e1f5fe
    style H fill:#fce4ec
    style I fill:#ffebee
    style L fill:#e8f5e8
```

---

## 🎯 Project Goals

| Goal | Status | Description |
|------|--------|-------------|
| **User Engagement** | ✅ | Social features encourage activity |
| **Monetization Path** | ✅ | STR currency & subscription tiers |
| **Performance** | ✅ | Redis caching for speed |
| **Scalability** | ✅ | Microservice-ready architecture |
| **Learning Tool** | ✅ | Complete Spring Boot example |

---

<p align="center">
  <b>🏃 Start tracking your runs today with RunnaLog! 🏃‍♀️</b>
</p>
