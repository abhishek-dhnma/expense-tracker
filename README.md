# Expense Tracker Application

A simple and elegant web application for tracking daily expenses, built with **Spring Boot** and **Vanilla Web Technologies** (HTML, CSS, JS).

## Features

- **Dashboard:** View a summary of total expenses and recent transactions.
- **Add Expense:** Add new expenses with amount, category, and date.
- **View Expenses:** See a detailed list of all expenses.
- **Categories Breakdown:** View total expenses grouped by category.
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

This application is deployed and running live on **Railway**.

**Live Application Link:** [https://expense-tracker-production-63c8.up.railway.app](https://expense-tracker-production-63c8.up.railway.app)

### Note on Database
The app uses an embedded SQLite database (`expenses.db`). Because it is hosted on a free cloud server, the data will reset if the application goes to sleep or restarts. This is completely expected for this assignment. If we want permanent data saving in a real-world scenario, we would connect a separate PostgreSQL database.

## API Endpoints
- `GET /expenses`: Fetch all the expenses from the database.
- `POST /expenses`: Create and save a new expense.

## Application Screenshot
<!-- Add your screenshot here later -->
![Application Screenshot]()

## System Design & Architecture Decisions

This section outlines my approach to the assignment, highlighting how I ensured maintainability, data correctness, and a smooth user experience within the given timebox.

### 1. Maintainability (Not a throwaway prototype)
I designed this application using a standard, clean **layered architecture** in Spring Boot. By separating responsibilities (Controller for HTTP, Repository for DB access), the codebase remains decoupled. I included structured data validation (`@Valid`) and automated unit tests (`ExpenseControllerTest`) to prove this codebase is designed to be regression-tested and extended over time, not just a quick script.

### 2. Idempotency (Retries and Page Reloads)
To ensure the API behaves correctly under spotty network conditions or if a user reloads the page, I implemented an **Idempotency-Key pattern**. 
- **Frontend:** The UI generates a unique `Idempotency-Key` (`crypto.randomUUID()`) when submitting a form and attaches it to the headers.
- **Backend:** The controller checks an in-memory `ConcurrentHashMap`. If it recognizes a recent key, it returns the existing response instead of creating a duplicate row, completely preventing accidental double-charges.

### 3. Database Choice (SQLite)
I chose **SQLite** as the persistence mechanism over a raw JSON file or a heavyweight Relational DB (like PostgreSQL). It provides the robust transaction safety and relational querying of SQL, but requires **zero configuration**. It embeds directly into the app, meaning the project can be cloned and run instantly without needing to provision external database containers.

### 4. Edge Cases & UI Reliability
I designed the system to degrade gracefully rather than crash:
- **Spam Clicking:** When the user clicks "Submit", I immediately set a loading state that physically disables the button, preventing double-clicks.
- **Network Failures:** I wrapped the frontend API calls in `try/catch` blocks. If the server fails or is slow, the app catches the error and displays a user-friendly Toast Notification instead of silently failing.

### 5. Data Correctness and Money Handling
Handling financial data requires strict precision. 
- **BigDecimal for Money:** I strictly defined the `amount` field as a `BigDecimal` in Java. Using standard `double` or `float` for currency is an anti-pattern due to floating-point rounding errors. `BigDecimal` ensures exact decimal representation.
- **Boundary Validation:** I used `jakarta.validation` (`@Positive`, `@NotBlank`) at the controller level to guarantee that no negative expenses or empty categories can ever reach the database.

### 6. Code Clarity and Structure
I prioritized a codebase that is immediately readable by other developers. I utilized **Lombok** (`@Data`, `@RequiredArgsConstructor`) to completely eliminate noisy boilerplate code (like getters, setters, and massive constructors). The structure strictly separates Domain Models, Data Access, and HTTP Routing.

### 7. Trade-offs and Judgment
Given the strict time constraints, I had to be ruthless with prioritization to deliver a functional, high-quality core product.
- **What Mattered Most:** Core feature completeness, data integrity (BigDecimal and Idempotency), and error-resistant User Experience.
- **What I Intentionally Skipped:** I opted for Vanilla JavaScript instead of a heavy framework like React. I also stored Idempotency Keys in an in-memory map instead of Redis, and skipped user authentication. Building those would have consumed the entire timebox. By scoping down these non-essentials, I successfully delivered a robust, complete core product.

### 8. Scaling to Millions of Users
Right now, this application is built as a single server with a local database file. To extend this to handle millions of users in the real world, I would implement the following architectural changes:
- **Move to PostgreSQL (Database Scaling):** SQLite saves data to a local file and locks it during writes, which creates a bottleneck for concurrent users. I would migrate to a dedicated PostgreSQL or MySQL server, which easily handles thousands of concurrent database connections.
- **Use Redis for Idempotency (Distributed Caching):** Currently, Idempotency Keys are saved in the server's RAM (`ConcurrentHashMap`). In a horizontally scaled environment with multiple servers, Server A wouldn't know about the keys in Server B's RAM. I would move this cache to **Redis**, a fast distributed in-memory store, so all servers share the same state.
- **Add Pagination (API Scaling):** Currently, the API sends all expenses at once. For users with 100,000 expenses, this would crash the browser and overload the server. I would implement Spring Data JPA Pagination so the API only sends 50 expenses at a time.
- **Separate the Frontend (CDN):** Instead of serving the HTML/JS from the Spring Boot server, I would rewrite the frontend in React and host it on a CDN (like Vercel or Cloudflare). This offloads the heavy lifting of static asset delivery from the Java backend, allowing it to focus purely on API logic.

## License
MIT License
