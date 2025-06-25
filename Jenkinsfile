pipeline {
  agent any

  tools {
    jdk 'JDK24'
    maven 'Maven'
    nodejs 'Node22'
  }

  environment {
    QASE_API_TOKEN = credentials('QASE_API_TOKEN')
    QASE_PROJECT_CODE = 'DIAGNOSTIC'
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
        bat 'mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"'
      }
    }

    stage('Run Tests & Report to Qase') {
      steps {
        bat 'mvn clean test'
      }
    }
  }
}
