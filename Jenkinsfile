pipeline {
    agent any

    environment {
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub_credential')
        DOCKER_IMAGE = 'iskxcr/teedy-app'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Build') {
            steps {
                checkout scmGit(
                     branches: [[name: '*/master']],
                     extensions: [],
                     userRemoteConfigs: [[
                        credentialsId: 'c3f515e7-3c75-4a94-839b-f7e058b23424',
                        url: 'https://github.com/IskXCr/Teedy.git'
                     ]]
                )
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage('Building image') {
            steps {
                 script {
                     docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                 }
            }
        }

        stage('Upload image') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub_credential') {
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push()
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push('latest')
                    }
                }
            }
        }

        stage('Run containers') {
            steps {
                script {
                    sh 'docker stop teedy-container-8081 || true'
                    sh 'docker rm teedy-container-8081 || true'

                    docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run(
                        '--name teedy-container-8081 -d -p 8081:8080'
                    )

                    sh 'docker ps --filter "name=teedy-container"'
                }
            }
        }
    }
}