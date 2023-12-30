pipeline {
    agent any

    environment {
        DOCKER_DRIVER = 'overlay2'
        IMAGE_NAME = 'zensoftio/kicker'
        CONTAINER_NAME = 'kicker'
        CONTAINER_PORT = '8585'
        DOMAINS = 'zensoft.io,zensoft.by,zensoft.kg'
    }

    stages {
        stage('Build') {
            steps {
                script {
                    // Этот этап повторяет build-jar из вашего GitLab CI/CD
                    sh 'docker build -t my-backend:latest -f Dockerfile .'
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    // Этот этап повторяет package-docker из вашего GitLab CI/CD
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials-id', usernameVariable: 'DOCKER_LOGIN', passwordVariable: 'DOCKER_PASS')]) {
                        sh 'docker login -u $DOCKER_LOGIN -p $DOCKER_PASS'
                        sh 'docker build -t $IMAGE_NAME:latest .'
                        sh 'docker push $IMAGE_NAME:latest'
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    // Этот этап повторяет deploy из вашего GitLab CI/CD
                    withCredentials([sshUserPrivateKey(credentialsId: 'ssh-key-credentials-id', keyFileVariable: 'SSH_KEY', passphraseVariable: '', usernameVariable: 'SSH_USER')]) {
                        sh '''
                            ssh $SSH_USER@$SSH_HOST "docker rm -f $CONTAINER_NAME || true"
                            ssh $SSH_USER@$SSH_HOST "docker rmi $IMAGE_NAME || true"
                            ssh $SSH_USER@$SSH_HOST "docker login -u $DOCKER_LOGIN -p $DOCKER_PASS"
                            ssh $SSH_USER@$SSH_HOST "docker pull $IMAGE_NAME"
                            ssh $SSH_USER@$SSH_HOST "docker run -d --name $CONTAINER_NAME --restart always \
                                --network kicker-net \
                                -p $CONTAINER_PORT:8080 \
                                -v $DATA_DIR:/data/ \
                                -e POSTGRES_HOST=$POSTGRES_HOST \
                                -e POSTGRES_DB=$POSTGRES_DB \
                                -e POSTGRES_USER=$POSTGRES_USER \
                                -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
                                -e DOMAINS=$DOMAINS \
                                $IMAGE_NAME"
                        '''
                    }
                }
            }
        }
    }
}
