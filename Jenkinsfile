pipeline {
    agent any

    environment {
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub_credential')
        DOCKER_IMAGE = 'iskxcr/teedy-app'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        DEPLOYMENT_NAME = "hello-node"
        CONTAINER_NAME = "docs"
        IMAGE_NAME = "iskxcr/teedy-app:latest"
    }

    stages {
        stage('Start Minikube') {
            steps {
                sh '''
                    if ! minikube status | grep -q "Running"; then
                    echo "Starting Minikube..."
                    minikube start
                    else
                    echo "Minikube already running."
                    fi
                '''
            }
        }

        // stage('Create deployment') {
        //     steps {
        //         sh '''
        //             kubectl create deployment ${DEPLOYMENT_NAME} --image=${DOCKER_IMAGE}:${DOCKER_TAG}
        //         '''
        //     }
        // }

        stage('Set Image') {
            steps {
                sh '''
                    echo "Setting image for deployment..."
                    kubectl set image deployments/${DEPLOYMENT_NAME} ${CONTAINER_NAME}=${IMAGE_NAME}
                '''
            }
        }

        stage('Verify') {
            steps {
                // sh 'kubectl rollout status deployments/${DEPLOYMENT_NAME}'
                sh 'kubectl get pods'
            }
        }

        // stage('Expose') {
            // steps {
                // sh 'kubectl expose deployments/${DEPLOYMENT_NAME} --type=LoadBalancer --port=8080'
            // }
        // }
    }
}