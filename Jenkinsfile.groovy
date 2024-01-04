pipeline {
    agent {
        label 'DeployServer'
    }

    environment {
        DOCKER_HUB_REGISTRY = 'https://registry.hub.docker.com'
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials-id')
        DOCKER_IMAGE_NAME = 'aliakseiyakovtsov/kicker'
        npm_config_cache = 'npm-cache'
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

        stage('Deploy') {
            steps {
                script {
                    env.remote = [
                        name: 'DeployServer',
                        host: '172.31.45.29',
                        user: 'ubuntu',
                        keyFile: credentials('id_rsa')
                    ]
                    sh '''
                        sudo apt-get update && sudo apt-get install -y openssh-client
                        eval $(ssh-agent -s)
                        cat <<< "$SSH_KEY" | ssh-add -
                        mkdir -p ~/.ssh
                        chmod 700 ~/.ssh
                        ssh-keyscan $SSH_HOST >> ~/.ssh/known_hosts
                        chmod 644 ~/.ssh/known_hosts
                    '''
                    sh '''
                        ssh $SSH_USER@$SSH_HOST "docker rm -f kicker || true"
                        ssh $SSH_USER@$SSH_HOST "docker rmi $DOCKER_IMAGE_NAME || true"
                        ssh $SSH_USER@$SSH_HOST "docker login -u ${DOCKER_LOGIN} -p ${DOCKER_PASS}"
                        ssh $SSH_USER@$SSH_HOST "docker pull $DOCKER_IMAGE_NAME"
                        ssh $SSH_USER@$SSH_HOST "
                            docker run -d --name kicker --restart always \
                            --network kicker-net \
                            -p 8585:8080 \
                            -v $DATA_DIR:/data/ \
                            -e POSTGRES_HOST=$POSTGRES_HOST \
                            -e POSTGRES_DB=$POSTGRES_DB \
                            -e POSTGRES_USER=$POSTGRES_USER \
                            -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
                            -e DOMAINS=$DOMAINS \
                            $DOCKER_IMAGE_NAME
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
