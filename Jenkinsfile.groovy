pipeline {
    agent any

    environment {
        // Укажите необходимые переменные окружения
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials-id')
    }

    stages {
        stage('Checkout') {
            steps {
                // Получаем исходный код из репозитория
                checkout scm
            }
        }

        stage('Build and Push Docker Images') {
            steps {
                script {
                    // Сборка и отправка образа бэкенда
                    docker.build("my-backend:latest", "-f backend/Dockerfile .")
                    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials-id') {
                        docker.image("my-backend:latest").push()
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                // Развертывание в Kubernetes (предполагается наличие deployment.yaml)
                script {
                    sh 'kubectl apply -f kubernetes/deployment.yaml'
                }
            }
        }
    }

    post {
        always {
            // Выполняется всегда, независимо от результата
            // Например, можно здесь почистить ресурсы
        }
        success {
            // Выполняется только при успешном завершении
            // Например, отправка уведомления о успешном развертывании
        }
        failure {
            // Выполняется только при неудачном завершении
            // Например, отправка уведомления о провале
        }
    }
}