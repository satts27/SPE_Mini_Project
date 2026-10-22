# Calculator REST API

A Java 21, Spring Boot, and Maven calculator service for the SPE mini-project.

## Build and test

```powershell
mvn clean test
mvn package
```

JUnit XML reports are written to `target/surefire-reports/`.

## Run

```powershell
mvn spring-boot:run
```

The API listens on `http://localhost:8081` (Jenkins uses port 8080).
Open `http://localhost:8081` in a browser to use the calculator frontend.

## API

All calculator endpoints accept decimal query parameters `a` and `b` and return a JSON object containing the operation, operands, and result.

```text
GET http://localhost:8081/api/v1/add?a=2&b=3
GET http://localhost:8081/api/v1/subtract?a=7&b=3
GET http://localhost:8081/api/v1/multiply?a=2.5&b=4
GET http://localhost:8081/api/v1/divide?a=7&b=2
GET http://localhost:8081/actuator/health
```

Missing or non-numeric parameters and division by zero return HTTP 400 with a JSON error response. Division results use `MathContext.DECIMAL128` precision.
