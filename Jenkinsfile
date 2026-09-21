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
        // Jenkins "Username with password" credential (GitHub user + personal access
        // token with repo scope). Only used by the Release stage to push commits and the tag.
        GIT_CREDENTIALS_ID = "github-credentials"
        // Release builds tag the image with the released version, regular builds with the build number.
        IMAGE_TAG = "${params.RELEASE ? params.RELEASE_VERSION : env.BUILD_NUMBER}"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    parameters {
        booleanParam(
            name: 'RELEASE',
            defaultValue: false,
            description: 'Cut a release: set RELEASE_VERSION in pom.xml, tag it in git, move the branch to the next -SNAPSHOT, then package, build the image and deploy the released version.'
        )
        string(
            name: 'RELEASE_VERSION',
            defaultValue: '',
            description: 'Version to release, e.g. 1.0.0. Required when RELEASE is checked; the next development version is derived from it (1.0.0 -> 1.0.1-SNAPSHOT).'
        )
    }

    stages {

        stage('Checkout') {
            steps {
                // This Jenkinsfile lives at the root of the beauty-backend repo,
                // so checkout scm puts pom.xml directly in the workspace root.
                checkout scm
            }
        }

        stage('Validate Release') {
            // Fail before the slow Build and Test stages when the release input is wrong.
            when {
                expression { params.RELEASE }
            }
            steps {
                script {
                    // Strict x.y.z: the value ends up on a command line and in a git tag.
                    if (!((params.RELEASE_VERSION ?: '') ==~ /\d+\.\d+\.\d+/)) {
                        error("RELEASE_VERSION must look like 1.2.3, got '${params.RELEASE_VERSION}'")
                    }
                    def branch = env.BRANCH_NAME ?: env.GIT_BRANCH?.replaceFirst('^origin/', '')
                    if (!(branch in ['main', 'master'])) {
                        error("Releases can only be cut from main or master, current branch: ${branch}")
                    }
                    env.RELEASE_BRANCH = branch
                }
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

        stage('Release') {
            when {
                expression { params.RELEASE }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: env.GIT_CREDENTIALS_ID, usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
                    // One command per bat step, see the Deploy stage. checkout scm leaves a
                    // detached HEAD, but release:prepare has to push to a branch.
                    bat "git checkout -B ${env.RELEASE_BRANCH}"
                    bat 'git config user.name "Jenkins"'
                    bat 'git config user.email "jenkins@localhost"'
                    // Commits the release version, tags it (tagNameFormat in pom.xml), commits the
                    // next SNAPSHOT and pushes all of it. Tests already ran in the Test stage.
                    bat "mvn -B release:prepare -DreleaseVersion=${params.RELEASE_VERSION} -Dusername=%GIT_USER% -Dpassword=%GIT_TOKEN% -Darguments=-DskipTests"
                }
                // prepare leaves the branch on the next SNAPSHOT commit; the stages below
                // have to build the release. Keep v in sync with tagNameFormat in pom.xml.
                bat "git checkout v${params.RELEASE_VERSION}"
            }
        }

        stage('Package') {
            when {
                anyOf {
                    branch 'main'
                    expression { params.RELEASE }
                }
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
                anyOf {
                    branch 'main'
                    expression { params.RELEASE }
                }
            }
            steps {
                // Built directly against the Docker Desktop daemon; the image
                // stays local, no push step is needed.
                bat "docker build -t ${DOCKER_IMAGE}:${IMAGE_TAG} -t ${DOCKER_IMAGE}:v1 ."
            }
        }

        stage('Deploy to Docker Desktop k8s') {
            when {
                anyOf {
                    branch 'main'
                    expression { params.RELEASE }
                }
            }
            steps {
                // One command per bat step: a multi-line bat script only reports
                // the exit code of its last command, so earlier failures would be hidden.
                bat 'kubectl config use-context docker-desktop'
                bat "kubectl set image deployment/${K8S_DEPLOYMENT} ${K8S_CONTAINER}=${DOCKER_IMAGE}:${IMAGE_TAG} --record"
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
