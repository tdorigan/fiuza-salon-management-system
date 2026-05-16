# Fiuza Salon Website and Management System

## Overview
A full-stack web application for managing a hair salon’s operations, including appointments, customers, products, and orders. Built with Java Spring Boot following the MVC pattern and deployed to production.

## Live Demo
https://fiuza-salon-management-system-production.up.railway.app/

Public pages are accessible without login.  
Admin access is not shared to protect the demo environment.

## Features

**Public Website**
- Services, products, and portfolio
- User registration and login
- English / Portuguese support

**Customer Area**
- Book appointments with availability rules
- Cart and order management
- Profile management

**Admin Portal**
- Manage services and products (CRUD)
- Image upload (Cloudflare R2)
- Manage appointments, orders, customers
- Basic reports

## Tech Stack
- Java 21, Spring Boot, Spring Security, Spring Data JPA
- Thymeleaf, Bootstrap
- MySQL
- Railway (hosting), Cloudflare R2 (storage)

## Architecture
Layered MVC:
- Controllers (routing)
- Services (business logic)
- Repositories (data access)
- Views (Thymeleaf SSR)

## Running Locally
Requires:
- MySQL database
- Environment variables for DB and R2 storage

Production credentials are not included.

## Author
Tiago Dorigan
