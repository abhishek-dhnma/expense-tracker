# Expense Tracker Application

A simple and elegant web application for tracking daily expenses, built with **Spring Boot** and **Vanilla Web Technologies** (HTML, CSS, JS).

## Features

- **Dashboard:** View a summary of total expenses and recent transactions.
- **Add Expense:** Add new expenses with amount, category, and date.
- **View Expenses:** See a detailed list of all expenses.
- **Categories Breakdown:** View total expenses grouped by category.
- **Delete Expenses:** Easily remove incorrect or old expenses.
- **Responsive Design:** Works seamlessly on both desktop and mobile devices.

## Tech Stack

- **Backend:** Java 21, Spring Boot (Web, Data JPA)
- **Database:** SQLite (Embedded, zero-configuration)
- **Frontend:** HTML5, CSS3, Vanilla JavaScript
- **Icons:** FontAwesome

## Local Development

### Prerequisites
- Java 21+ installed
- Maven installed

### Running the Application
1. Clone the repository:
   ```bash
   git clone <your-repo-link>
   cd expensetracker
   ```

2. Run using Maven:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(On Windows, use `mvnw.cmd spring-boot:run`)*

3. Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

## Deployment

This application can be easily deployed to container-based hosting platforms like [Render](https://render.com) or [Railway](https://railway.app) using their native Java build support.

### Render Native Java Deployment
1. Create a New Web Service.
2. Connect your GitHub repository.
3. Select the **Java** language environment.
4. Build Command: `./mvnw clean package -DskipTests`
5. Start Command: `java -jar target/expensetracker-0.0.1-SNAPSHOT.jar`

### Note on Database
The application uses an embedded SQLite database (`expenses.db`). When deployed to free tiers of services like Render or Railway, any added data will be reset if the application container restarts. For persistent data in production, consider attaching a persistent disk volume or updating `application.properties` to connect to a hosted PostgreSQL database.

## API Endpoints
- `GET /api/expenses`: Retrieve all expenses
- `GET /api/expenses/summary`: Retrieve summary statistics (total, categories)
- `POST /api/expenses`: Create a new expense
- `DELETE /api/expenses/{id}`: Delete an expense by ID

## License
MIT License
