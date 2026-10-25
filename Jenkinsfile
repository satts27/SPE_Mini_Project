def runOnAgent(String unixCommand, String windowsCommand) {
    if (isUnix()) {
        sh unixCommand
    } else {
        bat windowsCommand
    }
}

def sendBuildNotification(String result) {
    if (!(env.NOTIFICATION_EMAIL ?: '').trim()) {
        echo 'NOTIFICATION_EMAIL is not configured; build email was skipped.'
        return
    }

    emailext(
        to: env.NOTIFICATION_EMAIL,
        subject: "${env.JOB_NAME} #${env.BUILD_NUMBER}: ${result}",
        body: "${env.JOB_NAME} build #${env.BUILD_NUMBER} finished with ${result}.\n${env.BUILD_URL}",
        attachLog: result == 'FAILURE'
    )
}

pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven3'
    }

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        timestamps()
    }

    environment {
        DOCKERHUB_REPOSITORY = 'satts27/spe-calculator'
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-credentials'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    runOnAgent('mvn -B -DskipTests clean compile', 'mvn -B -DskipTests clean compile')
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    runOnAgent('mvn -B test', 'mvn -B test')
                }
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    runOnAgent('mvn -B -DskipTests package', 'mvn -B -DskipTests package')
                }
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    def branchTag = (env.BRANCH_NAME ?: 'local').replaceAll(/[^A-Za-z0-9_.-]/, '-')
                    env.IMAGE_TAG = "${branchTag}-${env.BUILD_NUMBER}"
                }
                script {
                    runOnAgent(
                        'docker build --pull --tag "$DOCKERHUB_REPOSITORY:$IMAGE_TAG" .',
                        'docker build --pull --tag "%DOCKERHUB_REPOSITORY%:%IMAGE_TAG%" .'
                    )
                    runOnAgent('docker compose build proxy', 'docker compose build proxy')
                }
            }
        }

        stage('Push') {
            when {
                branch 'main'
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: env.DOCKERHUB_CREDENTIALS_ID,
                    usernameVariable: 'DOCKERHUB_USERNAME',
                    passwordVariable: 'DOCKERHUB_TOKEN'
                )]) {
                    script {
                        runOnAgent(
                            '''
                                set -eu
                                trap 'docker logout >/dev/null 2>&1 || true' EXIT
                                printf '%s' "$DOCKERHUB_TOKEN" | docker login --username "$DOCKERHUB_USERNAME" --password-stdin
                                docker push "$DOCKERHUB_REPOSITORY:$IMAGE_TAG"
                                docker tag "$DOCKERHUB_REPOSITORY:$IMAGE_TAG" "$DOCKERHUB_REPOSITORY:latest"
                                docker push "$DOCKERHUB_REPOSITORY:latest"
                            ''',
                            '''
                                @echo off
                                powershell.exe -NoProfile -NonInteractive -Command "$ErrorActionPreference='Stop'; try { $env:DOCKERHUB_TOKEN | docker login --username $env:DOCKERHUB_USERNAME --password-stdin; if ($LASTEXITCODE -ne 0) { throw 'Docker login failed.' }; docker push $env:DOCKERHUB_REPOSITORY`:$env:IMAGE_TAG; if ($LASTEXITCODE -ne 0) { throw 'Docker image push failed.' }; docker tag $env:DOCKERHUB_REPOSITORY`:$env:IMAGE_TAG $env:DOCKERHUB_REPOSITORY`:latest; if ($LASTEXITCODE -ne 0) { throw 'Docker image tagging failed.' }; docker push $env:DOCKERHUB_REPOSITORY`:latest; if ($LASTEXITCODE -ne 0) { throw 'Docker latest tag push failed.' } } finally { docker logout | Out-Null }"
                            '''
                        )
                    }
                }
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                withEnv(["CALCULATOR_IMAGE=${env.DOCKERHUB_REPOSITORY}:${env.IMAGE_TAG}"]) {
                    script {
                        runOnAgent('docker compose build proxy', 'docker compose build proxy')
                        runOnAgent(
                            'docker compose pull calculator',
                            'docker compose pull calculator'
                        )
                        runOnAgent(
                            'docker compose up --detach --no-build --remove-orphans --wait --wait-timeout 120',
                            'docker compose up --detach --no-build --remove-orphans --wait --wait-timeout 120'
                        )
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                sendBuildNotification('SUCCESS')
            }
        }
        failure {
            script {
                sendBuildNotification('FAILURE')
            }
        }
        always {
            cleanWs()
        }
    }
}
