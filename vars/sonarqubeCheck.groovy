def call(branch, commerceDir, projectName, sonarUrl) {
    script {
        BRANCH_WITH_DASH = branch.replace('/', '-')
    }

    addProperty(commerceDir, "sonar.projectName=$projectName-$BRANCH_WITH_DASH")
    addProperty(commerceDir, "sonar.projectKey=$projectName-$BRANCH_WITH_DASH")

    echo "##### Execute sonarcheck #####"
    withSonarQubeEnv('sonarQubeConfiguration') {
      executeAntTasks(commerceDir, "sonarcheck", "dev", "-Xms256m -Xmx4096m -XX:+UseSerialGC -Dfile.encoding=UTF-8")
    }

    echo "Sonar Quality Gate check in progress..."

    script {
        SONAR_CHECK_URL = "$sonarUrl/api/qualitygates/project_status?projectKey=$projectName:$BRANCH_WITH_DASH"
        if (isUnix()) {
            output = sh(returnStdout: true, script: "echo \$(curl $SONAR_CHECK_URL | jq '.projectStatus.status')")
        } else {
            output = bat(returnStdout: true, script: 'for /f %%%%a in (''curl %SONAR_CHECK_URL% ^| jq \''.projectStatus.status\''') do @echo %%%%a')
        }
        while (output.contains("NONE")) {
          if (isUnix()) {
              sh('sleep 10s')
              output = sh(returnStdout: true, script: "echo \$(curl $SONAR_CHECK_URL | jq '.projectStatus.status')")
          } else {
              bat('ping -n 11 127.0.0.1 >nul')
              output = bat(returnStdout: true, script: 'for /f %%%%a in (''curl %SONAR_CHECK_URL% ^| jq \''.projectStatus.status\''') do @echo %%%%a')
          }
        }
        if (output.contains("ERROR")) {
            currentBuild.result = 'UNSTABLE'
            echo 'Build: SUCCESSFUL!\nSonar Quality Gate: FAILED!\nFinal result: UNSTABLE!'
        }
    }
}
    