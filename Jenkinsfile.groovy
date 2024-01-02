pipeline {
    agent {
        docker {
            image 'docker:latest'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    environment {
        DOCKER_DRIVER = 'overlay2'
        IMAGE_NAME = 'zensoftio/kicker'
    }

    stages {
        stage('Build') {
            steps {
                script {
                    sh 'docker run --rm -v $PWD:/app -w /app openjdk:8-jdk ./gradlew build'
                }
            }
        }

        stage('Package') {
            steps {
                script {
            def imageTag = env.BRANCH_NAME == 'master' ? 'latest' : env.BRANCH_NAME
            withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials-id', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                sh "docker login -u \$DOCKER_USERNAME -p \$DOCKER_PASSWORD"
            }
            sh "docker build -t \${IMAGE_NAME}:${imageTag} ."
            sh "docker push \${IMAGE_NAME}:${imageTag}"
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
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
            }
        }
    }
}
