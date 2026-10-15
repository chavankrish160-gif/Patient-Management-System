# 🏥 Patient Management System

A simple, web-based Patient Management System built with **Java, HTML, CSS, and JavaScript**. The application provides a clean interface to manage patient records with persistent JSON file storage.

## ✨ Features

- **Add Patients** — Register new patients with their personal and admission details.
- **View Patients** — Display saved patient records.
- **Edit Patients** — Update existing patient information.
- **Delete Patients** — Remove patient records when they are no longer needed.
- **Input Validation** — Validate required fields, age, and gender.
- **Persistent Storage** — Store patient records in a local JSON file.
- **REST API** — Handle patient operations through HTTP endpoints.
- **Responsive Interface** — Use the web interface to manage records.

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java | Backend logic and HTTP server |
| HTML5 | Web page structure |
| CSS3 | Styling and layout |
| JavaScript | Frontend interactions and API requests |
| JSON | Patient data storage |
| Git & GitHub | Version control and source code hosting |

## 📁 Project Structure

```text
Patient-Management-System/
├── data/
│   └── patients.json
├── src/
│   ├── patientrecord/
│   │   ├── Json.java
│   │   ├── PatientHandler.java
│   │   ├── PatientRecordsApplication.java
│   │   └── PatientRepository.java
│   └── public/
│       ├── index.html
│       ├── css/
│       │   └── style.css
│       └── js/
│           └── app.js
├── .gitignore
├── PRD.md
└── README.md
```

## ⚙️ Requirements

- Java Development Kit (JDK) 17 or later
- A modern web browser
- Git (optional, for cloning the repository)

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/chavankrish160-gif/Patient-Management-System.git
```

### 2. Open the project directory

```bash
cd Patient-Management-System
```

### 3. Compile the Java source files

Run this command from the project root in Windows Command Prompt:

```cmd
javac --add-modules jdk.httpserver -d out src\patientrecord\*.java
```

### 4. Start the application

```cmd
java --add-modules jdk.httpserver -cp out patientrecord.PatientRecordsApplication
```

### 5. Open the application

Visit the following URL in your browser:

**http://localhost:8081**

The application uses port `8081` by default. You can set the `SERVER_PORT` environment variable to use another port.

## 🔌 API Endpoints

The backend exposes the following patient management endpoints.

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/patients` | Retrieve all patients |
| GET | `/api/patients/{id}` | Retrieve a patient by ID |
| POST | `/api/patients` | Create a patient record |
| PUT | `/api/patients/{id}` | Update a patient record |
| DELETE | `/api/patients/{id}` | Delete a patient record |

Patient records include fields such as name, age, gender, phone number, address, medical problem, and admission date.

## 💾 Data Storage

Patient records are stored locally in:

```text
data/patients.json
```

The application reads and updates this file to maintain records between restarts. Back up this file before making changes to your stored data.

**Note:** JSON file storage is suitable for a small learning project or demonstration. It is not a substitute for a secure, production-grade healthcare database.

## 🔒 Security Notice

This project is intended for educational and portfolio purposes. It does not currently implement a complete authentication and authorization system. Do not use it to store real patient information without appropriate security, access controls, privacy protections, and testing.

## 🎯 Project Objectives

- Understand Java backend development and HTTP request handling.
- Learn how a frontend communicates with backend APIs.
- Implement CRUD (Create, Read, Update, Delete) operations.
- Practice data validation and persistent storage.
- Build a practical full-stack project for learning and portfolio development.

## 👨‍💻 Author

**Krish Chavan**

GitHub: [@chavankrish160-gif](https://github.com/chavankrish160-gif)

---

⭐ If you find this project useful, consider starring the repository.

