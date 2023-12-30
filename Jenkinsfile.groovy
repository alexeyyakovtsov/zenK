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
                    container('openjdk:8-jdk') {
                        script {
                            sh 'export GRADLE_USER_HOME=`pwd`/.gradle'
                            sh './gradlew build'
                        }
                    }
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials-id', usernameVariable: 'DOCKER_LOGIN', passwordVariable: 'DOCKER_PASS')]) {
                        script {
                            if (env.CI_COMMIT_REF_NAME == 'master') {
                                env.IMAGE_TAG = 'latest'
                            } else {
                                env.IMAGE_TAG = env.CI_COMMIT_REF_NAME
                            }
                            sh 'docker login -u $DOCKER_LOGIN -p $DOCKER_PASS'
                            sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG .'
                            sh 'docker push $IMAGE_NAME:$IMAGE_TAG'
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
                            ssh $SSH_USER@$SSH_HOST "docker pull $IMAGE_NAME"
                            ssh $SSH_USER@$SSH_HOST "
                                docker run -d --name $CONTAINER_NAME --restart always \
                                  --network kicker-net \
                                  -p $CONTAINER_PORT:8080 \
                                  -v $DATA_DIR:/data/ \
                                  -e POSTGRES_HOST=$POSTGRES_HOST \
                                  -e POSTGRES_DB=$POSTGRES_DB \
                                  -e POSTGRES_USER=$POSTGRES_USER \
                                  -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
                                  -e DOMAINS=$DOMAINS \
                                  $IMAGE_NAME
                            "
                        '''
                    }
                }
            }
        }
    }
}
