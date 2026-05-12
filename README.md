# Fiuza Salon Website and Management System

A full-stack web application developed using Spring Boot, Thymeleaf, and MySQL to support the operations of a hair salon business.

---

## Features

### 🌐 Public Website
- View salon information, services, and products
- Portfolio page
- User registration and login
- Language switch (English / Portuguese)

### 👤 Customer Area
- Book appointments with business hours validation
- View and cancel appointments
- Shopping cart
- Place orders and view order history
- Profile management

### 🛠️ Administrator Portal
- Manage services and products (CRUD + image upload)
- Manage customers
- Manage appointments
- Manage orders
- Reports with filters

---

## 🧰 Technologies Used

- Java, Spring Boot, Spring MVC, Spring Security
- Thymeleaf, HTML, CSS, JavaScript, Bootstrap
- MySQL, Hibernate / JPA
- Maven

---

## ⚙️ How to Run Locally

1. Clone repository:
   git clone https://github.com/tdorigan/fiuza-salon-management-system.git

2. Set environment variable:
   SPRING_DATASOURCE_PASSWORD=your_password

3. Create database:
   CREATE DATABASE fiuza_db;

4. Run:
   mvn spring-boot:run

5. Access:
   http://localhost:8080

---

## 🔐 Demo Accounts

Administrator is automatically created when the application starts:

Email: admin@fiuza.com  
Password: admin

Customers can register via the application.

---

## 🌍 Live Demo

(To be added after deployment)

---

## 👨‍💻 Author

Tiago Dorigan
