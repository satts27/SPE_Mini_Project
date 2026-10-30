# Calculator REST API

Calculator REST API built with Java 21, Spring Boot, Maven, Docker Compose, and Jenkins.

## Features

- Add, subtract, multiply, and divide endpoints
- Structured HTTP 400 responses for invalid input and division by zero
- Spring Boot Actuator health endpoint
- JUnit 5 unit and MockMvc integration tests
- Multi-stage Docker image
- Docker Compose deployment with Nginx reverse proxy
- Declarative Jenkins CI/CD pipeline

## Prerequisites

- Java 21
- Maven 3
- Docker Desktop with Docker Compose
- Jenkins with Docker access when running the pipeline
- Docker Hub account for publishing the application image

## Run locally with Maven

```powershell
cd S:\SPE_MiniProject
mvn clean test
mvn spring-boot:run
```

Open the application at `http://localhost:8081`.

## Run with Docker Compose

```powershell
cd S:\SPE_MiniProject
docker compose up --build -d
docker compose ps
```

Open:

- Frontend and API through Nginx: `http://localhost:8082`
- Calculator health endpoint: `http://localhost:8082/actuator/health`

Stop the stack:

```powershell
docker compose down
```

## Run the Docker Hub image directly

The Jenkins pipeline publishes the application image as:

```text
satts27/spe-calculator:latest
```

```powershell
docker login -u satts27
docker pull satts27/spe-calculator:latest
docker run --rm -d --name spe-calculator -p 8081:8081 satts27/spe-calculator:latest
```

Verify and stop the container:

```powershell
curl.exe http://localhost:8081/actuator/health
curl.exe "http://localhost:8081/api/v1/add?a=2&b=3"
docker stop spe-calculator
```

## Publish the image to Docker Hub

Create a Docker Hub access token and use it instead of an account password.

```powershell
docker login -u satts27
docker build -t satts27/spe-calculator:latest .
docker push satts27/spe-calculator:latest
```

Docker Hub repository:

```text
https://hub.docker.com/r/satts27/spe-calculator
```

## API examples

```powershell
curl.exe "http://localhost:8082/api/v1/add?a=2&b=3"
curl.exe "http://localhost:8082/api/v1/subtract?a=7&b=3"
curl.exe "http://localhost:8082/api/v1/multiply?a=2.5&b=4"
curl.exe "http://localhost:8082/api/v1/divide?a=7&b=2"
curl.exe http://localhost:8082/actuator/health
```

The calculator endpoints accept numeric `a` and `b` query parameters and return JSON. Missing or non-numeric parameters and division by zero return HTTP 400.

## Tests

```powershell
mvn clean test
```

JUnit XML reports are created in:

```text
target/surefire-reports/
```

Jenkins publishes these reports with the pipeline's `junit` step.

## Run Jenkins in Docker

This repository includes a containerized Jenkins image.

```powershell
cd S:\SPE_MiniProject
docker compose -f docker-compose.jenkins.yml up -d --build
```

Open Jenkins at:

```text
http://localhost:8090
```

Port 8090 is used if another Jenkins installation already owns port 8080. The mapping is controlled by `docker-compose.jenkins.yml`.

Required Jenkins plugins:

- Git
- Pipeline
- GitHub Branch Source
- Docker Pipeline
- Email Extension
- Workspace Cleanup

## Configure Jenkins

1. Complete the Jenkins initial setup.
2. Create a **Multibranch Pipeline** named `SPE_Mini_Project`.
3. Add the GitHub repository URL as the branch source:

   ```text
   https://github.com/satts27/SPE_Mini_Project.git
   ```

4. Add a Jenkins **Username with password** credential with ID `dockerhub-credentials`. Use the Docker Hub username and an access token.
5. Configure the SMTP settings under **Manage Jenkins → System → E-mail Notification**:
   - SMTP server: `smtp.gmail.com`
   - Port: `587`
   - Enable STARTTLS
   - Enable SMTP authentication
   - Use a Gmail address and Gmail App Password
6. Send a Jenkins test email and verify delivery.
7. Scan the repository and select **Build Now** on the `main` branch.

SMTP credentials and Docker Hub credentials must be stored in Jenkins, never committed to the repository.

## Jenkins pipeline stages

The `Jenkinsfile` performs the following stages:

1. Checkout repository source
2. Compile and package the Maven application
3. Run tests and publish JUnit XML results
4. Build the Docker image
5. Push the versioned image and `latest` tag to Docker Hub on `main`
6. Deploy with Docker Compose
7. Wait for the calculator and Nginx health checks
8. Send success or failure notifications by email

GitHub API rate limits can delay branch indexing when Jenkins uses anonymous GitHub access. Authenticate the GitHub branch source with a GitHub credential to avoid most anonymous-rate-limit delays.
