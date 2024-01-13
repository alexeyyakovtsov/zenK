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
        stage('Clean Workspace') {
            steps {
                script {
                    sh 'sudo rm -rf $WORKSPACE/*'
                }
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    docker.image('openjdk:11-jdk').inside('-v /var/run/docker.sock:/var/run/docker.sock') {
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
                    sleep(time: 40, unit: 'SECONDS')
                    def responseCode = sh(script: 'curl -s -o /dev/null -w %{http_code} http://localhost:8585', returnStatus: true).trim()

                    if (responseCode == '200') {
                        echo 'Smoke Test Passed!'
                    } else {
                        error 'Smoke Test Failed!'
                    }
                }
            }
        }
    }
        
    post {
        always {
            cleanWs()
        }
    }
}
