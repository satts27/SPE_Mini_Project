# Calculator REST API

A Java 21, Spring Boot, and Maven calculator service for the SPE mini-project, with a browser frontend and a Jenkins/Docker Compose delivery pipeline.

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

## Run with Docker Compose

Docker Compose builds the multi-stage image, starts the calculator and Nginx reverse proxy, and waits for their health checks:

```powershell
docker compose up --build -d
```

Open `http://localhost:8082` for the frontend and API. Jenkins keeps port 8080; the calculator listens on port 8081 inside the Compose network. To stop the stack, run `docker compose down`.

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

## Jenkins pipeline

The declarative `Jenkinsfile` builds and tests every branch and pull request, archives the executable JAR, and builds a Docker image. Only builds for the `main` branch push the versioned image and `latest` tag to Docker Hub and deploy the stack with Docker Compose. Deployment waits for the calculator's `/actuator/health` check and the proxy health check to pass.

### Jenkins setup

1. Install the Pipeline, Git, GitHub Branch Source, Workspace Cleanup, and Email Extension plugins.
2. In **Manage Jenkins → Tools**, configure a JDK installation named `JDK21` (Java 21) and a Maven installation named `Maven3`.
3. Add a Jenkins **Username with password** credential with ID `dockerhub-credentials`. Use your Docker Hub username and a Docker Hub access token as the password.
4. Create a **Multibranch Pipeline** job for this GitHub repository. Configure it to discover branches and pull requests, and ensure it loads the `Jenkinsfile` from each branch.
5. Configure the GitHub webhook to notify Jenkins of pushes and pull requests, or configure periodic multibranch indexing as a fallback.
6. Ensure the Jenkins agent can run Docker and Docker Compose v2, and can bind host port 8082. The deployment runs on that agent's Docker host. Jenkins itself can continue using port 8080.
7. Configure SMTP in **Manage Jenkins → System** and set the Jenkins global environment variable `NOTIFICATION_EMAIL` to your email address. The pipeline sends success and failure notifications; failure emails include the build log.
8. In GitHub, protect `main` by requiring pull requests before merge. Once Jenkins status checks are publishing to GitHub, require the Jenkins check before allowing a merge. Keep deployment credentials in Jenkins, never in the repository.

The multibranch pipeline verifies feature branches and pull requests but does not merge them. Review and merge the pull request on GitHub; the resulting `main` build is the only build that pushes and deploys. The deployed app is available at `http://localhost:8082`.
