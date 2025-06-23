pipeline {
  agent any

  tools {
    jdk 'JDK24'
    maven 'Maven'
  }

  stages {
    stage('Build') {
      steps {
        bat 'mvn clean test'
      }
    }
  }
}
