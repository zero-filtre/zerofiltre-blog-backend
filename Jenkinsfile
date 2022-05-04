def label = "worker-${UUID.randomUUID().toString()}"

podTemplate(label: label, containers: [
        containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true),
        containerTemplate(name: 'kubectl', image: 'roffe/kubectl', command: 'cat', ttyEnabled: true),
        containerTemplate(name: 'maven', image: 'maven:3.8.4-openjdk-11', command: 'cat', ttyEnabled: true)
],
        volumes: [
                hostPathVolume(mountPath: '/root/.m2', hostPath: '/home/jenkins/.m2'),
                hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')
        ]) {
    node(label) {
        try {
            stage('Checkout') {
                checkout scm
            }

            stage('Build with tests') {
                container('maven') {
                    sh "mvn clean package "
                }
            }

            if (getEnvName(env.BRANCH_NAME) == 'uat' || getEnvName(env.BRANCH_NAME) == 'prod') {
                stage('Integration tests') {
                    container('maven') {
                        sh "mvn failsafe:integration-test "
                    }
                }

            }

            stage('Sonarqube Analysis') {
                withSonarQubeEnv('SonarQubeServer') {
                    container('maven') {
                        sh " mvn sonar:sonar -s .m2/settings.xml -Dintegration-tests.skip=true -Dmaven.test.failure.ignore=true"
                    }
                }
                timeout(time: 1, unit: 'MINUTES') {
                    def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
                    if (qg.status != 'OK') {
                        error "Pipeline aborted due to quality gate failure: ${qg.status}"
                    }
                }
            }


            withEnv(["api_image_tag=${getTag(env.BUILD_NUMBER, env.BRANCH_NAME)}",
                     "env_name=${getEnvName(env.BRANCH_NAME)}",
                     "api_host=${getApiHost(env.BRANCH_NAME)}"

            ]) {
                stage('Build and push API to docker registry') {
                    withCredentials([usernamePassword(credentialsId: 'DockerHubCredentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        buildAndPush(USERNAME, PASSWORD)
                    }
                }

                stage('Deploy on k8s') {
                    runApp()
                }
            }
        } finally {
            //send email
        }

    }
}

def buildAndPush(dockerUser, dockerPassword) {
    container('docker') {
        sh """
                docker build -t ${api_image_tag}  --pull --no-cache .
                echo "Image build complete"
                docker login -u $dockerUser -p $dockerPassword
                docker push ${api_image_tag}
                echo "Image push complete"
         """
    }
}

def runApp() {
    container('kubectl') {
        dir('k8s') {
            sh """
                  echo "Branch:" ${env.BRANCH_NAME}
                  echo "env:" ${env_name}
                  envsubst < microservices.yml | kubectl apply -f -
               """
        }
        sh """
                kubectl set image deployment/zerofiltretech-blog-${env_name} zerofiltretech-blog-${env_name}=${api_image_tag} -n zerofiltretech-${env_name} --record
                if ! kubectl rollout status -w deployment/zerofiltretech-blog-${env_name} -n zerofiltretech-${env_name}; then
                    kubectl rollout undo deployment.v1.apps/zerofiltretech-blog-${env_name} -n zerofiltretech-${env_name}
                    kubectl rollout status deployment/zerofiltretech-blog-${env_name} -n zerofiltretech-${env_name}
                    exit 1
                fi
            """
    }
}


String getEnvName(String branchName) {
    if (branchName == 'main') {
        return 'prod'
    }
    return (branchName == 'ready') ? 'uat' : 'dev'
}

String getApiHost(String branchName) {
    String prefix = "blog-api"
    String suffix = ".zerofiltre.tech"
    if (branchName == 'main') {
        return prefix + suffix
    }
    return (branchName == 'ready') ? prefix + "-uat" + suffix : prefix + "-dev" + suffix
}

String getTag(String buildNumber, String branchName) {
    String tag = "imzerofiltre/zerofiltretech-blog:" + UUID.randomUUID().toString() +'-'+buildNumber
    if (branchName == 'main') {
        return tag + '-stable'
    }
    return tag + '-unstable'
}


