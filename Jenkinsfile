pipeline {
  agent any

  tools {
    jdk 'JDK24'         // Must be set in Jenkins global tools
    maven 'Maven'       // Same
    nodejs 'Node22'     // Same
  }

  environment {
    QASE_API_TOKEN = credentials('QASE_API_TOKEN')   // Must exist in Jenkins credentials
    QASE_PROJECT_CODE = 'DIAGNOSTIC'                 // Your Qase project code
    QASE_RUN_NAME = "Jenkins Run ${BUILD_NUMBER}"    // Optional but recommended
  }

  options {
    timestamps()
  }

  triggers {
    githubPush()
  }

  stages {
    stage('Install Chromium Only') {
      steps {
        bat 'mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"'
      }
    }

    stage('Run Tests & Report to Qase') {
      steps {
        bat 'mvn clean test -Dheadless=false'
      }
    }
  }
}
