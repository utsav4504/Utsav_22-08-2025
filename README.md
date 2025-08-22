# Store Monitoring Application

Monitor restaurant store uptime and downtime across the US based on status polling data. The service ingests CSVs for **store statuses**, **business hours**, and **timezones**, computes availability only during each store’s **local business hours**, and produces a CSV report with metrics for the **last hour**, **last day**, and **last week**.

---

## ✨ Features

- CSV ingestion on startup (statuses, business hours, timezones)
- Local-timezone–aware calculations per store
- Uptime/downtime metrics restricted to business hours
- Asynchronous report generation (`/trigger_report` + `/get_report`)
- Downloadable CSV output

---

## 🧱 Tech Stack

- **Java** 17+
- **Spring Boot**
- **MySQL**
- **Maven**

---

## 📦 Data Sources

The app expects a ZIP containing three CSVs:

- `store_status.csv` – Timestamped active/inactive status polls per store
- `menu_hours.csv` – Business hours per store by day of week
- `timezones.csv` – Timezone per store

**Download:** [store-monitoring-data.zip](https://storage.googleapis.com/hiring-problem-statements/store-monitoring-data.zip)

After download, extract and place the CSV files in:

```
src/main/resources/data/
```

> ⚠️ The repository does not include these CSV files due to size limits.

---

## 🛠️ Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL server running

### Database Setup

Create a database named `store_db`, then configure credentials in `src/main/resources/application.properties`:

```properties
# --- Datasource ---
spring.datasource.url=jdbc:mysql://localhost:3306/store_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# --- JPA / Hibernate ---
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# --- App Settings ---
app.data.dir=src/main/resources/data
```

> Ensure the CSV files exist under `src/main/resources/data/` **before** starting the application.

### Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

On startup, the application ingests the CSVs and becomes ready to generate reports.

---

## 📡 API Endpoints

### `POST /trigger_report`

- Triggers asynchronous report generation.
- Response example:
  ```json
  {
    "report_id": "efb2d1f0-6e3a-4f2a-9a6a-1f3c8e3d9abc",
    "status": "Running"
  }
  ```

**cURL**
```bash
curl -X POST http://localhost:8080/trigger_report \
  -H "Content-Type: application/json"
```

---

### `GET /get_report?report_id={report_id}`

- Poll this endpoint using the `report_id` returned by `/trigger_report`.
- Responses:
  - `"Running"` while processing
  - `"Complete"` plus CSV content once ready

**cURL**
```bash
curl "http://localhost:8080/get_report?report_id=efb2d1f0-6e3a-4f2a-9a6a-1f3c8e3d9abc"
```

**Success Response Structure (example)**
```json
{
  "status": "Complete",
  "report": "store_id,uptime_last_hour,uptime_last_day,uptime_last_week,downtime_last_hour,downtime_last_day,downtime_last_week\n1,37,20.5,135.2,23,3.5,28.8\n..."
}
```

> Implementation detail may vary (e.g., CSV download vs inline string). Adjust as per your controller code.

---

## 📊 Sample Report Columns

Each row corresponds to a unique `store_id` and includes:

| Column               | Description                                                  |
|----------------------|--------------------------------------------------------------|
| `store_id`           | Unique identifier of the store                               |
| `uptime_last_hour`   | Minutes of uptime **during business hours** in the last hour |
| `uptime_last_day`    | Hours of uptime **during business hours** in last 24 hours   |
| `uptime_last_week`   | Hours of uptime **during business hours** in last 7 days     |
| `downtime_last_hour` | Minutes of downtime **during business hours** in last hour   |
| `downtime_last_day`  | Hours of downtime **during business hours** in last 24 hours |
| `downtime_last_week` | Hours of downtime **during business hours** in last 7 days   |

---

## 📁 Project Structure (excerpt)

```
.
├── src
│   ├── main
│   │   ├── java/... (Spring Boot sources)
│   │   └── resources
│   │       ├── application.properties
│   │       └── data/
│   │           ├── store_status.csv
│   │           ├── menu_hours.csv
│   │           └── timezones.csv
├── target/
├── pom.xml
└── README.md
```

---

## 🧹 .gitignore

```gitignore
# Ignore large data files
src/main/resources/data/

# Ignore build output
target/

# IDE files
.idea/
*.iml
.vscode/
```

---

## 🪟 Handling Line Ending Warnings (Windows)

Git may warn about LF vs CRLF line endings. To auto-convert safely:

```bash
git config --global core.autocrlf true
```

Alternatively, add a `.gitattributes`:

```gitattributes
* text=auto
```

---

## 🚀 Improvements & Future Work

- Incremental ingestion of new CSV data
- Secure REST APIs with authentication/authorization
- Pagination for large datasets
- Dockerize the service for easier deployment
- Unit and integration tests
- Use Git LFS or cloud storage for large files
- Scheduled periodic report generation
- Robust error handling and retries around ingestion
- Caching of recent report results

---

## 🎥 Demo

Watch a demo of the workflow:  
[Demo Video Link](https://loom.com/share/your-demo-video-link)

---

## 👤 Author

**Utsav Jain**  
Date: **August 22, 2025**
