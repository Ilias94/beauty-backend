pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'jdk'
    }

    environment {
        // Local-only image: Kubernetes in Docker Desktop reads straight from the
        // local Docker daemon (imagePullPolicy: Never in k8s/app-deployment.yaml),
        // so there is no registry and no credentials to configure.
        DOCKER_IMAGE = "docker-app/beauty"
        K8S_DEPLOYMENT = "beauty"
        K8S_CONTAINER = "beauty"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {

        stage('Checkout') {
            steps {
                // This Jenkinsfile lives at the root of the beauty-backend repo,
                // so checkout scm puts pom.xml directly in the workspace root.
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -B clean compile'
            }
        }

        stage('Test') {
            // Tests rely on TestContainers spinning up real PostgreSQL,
            // so the Jenkins agent needs Docker available.
            steps {
                bat 'mvn -B test'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Package') {
            when {
                branch 'main'
            }
            steps {
                bat 'mvn -B package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Docker Build') {
            when {
                branch 'main'
            }
            steps {
                // Built directly against the Docker Desktop daemon; the image
                // stays local, no push step is needed.
                bat "docker build -t ${DOCKER_IMAGE}:${env.BUILD_NUMBER} -t ${DOCKER_IMAGE}:v1 ."
            }
        }

        stage('Deploy to Docker Desktop k8s') {
            when {
                branch 'main'
            }
            steps {
                // One command per bat step: a multi-line bat script only reports
                // the exit code of its last command, so earlier failures would be hidden.
                bat 'kubectl config use-context docker-desktop'
                bat "kubectl set image deployment/${K8S_DEPLOYMENT} ${K8S_CONTAINER}=${DOCKER_IMAGE}:${env.BUILD_NUMBER} --record"
                bat "kubectl rollout status deployment/${K8S_DEPLOYMENT} --timeout=120s"
            }
        }
    }

    post {
        failure {
            echo "Build failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        }
        cleanup {
            cleanWs()
        }
    }
}
