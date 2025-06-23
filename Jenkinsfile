pipeline {
  agent any

  tools {
    jdk 'JDK24'
    maven 'Maven'
    nodejs 'Node22'
  }

  environment {
    QASE_API_TOKEN = credentials('QASE_API_TOKEN') // Must be defined in Jenkins → Credentials
    QASE_PROJECT_CODE = 'DIAGNOSTIC'
    QASE_RUN_NAME = "Run_${env.BUILD_NUMBER}"
  }

  options {
    timestamps()
  }

  triggers {
    githubPush()
  }

  stages {

    stage('Install Playwright') {
      steps {
        bat 'mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"'
      }
    }

    stage('Jenkinsfile Loaded') {
      steps {
        echo '✅ Jenkinsfile loaded successfully!'
      }
    }

    stage('Unit Tests') {
      steps {
        bat 'mvn clean test -DskipE2E'
      }
      post {
        success {
          junit '**/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Report to Qase') {
      steps {
        echo "✅ Qase reporter uploads results automatically"
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: '**/target/screenshots/*.png, **/target/*.html, **/target/*.zip', allowEmptyArchive: true
      junit '**/target/surefire-reports/*.xml'
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