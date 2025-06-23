pipeline {
  agent any

  tools {
    jdk 'JDK24'
    maven 'Maven'
  }

  environment {
    QASE_API_TOKEN = credentials('QASE_API_TOKEN') // Add this in Jenkins credentials
    QASE_PROJECT_CODE = 'DIAGNOSTIC'
    QASE_RUN_ID = "RUN-${env.BUILD_NUMBER}"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Run Tests') {
      steps {
        bat 'mvn clean test'
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: '**/target/**/*.html, **/target/**/*.zip, **/target/screenshots/*.png', allowEmptyArchive: true
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
