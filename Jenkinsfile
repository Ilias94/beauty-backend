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
        K8S_SERVICE = "beauty-service"
        KIND_NODE = "desktop-control-plane"
        // The Jenkins service runs as LocalSystem, whose profile has no kubeconfig;
        // Docker Desktop writes the docker-desktop context to the user's profile only.
        KUBECONFIG = "C:\\Users\\ilbel\\.kube\\config"
        // The credential the job already uses to check out from GitHub (Ilias94 + token).
        // The Release stage also pushes commits and the tag with it, so it needs write access.
        GIT_CREDENTIALS_ID = "5fec19ce-b42c-4aa8-9b1f-eac8eadbeca6" // gitleaks:allow (an ID, not the token)
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

        stage('Secret Scan') {
            // Scans the checked-out tree (not git history) with gitleaks, before anything
            // is built or deployed. A finding fails the build; false positives go in
            // .gitleaks.toml or get an inline gitleaks:allow comment.
            steps {
                bat 'docker run --rm -v "%WORKSPACE%:/repo" zricethezav/gitleaks:v8.21.2 dir /repo --config /repo/.gitleaks.toml --redact --no-banner --report-format json --report-path /repo/gitleaks-report.json'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'gitleaks-report.json', allowEmptyArchive: true
                }
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
                    branch 'master'
                    expression { params.RELEASE }
                }
            }
            steps {
                bat 'mvn -B package -DskipTests'
                // A step rather than post { success }: that condition looks at the whole
                // build, so an UNSTABLE Secret Scan would silently skip archiving the jar.
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Docker Build') {
            when {
                anyOf {
                    branch 'master'
                    expression { params.RELEASE }
                }
            }
            steps {
                // Built directly against the Docker Desktop daemon; the image
                // stays local, no push step is needed.
                bat "docker build -t ${DOCKER_IMAGE}:${IMAGE_TAG} -t ${DOCKER_IMAGE}:v1 ."
                // Docker Desktop runs Kubernetes as a kind cluster, whose node has its own
                // containerd image store: without this, pods fail with ErrImageNeverPull.
                bat "docker save -o kind-image.tar ${DOCKER_IMAGE}:${IMAGE_TAG} ${DOCKER_IMAGE}:v1"
                bat "docker cp kind-image.tar ${KIND_NODE}:/kind-image.tar"
                bat "docker exec ${KIND_NODE} ctr -n k8s.io images import /kind-image.tar"
                bat "docker exec ${KIND_NODE} rm -f /kind-image.tar"
            }
        }

        stage('Deploy to Docker Desktop k8s') {
            when {
                anyOf {
                    branch 'master'
                    expression { params.RELEASE }
                }
            }
            steps {
                // One command per bat step: a multi-line bat script only reports
                // the exit code of its last command, so earlier failures would be hidden.
                bat 'kubectl config use-context docker-desktop'
                bat "kubectl set image deployment/${K8S_DEPLOYMENT} ${K8S_CONTAINER}=${DOCKER_IMAGE}:${IMAGE_TAG} --record"
                // From here on a failure means the new version is (partly) live, so post
                // rolls it back. Before this point the cluster is untouched.
                script { env.DEPLOY_STARTED = 'true' }
                bat "kubectl rollout status deployment/${K8S_DEPLOYMENT} --timeout=900s"
                // Smoke test from inside the cluster, through the Service, so it checks the
                // same path real traffic takes. Readiness only turns UP after startup
                // (Flyway, DB connection) has finished.
                bat "kubectl run smoke-test-${env.BUILD_NUMBER} --rm -i --restart=Never --image=curlimages/curl:8.10.1 --command -- curl -fsS --retry 10 --retry-delay 3 --retry-all-errors http://${K8S_SERVICE}:8080/actuator/health/readiness"
            }
            post {
                failure {
                    script {
                        if (env.DEPLOY_STARTED == 'true') {
                            echo "Deploy or smoke test failed, rolling back deployment/${K8S_DEPLOYMENT}"
                            bat "kubectl rollout undo deployment/${K8S_DEPLOYMENT}"
                            bat "kubectl rollout status deployment/${K8S_DEPLOYMENT} --timeout=900s"
                        }
                    }
                }
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
