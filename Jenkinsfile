def deploymentStarted = false
pipeline {
    agent any
    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        timestamps()
        timeout(time: 20, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '15', artifactNumToKeepStr: '10'))
    }
    triggers { pollSCM('H/2 * * * *') }
    environment {
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
        PATH = "/usr/lib/jvm/java-17-openjdk-amd64/bin:${env.PATH}"
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm 
                dir('evidence') { deleteDir() }
                script { env.REVISION = sh(script: 'git rev-parse HEAD', returnStdout: true).trim() }
                sh 'mkdir -p evidence; git log -1 --format=fuller > evidence/commit.txt'
            }
        }
        stage('Build') { steps { sh 'mvn -B -Dbuild.revision="$REVISION" clean compile' } }
        stage('Test') {
            steps { sh 'mvn -B -Dbuild.revision="$REVISION" test' }
            post { always { junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: false } }
        }
        stage('Package') {
            steps {
                sh 'mvn -B -Dbuild.revision="$REVISION" -DskipTests package'
                sh 'test -s target/student-feedback.war; sha256sum target/student-feedback.war > evidence/war-sha256.txt'
            }
        }
        stage('Archive') { steps { archiveArtifacts artifacts: 'target/student-feedback.war', fingerprint: true } }
        stage('Deploy') {
            steps {
                script { deploymentStarted = true }
                sh 'sudo -n -u tomcat /usr/local/sbin/student-feedback-release deploy'
            }
        }
        stage('Verify') {
            steps {
                sh 'python3 scripts/verify-health.py "intentional-rollback-demo" > evidence/health-check.json'
                sh 'cat evidence/health-check.json'
                sh 'sudo -n -u tomcat /usr/local/sbin/student-feedback-release accept'
                script { deploymentStarted = false }
            }
        }
    }
    post {
        unsuccessful {
            script {
                if (deploymentStarted) {
                    sh 'sudo -n -u tomcat /usr/local/sbin/student-feedback-release rollback'
                }
            }
        }
        always { archiveArtifacts artifacts: 'evidence/*', allowEmptyArchive: true }
    }
}
