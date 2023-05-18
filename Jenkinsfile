pipeline {
  agent any
  stages {
    stage('Check out') {
      steps {
        git(url: 'https://github.com/juea01/shoppingDisproductService', branch: 'main', poll: true)
      }
    }

    stage('Compilation') {
      steps {
        sh 'mvnw clean install -DskipTests'
      }
    }

  }
}