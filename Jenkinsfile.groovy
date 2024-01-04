pipeline {
    agent {
        label 'DeployServer'
    }

    environment {
        DOCKER_HUB_REGISTRY = 'https://registry.hub.docker.com'
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials-id')
        DOCKER_IMAGE_NAME = 'zenk/kicker'
        npm_config_cache = 'npm-cache'
    }

   stage('Login to Docker Hub') {      	
        steps{                       	
        sh 'echo $DOCKER_HUB_CREDENTIALS | sudo docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'                		
        echo 'Login Completed'      
    }           
}