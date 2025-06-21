pipeline {
  agent any

  triggers {
    githubPush()
  }

  tools {
    jdk 'JDK24'
    maven 'Maven'
    nodejs 'Node22'
  }

  environment {
    QASE_API_TOKEN = credentials('QASE_API_TOKEN')
    QASE_PROJECT_CODE = 'DIAGNOSTIC'
    QASE_RUN_NAME = "Run_${env.BUILD_NUMBER}"
  }

  options {
    timestamps()
    // ✅ No need for skipDefaultCheckout — use default behavior
  }

  stages {

    stage('Install Playwright') {
      steps {
        bat 'mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"'
      }
    }

    stage('Test') {
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

    stage('E2E Tests') {
      steps {
        bat 'mvn clean test'
      }
      post {
        always {
          junit '**/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Report to Qase') {
      steps {
        echo "✅ Qase reporter uploads results from this test run automatically"
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
