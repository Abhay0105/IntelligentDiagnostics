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
        bat 'mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"'
      }
    }

    stage('Unit Tests') {
      steps {
        bat 'mvn test -DskipE2E'
      }
      post { success { junit '**/target/surefire-reports/*.xml' } }
    }

    stage('E2E Tests') {
      steps {
        bat 'mvn test -Pplaywright'
      }
      post {
        always {
          junit '**/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Report to Qase') {
      environment {
        QASE_API_TOKEN = '73768f37203aedffd6550ff1b4a047b48385b28e0fd842acfcd4d226b371805a'
        QASE_PROJECT_CODE = 'DIAGNOSTIC'
      }
      steps {
        bat 'mvn test -Pplaywright' // reporter passes automatically to Qase
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
