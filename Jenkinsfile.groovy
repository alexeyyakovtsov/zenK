pipeline {
    agent {
        label 'DeployServer'
    }

    environment {
        SSH_USER = credentials('SSH_USER')
        SSH_HOST = credentials('SSH_HOST')
        DOCKER_HUB_REGISTRY = credentials('DOCKER_HUB_REGISTRY')
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials-id')
        DOCKER_IMAGE_NAME = 'aliakseiyakovtsov/kicker'
        DOCKER_NETWORK = credentials('DOCKER_NETWORK')
        npm_config_cache = 'npm-cache'
        POSTGRES_HOST = credentials('POSTGRES_HOST')
        POSTGRES_DB = credentials('POSTGRES_DB')
        POSTGRES_USER = credentials('POSTGRES_USER')
        POSTGRES_PASSWORD = credentials('POSTGRES_PASSWORD')
        DATA_DIR = "./data"
        DOMAINS = ""
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
                    docker.image('openjdk:11-jdk').inside('-v /var/run/docker.sock:/var/run/docker.sock') {
                        sh './gradlew clean build --refresh-dependencies'
                        sh './gradlew build'
                    }
                }
            }
        }

        stage('Package and Push to Docker Hub') {
            steps {
                script {
                def dockerImage = docker.image("${DOCKER_IMAGE_NAME}")
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials-id', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                    sh "echo $DOCKER_PASSWORD | docker login --username $DOCKER_USERNAME --password-stdin $DOCKER_HUB_REGISTRY"
                    sh "docker build -t ${DOCKER_IMAGE_NAME} ."
                    sh "docker push ${DOCKER_IMAGE_NAME}"
                }
            }
            }
        }

        stage('Execute Remote Docker Commands') {
            steps {
                sshagent(credentials: ['ubuntu']) {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials-id', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                    sh '''
                        docker-compose down || true
                        docker-compose pull
                        docker-compose up -d
                        '''
                        }
                    }
                }
            }
        }

        stage('Smoke Test') {
            steps {
                script {
                    sleep(time: 20, unit: 'SECONDS')
                    def response = sh(script: 'curl -I http://localhost:8585', returnStdout: true).trim()
                    echo "Response from curl: ${response}"

                    def statusLine = response =~ /HTTP\/1\.1 (\d+)/
                    if (statusLine) {
                        def statusCode = statusLine[0][1] as Integer
                        if (statusCode == 200) {
                            echo "Smoke test passed successfully"
                        } else {
                            error "Smoke test failed: HTTP response code is not 200"
                        }
                    } else {
                        error "Failed to parse HTTP status from response"
                    }
                }
            }
        }
    }
        
    post {
        always {
            sh 'rm -rf workspace/*ZenK*'
        }
    }
}
