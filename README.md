🏃‍♂️ RunnaLog

RunnaLog is a personal running log application that helps users track runs, monitor progress, and explore subscription-based features.

⚡ Features

User registration & authentication

Admin and normal user roles

Logging, deleting and viewing runs aswell as other users run history

Running statistics tracking

Liking and commenting below runs

An in-app currency called strides that is used to upgrade subscriptions

Personal ai trainer that creates plans based on your preferences (unlocks only for admin and elite subscribers)

## 🛠️ Tech Stack

### Core Framework
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)

### Security & Authentication
![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)

### Database & Persistence
![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-3.x-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![H2 Database](https://img.shields.io/badge/H2_Database-2.x-004088?style=for-the-badge&logo=h2&logoColor=white) *(Testing)*

### Web & Frontend
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![Spring Web](https://img.shields.io/badge/Spring_Web-6.x-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Feign Client](https://img.shields.io/badge/Spring_Cloud_Feign-4.2.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### Performance & Caching
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Spring Cache](https://img.shields.io/badge/Spring_Cache-6.x-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### Development & Utilities
![Lombok](https://img.shields.io/badge/Lombok-1.18.28-FF5722?style=for-the-badge)
![Bean Validation](https://img.shields.io/badge/Bean_Validation-3.x-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Apache Commons Pool](https://img.shields.io/badge/Apache_Commons_Pool-2.12.0-7F2B7B?style=for-the-badge&logo=apache&logoColor=white)

### Build Tool
![Apache Maven](https://img.shields.io/badge/Maven-4.0.0-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

When the app starts and the database is empty:

Two users are added automatically:

Admin User – full access
    username: Admin
    password: admin1

Normal User – standard access
    username: User1
    password: user01

Pre-populated runs:

Both users get one run posted

The normal user starts with 6425 STR, so lecturers and testers can try-out the subscription logic

🔍 Testing Subscription Logic

Normal user’s 6425 STR allows testing features that require subscriptions.

Use the admin account to manage users and explore admin functionalities.

⚠️ Notes

Database must be running before starting the app

Auto-initialization only runs when the database is empty
