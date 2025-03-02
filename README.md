# Daily Davlog : backend
## 1. Overview
Daily Devlog is a web service designed to analyze users' daily development activities using GitHub OAuth authentication. It collects commits and issue activity data automatically and provides an AI-powered daily retrospective summary. The system offers insightful feedback on a developer’s work, helping them reflect on their productivity and progress.

## 2. Features
🔑 GitHub OAuth Login
Users can securely log in using their GitHub credentials, allowing the system to retrieve their development activities.

📊 Automated GitHub Activity Tracking
Once authenticated, the service automatically collects commits and issue data from the user’s repositories.

🧠 AI-powered Activity Analysis (GPT API)
The GPT API processes and analyzes the collected data to generate a structured summary of the user’s daily work. This includes:

A brief retrospective report summarizing the user’s contributions
AI-generated feedback based on daily activities
Highlighting key achievements and areas for improvement
📜 Interactive API Documentation (Swagger)
The API comes with Swagger documentation, making it easy for developers to explore and integrate the service.

📡 Real-Time Monitoring with Redis
Using Redis, the system supports real-time monitoring of GitHub activities, ensuring that data is always up to date and responsive.

## 3. Technology Stack
- Java 17+
- Spring Boot 3.x
- Maven/Gradle
- GitHub API
OpenAI GPT API
## 4. How It Works (Execution Steps)
User Authentication & Access Token Issuance

The user logs in via GitHub OAuth, and an access token is issued to authenticate API requests.
Data Collection from GitHub

Only authenticated users can retrieve commit histories and issue lists from specific repositories.
The service periodically fetches data to ensure accurate tracking of the user’s activity.
AI Analysis & Personalized Retrospective

The collected data is sent to the GPT API, where it undergoes AI-driven analysis.
The API processes the information, generates a daily report, and provides constructive feedback on the user's progress.
The user receives a summarized retrospective highlighting their strengths, contributions, and possible areas of improvement.
## 🔥 Why Use Daily Devlog?
Daily Devlog is an efficient way for developers to track their progress and gain valuable insights into their coding habits. Whether you're an individual developer looking for motivation or a team wanting to monitor progress, this service provides a clear, AI-generated summary of your work every day.
