pipeline {
  agent any

  triggers {
    githubPush()
  }

  tools {
    jdk 'JDK24' // ✅ You're sticking to Java 24
    maven 'Maven'
    nodejs 'Node22'
  }

  environment {
    QASE_API_TOKEN = credentials('QASE_API_TOKEN') // 🔐 From Jenkins credentials
    QASE_PROJECT_CODE = 'DIAGNOSTIC'
    QASE_RUN_NAME = "Run_${env.BUILD_NUMBER}"
  }

  options {
    timestamps() // 📅 Better log tracing
    skipDefaultCheckout true // ✅ Manual checkout to configure Git credentials
  }

  stages {

    stage('Checkout SCM') {
      steps {
        checkout([$class: 'GitSCM',
          branches: [[name: '*/main']],
          userRemoteConfigs: [[
            url: 'https://github.com/Abhay0105/IntelligentDiagnostics.git',
            credentialsId: 'github-access-token' // 🔐 GitHub PAT saved in Jenkins
          ]]
        ])
      }
    }

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
        bat 'mvn clean test -DskipE2E' // ✅ Clean + skip E2E
      }
      post {
        success {
          junit '**/target/surefire-reports/*.xml'
        }
      }
    }

    stage('E2E Tests') {
      steps {
        bat 'mvn clean test' // ✅ Full E2E run
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
