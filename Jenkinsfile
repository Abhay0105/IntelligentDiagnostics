pipeline {
  agent any

  tools {
    jdk 'JDK24'        // Make sure this exists in Jenkins → Global Tool Configuration
    maven 'Maven'      // Make sure this exists too
  }

  environment {
    PATH = "${tool 'NodeJS'}\\bin;${env.PATH}"  // If you have NodeJS tool installed in Jenkins
    PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD = '1'      // Skip if browsers already installed
  }

  stages {
    stage('Checkout Code') {
      steps {
        git 'https://github.com/Abhay0105/IntelligentDiagnostics.git'
      }
    }

    stage('Install Playwright Browsers') {
      steps {
        bat 'npx playwright install'
      }
    }

    stage('Build and Test') {
      steps {
        bat 'mvn clean test'
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: '**/target/*.log', allowEmptyArchive: true
      junit '**/target/surefire-reports/*.xml'
    }
  }
}
