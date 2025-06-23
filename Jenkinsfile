pipeline {
  agent any
  tools { jdk 'JDK17'; maven 'Maven3'; nodejs 'Node18' }
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

    stage('E2E Tests') {
      steps {
        sh 'mvn test -Pplaywright'
      }
      post {
        always {
          junit '**/target/playwright-report/*.xml'
          publishHTML([reportDir: 'target/playwright-report', reportFiles: 'index.html', reportName: 'Playwright HTML Report'])
        }
      }
    }

    stage('Report to Qase') {
      environment {
        QASE_API_TOKEN = credentials('QASE_API_TOKEN')
        QASE_PROJECT_CODE = 'YOUR_PROJ'
        QASE_RUN_ID = "RUN-${env.BUILD_NUMBER}"
      }
      steps {
        sh 'mvn test -Pplaywright' // reporter passes automatically to Qase
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
        to: 'team@example.com'
      )
    }
  }
}
