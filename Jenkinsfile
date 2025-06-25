pipeline {
  agent any

  tools {
    jdk 'JDK24'        // Make sure "JDK24" is correctly configured in Jenkins → Global Tool Configuration
    maven 'Maven'      // Ensure Maven is configured with name "Maven"
    nodejs 'Node22'    // Ensure NodeJS is configured with name "Node22"
  }

  environment {
    QASE_API_TOKEN = credentials('QASE_API_TOKEN')  // Make sure this credential exists in Jenkins
    QASE_PROJECT_CODE = 'DIAGNOSTIC'
  }

  options {
    timestamps()
  }

  triggers {
    githubPush()  // Auto-trigger build on GitHub push if webhook is set correctly
  }

  stages {

    stage('Install Playwright') {
      steps {
        bat 'mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"'
      }
    }

    stage('Run Tests & Report to Qase') {
      steps {
        bat 'mvn clean test'
      }
    }

    stage('Check Git Version') {
      steps {
        bat 'git --version'
      }
    }
  }
}
