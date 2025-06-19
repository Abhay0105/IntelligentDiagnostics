pipeline {
  agent any
  triggers { 
    githubPush() 
  }
  tools {
    jdk 'JDK17'
    maven 'Maven3'
    nodejs 'Node18'
  }
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Install Browsers') {
      steps {
        sh 'mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"'
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
      post { success { junit '**/target/playwright-report/*.xml' } }
    }
    stage('Send to Qase') {
      steps { qaseReport(projectKey: 'MYPROJ', runName: "#${env.BUILD_NUMBER}") }
    }
  }
  post {
    always { archiveArtifacts artifacts: '**/target/**/*.html', allowEmptyArchive: true }
    failure { mail to: 'team@...', subject: "Build failed #${env.BUILD_NUMBER}" }
  }
}
