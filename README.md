# Countries

A Spring Boot application that fetches country data from public APIs, persists it to a relational database, and exposes services to work with the data. The project currently includes the data model, repositories, and service layer for synchronizing countries; HTTP endpoints are minimal/incomplete and marked as TODOs.

> Note: This README mirrors the current repository state and adds TODOs where functionality is not yet implemented or is unclear.

## Stack
- Language: Java 21
- Framework: Spring Boot 3.5.7
  - Spring Web
  - Spring Data JPA / JDBC
- Build & Package Manager: Maven (wrapper included: `mvnw`, `mvnw.cmd`)
- Database: PostgreSQL (via Docker Compose); H2 available as runtime/test dependency
- Logging: Spring Boot logging (application properties)
- Container/Infra: Docker Compose for PostgreSQL

## Entry Point
- Main application class: `com.osato.countries.CountriesApplication`
  - Starts the Spring Boot application context.

## Requirements
- Java 21 (JDK)
- Docker + Docker Compose (to run PostgreSQL locally)
- Git (optional)

## Configuration
Application properties are in `src/main/resources/application.properties`.

Environment/property keys:
- `spring.application.name` (default: `countries`)
- External APIs
  - `app.countries-domain` (default: `https://restcountries.com/v2/all?fields=name,capital,region,population,flag,currencies`)
  - `app.currency-domain` (default: `https://open.er-api.com/v6/latest/USD`)
- Database (PostgreSQL)
  - `spring.datasource.url` (default: `jdbc:postgresql://localhost:5332/countries_db`)
  - `spring.datasource.username` (default: `osato`)
  - `spring.datasource.password` (default: `isUglySubjective!`)
  - `spring.datasource.driver-class-name` (default: `org.postgresql.Driver`)
- JPA
  - `spring.jpa.hibernate.ddl-auto` (default: `update`)
  - `spring.jpa.show-sql` (default: `true`)
- Logging levels
  - `logging.level.com.example` (default: `DEBUG`)
  - `logging.level.org.springframework` (default: `INFO`)

Security note:
- The repository currently contains example credentials in `application.properties`. For real deployments, externalize secrets via environment variables or a secrets manager and remove hard-coded credentials from source control.

You can override any of the above via environment variables or an external properties file when running the app, e.g.:
- On Linux/macOS:
  ```bash
  export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5332/countries_db
  export SPRING_DATASOURCE_USERNAME=postgres
  export SPRING_DATASOURCE_PASSWORD=secret
  ./mvnw spring-boot:run
  ```
- On Windows (PowerShell):
  ```powershell
  $env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5332/countries_db"
  $env:SPRING_DATASOURCE_USERNAME="postgres"
  $env:SPRING_DATASOURCE_PASSWORD="secret"
  .\mvnw.cmd spring-boot:run
  ```

## Setup and Run
1. Start PostgreSQL via Docker Compose:
   ```bash
   docker compose up -d
   ```
   Docker compose file: `docker-compose.yml`
   - Exposes Postgres on `localhost:5332`
   - Default DB: `countries_db`, user: `osato`

2. Build and run the application:
   - Linux/macOS:
     ```bash
     ./mvnw clean package
     ./mvnw spring-boot:run
     ```
   - Windows (PowerShell):
     ```powershell
     .\mvnw.cmd clean package
     .\mvnw.cmd spring-boot:run
     ```

3. Packaging as a jar:
   ```bash
   ./mvnw clean package
   java -jar target/countries-0.0.1-SNAPSHOT.jar
   ```

## Scripts and Useful Commands
- Run app in dev: `./mvnw spring-boot:run` (or `.\mvnw.cmd spring-boot:run` on Windows)
- Run tests: `./mvnw test`
- Format/compile: `./mvnw -DskipTests package`
- Build Docker image (Spring Boot plugin):
  ```bash
  ./mvnw spring-boot:build-image -DskipTests
  ```

## HTTP API
Current controller `com.osato.countries.controllers.CountryController` is present but endpoints are commented/not implemented.

- TODO: Implement and document REST endpoints, e.g.:
  - `POST /countries/refresh` — trigger synchronization with external APIs
  - `GET /countries` — list countries
  - `GET /countries/{name}` — get a single country by name

When implemented, include example requests/responses here.

## Services Overview
- `CountryWebClientService`
  - Fetches countries from `app.countries-domain`
  - Fetches currency exchange rates from `app.currency-domain`
  - Maps and upserts country data into the database (see `Country` entity)
  - Method `syncAllCountries()` performs end-to-end refresh and upsert logic
- `ImageService` (present; behavior TBD)
- `CountryService` (skeleton)

## Data Model
- `Country` entity with fields: `name`, `nameNormalized`, `capital`, `region`, `population`, `currencyCode`, `exchangeRate`, `estimatedGdp`, `flagUrl`, `lastRefreshedAt`.
- `Metadata` entity is present.
- Repositories: `CountryRepository`, `MetadataRepository` (Spring Data JPA).

## Project Structure
```
.
├─ docker-compose.yml
├─ pom.xml
├─ src
│  ├─ main
│  │  ├─ java/com/osato/countries
│  │  │  ├─ CountriesApplication.java
│  │  │  ├─ config/AppConfig.java
│  │  │  ├─ controllers/CountryController.java
│  │  │  ├─ mappers/CountryMapper.java
│  │  │  ├─ models/dtos/{CountryDto, StatusResponse}.java
│  │  │  ├─ models/entities/{Country, Metadata}.java
│  │  │  ├─ repositories/{CountryRepository, MetadataRepository}.java
│  │  │  └─ services/{CountryService, CountryWebClientService, ImageService}.java
│  │  └─ resources/application.properties
│  └─ test/java/com/osato/countries/CountriesApplicationTests.java
└─ target/ ... (build output)
```

## Tests
- Unit/integration tests can be run via Maven:
  ```bash
  ./mvnw test
  ```
- Current test suite: `CountriesApplicationTests` exists; add more tests as functionality grows.
- TODO: Add tests for `CountryWebClientService.syncAllCountries()` and repository interactions. Consider using Testcontainers for PostgreSQL.

## Database
- Default connection uses local Postgres via Docker Compose:
  - Host: `localhost:5332`
  - DB: `countries_db`
  - User: `osato`
  - Password: `isUglySubjective!`
- Schema management: `spring.jpa.hibernate.ddl-auto=update`
- TODO: Migrate to schema migrations with Flyway or Liquibase.

## License
- TODO: Add a proper license (e.g., MIT, Apache-2.0). The `pom.xml` currently has an empty `<licenses>` block.

## Roadmap / TODOs
- [ ] Implement REST endpoints in `CountryController` and document them here
- [ ] Add error handling for external API failures (custom exceptions, proper HTTP status mapping)
- [ ] Externalize secrets and remove sample credentials from repo
- [ ] Add integration tests (preferably with Testcontainers) and unit tests for services/mappers
- [ ] Add CI workflow (build, test)
- [ ] Add Flyway/Liquibase migrations
- [ ] Provide OpenAPI/Swagger documentation
