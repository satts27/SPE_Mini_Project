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

## Run with Docker Hub image

The published application image is:

```text
satts27/spe-calculator:latest
```

Run the calculator image directly and map it to port 8081:

```powershell
docker login
docker pull satts27/spe-calculator:latest
docker run --rm -d --name spe-calculator -p 8081:8081 satts27/spe-calculator:latest
```

Verify it:

```powershell
curl http://localhost:8081/actuator/health
curl "http://localhost:8081/api/v1/add?a=2&b=3"
docker stop spe-calculator
```

## Run with Docker Compose

Docker Compose starts the calculator and Nginx reverse proxy and waits for their health checks. To build and run the local source:

```powershell
docker compose up --build -d
docker compose ps
```

To use the Docker Hub image instead of building locally, set `CALCULATOR_IMAGE`:

```powershell
$env:CALCULATOR_IMAGE="satts27/spe-calculator:latest"
docker compose pull calculator
docker compose up -d --no-build
docker compose ps
```

Open `http://localhost:8082` for the frontend and API. Jenkins uses port 8080, while the calculator listens on port 8081 only inside the Compose network. To stop the stack, run `docker compose down`.

## Publish the image to Docker Hub

Use a Docker Hub access token rather than an account password. Set the Docker Hub namespace once; this repository uses `satts27`.

```powershell
docker login -u satts27
docker build -t satts27/spe-calculator:latest .
docker push satts27/spe-calculator:latest
```

Add a build-specific immutable tag before pushing to production:

```powershell
$tag = (Get-Date -Format 'yyyyMMdd-HHmmss')
docker build -t "satts27/spe-calculator:$tag" .
docker push "satts27/spe-calculator:$tag"
```

The tagged and `latest` versions should appear at `https://hub.docker.com/r/satts27/spe-calculator`.

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

## Run Jenkins in Docker

Jenkins itself is containerized, so setup does not depend on a locally installed Jenkins service.

```powershell
docker compose -f docker-compose.jenkins.yml up -d --build
```

Open `http://localhost:8090`, complete the initial administrator setup, install the required plugins below, and create a Pipeline job pointed at this repository. The Jenkins container mounts the Docker Engine socket and has Maven, Docker CLI, and Docker Compose available.

Required plugins: Git, Pipeline, Docker Pipeline, Workspace Cleanup, and Email Extension. Configure the Jenkins credentials described below.

## Jenkins pipeline

The declarative `Jenkinsfile` builds and tests every branch and pull request, archives the executable JAR, and builds a Docker image. Only builds for the `main` branch push the versioned image and `latest` tag to Docker Hub and deploy the stack with Docker Compose. Deployment waits for the calculator's `/actuator/health` check and the proxy health check to pass.

### Jenkins setup

1. Install the Pipeline, Git, GitHub Branch Source, Docker Pipeline, Workspace Cleanup, and Email Extension plugins.
2. The container already provides Java 21, Maven, Docker CLI, and Docker Compose; no local agent tools or host Jenkins installation are required.
3. Add a Jenkins **Username with password** credential with ID `dockerhub-credentials`. Use your Docker Hub username and a Docker Hub access token as the password.
4. Create a **Multibranch Pipeline** job for this GitHub repository. Configure it to discover branches and pull requests, and ensure it loads the `Jenkinsfile` from each branch.
5. Configure the GitHub webhook to notify Jenkins of pushes and pull requests, or configure periodic multibranch indexing as a fallback.
6. Ensure the Jenkins agent can run Docker and Docker Compose v2, and can bind host port 8082. The deployment runs on that agent's Docker host. Jenkins itself can continue using port 8080.
7. Configure Jenkins SMTP so it can send mail:
   - In **Manage Jenkins → System → Extended E-mail Notification**, set SMTP server `smtp.gmail.com`, SMTP port `465`, enable SSL, and add your Gmail username and app password.
   - In the Jenkins Mailer section, enter the same Gmail address and configure `smtp.gmail.com` with port `465` and SSL.
   - Create a Gmail **App Password** from Google's account security page. Do not use your normal Gmail password or store the password in Git.
   - The pipeline sends success and failure notifications to `suryasatishganiga@gmail.com` by default. An optional Jenkins environment variable named `NOTIFICATION_EMAIL` can override that address.
8. In GitHub, protect `main` by requiring pull requests before merge. Once Jenkins status checks are publishing to GitHub, require the Jenkins check before allowing a merge. Keep deployment credentials in Jenkins, never in the repository.

The multibranch pipeline verifies feature branches and pull requests but does not merge them. Review and merge the pull request on GitHub; the resulting `main` build is the only build that pushes and deploys. The deployed app is available at `http://localhost:8082`.

## Capture the Jenkins success screenshot

After starting a build:

1. Open the build page in Jenkins at **Dashboard → SPE_Mini_Project → main → Build History → green build number**.
2. Select **Stage View** to show the green pipeline stages, or open **Console Output** for the full successful log.
3. Press `Windows + Shift + S`, drag over the Jenkins window, and save the PNG through the Snipping Tool.
4. Include the build number and each stage in the screenshot. A successful pipeline run may show all stages in green or blue depending on the Jenkins theme.
5. Keep a second screenshot of the received email from `Suryasatishganiga@gmail.com`'s inbox, using the Jenkins build subject and number as evidence.
