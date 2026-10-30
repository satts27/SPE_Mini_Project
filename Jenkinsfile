pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
    }

    environment {
        DOCKERHUB_REPOSITORY = 'satts27/spe-calculator'
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-credentials'
        NOTIFICATION_EMAIL = 'suryasatishganiga@gmail.com'
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Build') {
            steps { sh 'mvn -B -DskipTests clean package' }
        }

        stage('Test') {
            steps { sh 'mvn -B test' }
            post { always { junit testResults: 'target/surefire-reports/*.xml' } }
        }

        stage('Package') {
            steps {
                sh 'mvn -B -DskipTests package'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    def safeBranch = (env.BRANCH_NAME ?: 'local').replaceAll(/[^A-Za-z0-9_.-]/, '-')
                    env.IMAGE_TAG = "${safeBranch}-${env.BUILD_NUMBER}"
                }
                sh 'docker build --pull --tag "$DOCKERHUB_REPOSITORY:$IMAGE_TAG" .'
                sh 'docker compose build proxy'
            }
        }

        stage('Push') {
            when { branch 'main' }
            steps {
                withCredentials([usernamePassword(credentialsId: env.DOCKERHUB_CREDENTIALS_ID,
                                                  usernameVariable: 'DOCKERHUB_USERNAME',
                                                  passwordVariable: 'DOCKERHUB_TOKEN')]) {
                    sh '''
                        trap 'docker logout >/dev/null 2>&1 || true' EXIT
                        printf '%s' "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin
                        docker push "$DOCKERHUB_REPOSITORY:$IMAGE_TAG"
                        docker tag "$DOCKERHUB_REPOSITORY:$IMAGE_TAG" "$DOCKERHUB_REPOSITORY:latest"
                        docker push "$DOCKERHUB_REPOSITORY:latest"
                    '''
                }
            }
        }

        stage('Deploy') {
            when { branch 'main' }
            steps {
                withEnv(["CALCULATOR_IMAGE=${DOCKERHUB_REPOSITORY}:${env.IMAGE_TAG}"]) {
                    sh 'docker compose pull calculator || true'
                    sh 'docker compose up -d --no-build --remove-orphans'
                    sh 'docker compose up --wait --wait-timeout 120'
                }
            }
        }
    }

    post {
        success {
            catchError(buildResult: null, stageResult: 'SUCCESS') {
                emailext(
                    to: "${env.NOTIFICATION_EMAIL}",
                    subject: "${env.JOB_NAME} #${env.BUILD_NUMBER}: SUCCESS",
                    body: "Build: ${env.JOB_NAME}\nBuild Number: ${env.BUILD_NUMBER}\nStatus: SUCCESS\nBuild URL: ${env.BUILD_URL}",
                    attachLog: false
                )
            }
        }
        failure {
            catchError(buildResult: null, stageResult: 'SUCCESS') {
                emailext(
                    to: "${env.NOTIFICATION_EMAIL}",
                    subject: "${env.JOB_NAME} #${env.BUILD_NUMBER}: FAILURE",
                    body: "Build: ${env.JOB_NAME}\nBuild Number: ${env.BUILD_NUMBER}\nStatus: FAILURE\nBuild URL: ${env.BUILD_URL}",
                    attachLog: false
                )
            }
        }
        always {
            echo 'Build workspace cleanup is handled by the Jenkins workspace retention policy.'
        }
    }
}
