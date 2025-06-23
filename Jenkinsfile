pipeline {
  agent any
  tools { jdk 'JDK24'; maven 'Maven'; nodejs 'Node22' }
  triggers { githubPush() }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Install Playwright') {
      steps {
        sh 'mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"'
      }
    }

    stage('Unit Tests') {
      steps {
        sh 'mvn test -DskipE2E'
      }
      post { success { junit '**/target/surefire-reports/*.xml' } }
    }

    stage('Report to Qase') {
      environment {
        QASE_API_TOKEN = credentials('QASE_API_TOKEN')
        QASE_PROJECT_CODE = 'DIAGNOSTIC'
        QASE_RUN_ID = "RUN-${env.BUILD_NUMBER}"
      }
      steps {
        sh 'mvn test' // reporter passes automatically to Qase
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'target/**/*.html, target/**/*.zip', allowEmptyArchive: true
      junit '**/target/**/*.xml'
    }
    failure {
      emailext (
        subject: "❌ Build ${env.JOB_NAME} #${env.BUILD_NUMBER} Failed",
        body: "View console output at ${env.BUILD_URL}",
        to: 'abhaybhati@virtuowhiz.com'
      )
    }
  }
}