pipeline {
    agent {
        label 'DeployServer'
    }

    environment {
        SSH_USER = 'ubuntu'
        SSH_HOST = '172.31.45.29'
        DOCKER_HUB_REGISTRY = 'https://registry.hub.docker.com'
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials-id')
        DOCKER_IMAGE_NAME = 'aliakseiyakovtsov/kicker'
        DOCKER_NETWORK = 'kicker-net'
        npm_config_cache = 'npm-cache'
        POSTGRES_HOST = 'db'
        POSTGRES_DB="kicker"
        POSTGRES_USER="kicker"
        POSTGRES_PASSWORD="&d5yNc6FkoB0"
        DOMAINS = ''
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
                script {
                    sh '''
                        ssh $SSH_USER@$SSH_HOST "
                            docker rm -f kicker || true
                            docker rmi $DOCKER_IMAGE_NAME || true
                            docker login -u ${DOCKER_LOGIN} -p ${DOCKER_PASS}
                            docker pull $DOCKER_IMAGE_NAME
                            docker run -d --name kicker --restart always \
                                --network $DOCKER_NETWORK \
                                -p 8585:8080 \
                                -v $DATA_DIR:/data/ \
                                -e POSTGRES_HOST=$POSTGRES_HOST \
                                -e POSTGRES_DB=$POSTGRES_DB \
                                -e POSTGRES_USER=$POSTGRES_USER \
                                -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
                                -e DOMAINS=$DOMAINS \
                                $DOCKER_IMAGE_NAME
                        "
                    '''
                }
                script {
                    sshCommand remote: env.remote,
                        command: '''
                            docker-compose -f docker-compose.yml up -d
                        '''
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
