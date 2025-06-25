pipeline {
  agent any

  tools {
    jdk 'JDK24'        // Ensure JDK24 is configured in Jenkins > Global Tool Config
    maven 'Maven'      // Ensure Maven is configured
    nodejs 'Node22'    // Ensure NodeJS is configured
  }

  environment {
    QASE_API_TOKEN = credentials('QASE_API_TOKEN')  // Secret token for Qase integration
    QASE_PROJECT_CODE = 'DIAGNOSTIC'
  }

  options {
    timestamps()
  }

  triggers {
    githubPush()  // Trigger on GitHub push
  }

  stages {
    stage('Install Chromium Only') {
      steps {
        bat 'mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"'
      }
    }

    stage('Run Tests') {
      steps {
        bat 'mvn clean test'
      }
    }

    stage('Validate Git Installation') {
      steps {
        bat 'git --version'
      }
    }

    stage('Check System PATH') {
      steps {
        bat 'echo %PATH%'
      }
    }
  }
}
