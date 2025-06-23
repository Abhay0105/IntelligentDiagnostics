
pipeline {
  agent any
  tools { jdk 'JDK24'; maven 'Maven'; nodejs 'Node22' }
  triggers { githubPush() }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Unit Tests') {
      steps {
        sh 'mvn clean test'
      }
      post { success { junit '**/target/surefire-reports/*.xml' } }
    }

    stage('Report to Qase') {
      environment {
        QASE_API_TOKEN = credentials('QASE_API_TOKEN')
        QASE_PROJECT_CODE = 'DIAGNOSTIC'
      }
      steps {
        sh 'mvn clean test' // reporter passes automatically to Qase
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
