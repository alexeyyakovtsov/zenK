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
        stage('Checkout') {
            steps {
                script {
                    checkout([$class: 'GitSCM', branches: [[name: '*/main']], userRemoteConfigs: [[credentialsId: 'github-credentials-id', url: 'https://github.com/alexeyyakovtsov/zenK']]])
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    docker.build IMAGE_NAME, '-f Dockerfile .'
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    docker.image(IMAGE_NAME).inside {
                        sh './gradlew test --no-daemon'
                    }
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials-id', usernameVariable: 'DOCKER_LOGIN', passwordVariable: 'DOCKER_PASS')]) {
                        docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials-id') {
                            sh "docker tag $IMAGE_NAME:latest $DOCKER_LOGIN/$IMAGE_NAME:latest"
                            sh 'docker push $DOCKER_LOGIN/$IMAGE_NAME:latest'
                        }
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    withCredentials([sshUserPrivateKey(credentialsId: 'ssh-key-credentials-id', keyFileVariable: 'SSH_KEY', passphraseVariable: '', usernameVariable: 'SSH_USER')]) {
                        sh '''
                            ssh $SSH_USER@$SSH_HOST "docker rm -f $CONTAINER_NAME || true"
                            ssh $SSH_USER@$SSH_HOST "docker rmi $IMAGE_NAME || true"
                            ssh $SSH_USER@$SSH_HOST "docker login -u $DOCKER_LOGIN -p $DOCKER_PASS"
                            ssh $SSH_USER@$SSH_HOST "docker pull $DOCKER_LOGIN/$IMAGE_NAME:latest"
                            ssh $SSH_USER@$SSH_HOST "docker run -d --name $CONTAINER_NAME --restart always \
                                --network kicker-net \
                                -p $CONTAINER_PORT:8080 \
                                -v $DATA_DIR:/data/ \
                                -e POSTGRES_HOST=$POSTGRES_HOST \
                                -e POSTGRES_DB=$POSTGRES_DB \
                                -e POSTGRES_USER=$POSTGRES_USER \
                                -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
                                -e DOMAINS=$DOMAINS \
                                $DOCKER_LOGIN/$IMAGE_NAME:latest"
                        '''
                    }
                }
            }
        }
    }
}
