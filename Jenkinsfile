#!/usr/bin/env groovy

/* History: 20 most recent builds */
properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '20']]])

node {
  stage 'Checkout'
  checkout scm

  stage 'Build'

  /* maven build */
  mvn 'clean install -B -V'

  /* Save Test Results */
  stage 'Results'

  /* Archive the test results */
  junit '**/target/surefire-reports/TEST-*.xml'
}

void mvn(def args) {
  String jdktool = tool 'jdk8'
  def mvnHome = tool name: 'mvn'

  /* JAVA_HOME */
  List javaEnv = [
    "PATH+JDK=${jdktool}/bin", "JAVA_HOME=${jdktool}"
  ]

  withEnv(javaEnv) {
    timeout(time: 30, unit: 'MINUTES') {
      if (isUnix()) {
        sh "${mvnHome}/bin/mvn ${args}"
      } else {
        bat "${mvnHome}\\bin\\mvn ${args}"
      }
    }
  }
}
