pipeline {

    agent any

    tools {
        maven 'Maven 3'
        jdk 'Java 8'
    }

    options {
        buildDiscarder(logRotator(artifactNumToKeepStr: '2'))
    }

    environment {
        COMMIT = sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()
        VERSION = readMavenPom().getVersion()
    }

    stages {
        stage ('Build') {
            steps {
                script {
                    currentBuild.displayName = "${VERSION} #${BUILD_NUMBER}".replace("-SNAPSHOT", "")
                    currentBuild.description = "git-${COMMIT}"
                }
                sh 'mvn clean package'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/facade-*.jar', fingerprint: true
                }
            }
        }
    }

    post {
        always {
            deleteDir()
        }
    }
}