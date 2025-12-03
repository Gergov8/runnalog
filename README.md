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

🛠️ Technologies
<div> <img src="https://img.shields.io/badge/Java-17-informational?style=flat&logo=java&logoColor=white" alt="Java 17" /> <img src="https://img.shields.io/badge/Spring_Boot-3.2.2-success?style=flat&logo=spring&logoColor=white" alt="Spring Boot" /> <img src="https://img.shields.io/badge/MySQL-8.0-blue?style=flat&logo=mysql&logoColor=white" alt="MySQL" /> <img src="https://img.shields.io/badge/Thymeleaf-3.1-orange?style=flat&logo=thymeleaf&logoColor=white" alt="Thymeleaf" /> </div>
🚀 Initial Setup

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
