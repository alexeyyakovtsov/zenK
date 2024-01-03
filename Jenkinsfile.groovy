pipeline {
    agent {
        label 'DeployServer'
    }

    environment {
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials-id')
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
                   def imageTag = env.BRANCH_NAME ?: 'latest'
                    docker.image("zenK/kicker:${imageTag}")
                        .withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials-id') {
                            docker.image("zenK/kicker:${imageTag}")
                                .push()
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
                        apk add --no-cache openssh-client
                        eval $(ssh-agent -s)
                        echo "$SSH_KEY" | tr -d '\r' | ssh-add - > /dev/null
                        mkdir -p ~/.ssh
                        chmod 700 ~/.ssh
                        ssh-keyscan $SSH_HOST >> ~/.ssh/known_hosts
                        chmod 644 ~/.ssh/known_hosts
                    '''
                    sh '''
                        ssh $SSH_USER@$SSH_HOST "docker rm -f kicker || true"
                        ssh $SSH_USER@$SSH_HOST "docker rmi $IMAGE_NAME || true"
                        ssh $SSH_USER@$SSH_HOST "docker login -u \${DOCKER_LOGIN} -p \${DOCKER_PASS}"
                        ssh $SSH_USER@$SSH_HOST "docker pull $IMAGE_NAME"
                        ssh $SSH_USER@$SSH_HOST "
                            docker run -d --name kicker --restart always \
                            --network kicker-net \
                            -p 8585:8080 \
                            -v $DATA_DIR:/data/ \
                            -e POSTGRES_HOST=$POSTGRES_HOST \
                            -e POSTGRES_DB=$POSTGRES_DB \
                            -e POSTGRES_USER=$POSTGRES_USER \
                            -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
                            -e DOMAINS=zensoft.io,zensoft.by,zensoft.kg \
                            $IMAGE_NAME
                        "
                    '''
                }
                script {
                    // Дополнительный шаг для выполнения команд на Deploy сервере
                    sshCommand remote: env.remote,
                        command: '''
                            # Ваша команда для выполнения на Deploy сервере
                            # Например, это может быть команда для перезапуска Docker-compose
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
