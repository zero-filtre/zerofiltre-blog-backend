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
                scm_vars = checkout scm
                env.GIT_COMMIT = scm_vars.GIT_COMMIT

            }

            stage('Build + unit tests') {
                container('maven') {
                    sh "mvn clean package "
                }
            }

            if (getEnvName(env.BRANCH_NAME) == 'uat') {
                stage('Integration tests') {
                    container('maven') {
                        sh "mvn failsafe:integration-test "
                    }
                }
            }

            stage('Sonarqube Analysis') {
                withSonarQubeEnv('SonarCloudServer') {
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
                     "api_host=${getApiHost(env.BRANCH_NAME)}",
                     "replicas=${getReplicas(env.BRANCH_NAME)}",
                     "requests_cpu=${getRequestsCPU(env.BRANCH_NAME)}",
                     "requests_memory=${getRequestsMemory(env.BRANCH_NAME)}",
                     "limits_cpu=${getLimitsCPU(env.BRANCH_NAME)}",
                     "limits_memory=${getLimitsMemory(env.BRANCH_NAME)}"
            ]) {
                stage('Build and push API to docker registry') {
                    withCredentials([usernamePassword(credentialsId: 'DockerHubCredentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        buildAndPush(USERNAME, PASSWORD)
                    }
                }

                stage('Deploy on k8s') {
                    runApp()
                }

                if (getEnvName(env.BRANCH_NAME) == 'uat') {
                    stage('Perf tests') {
                        try{
                            container('maven') {
                                sh "mvn jmeter:jmeter jmeter:results "
                            }
                        }catch (error){
                            echo error.getMessage()
                        }
                    }
                }
            }
        } catch (exc) {
            currentBuild.result = 'FAILURE'
            throw exc
        } finally {
            if (currentBuild.result == 'FAILURE') {
                script {
                    env.COMMIT_AUTHOR_NAME = sh(script: "git --no-pager show -s --format='%an' ${env.GIT_COMMIT}", returnStdout: true)
                    env.COMMIT_AUTHOR_EMAIL = sh(script: "git --no-pager show -s --format='%ae' ${env.GIT_COMMIT}", returnStdout: true)
                }
                sendEmail()
            }
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

String getRequestsCPU(String branchName) {
    if (branchName == 'main') {
        return '0.5'
    } else {
        return '0.1'
    }
}

String getRequestsMemory(String branchName) {
    if (branchName == 'main') {
        return '2Gi'
    } else {
        return '0.5Gi'
    }
}

String getLimitsCPU(String branchName) {
    if (branchName == 'main') {
        return '1'
    } else {
        return '0.5'
    }
}

String getLimitsMemory(String branchName) {
    if (branchName == 'main') {
        return '2Gi'
    } else {
        return '1.2Gi'
    }
}

String getReplicas(String branchName) {
    if (branchName == 'main') {
        return '1'
    }
    return (branchName == 'ready') ? '1' : '1'
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
    String tag = "imzerofiltre/zerofiltretech-blog:" + UUID.randomUUID().toString() + '-' + buildNumber
    if (branchName == 'main') {
        return tag + '-stable'
    }
    return tag + '-unstable'
}

def sendEmail() {
    String url = env.BUILD_URL.replace("http://jenkins:8080", "https://jenkins.zerofiltre.tech")
    mail(
            to: env.COMMIT_AUTHOR_EMAIL,
            subject: env.COMMIT_AUTHOR_NAME + " build #${env.BUILD_NUMBER} is a ${currentBuild.currentResult} - (${currentBuild.fullDisplayName})",
            body: "Check console output at: ${url}/console" + "\n")
}


