def call(codeNumber) {
    script {
        while (true) {
          withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'commerceCloudCredentials', usernameVariable: 'subscriptionId', passwordVariable: 'token']]) {
              def cmdUnix = "curl --location --request GET 'https://portalrotapi.hana.ondemand.com/v2/subscriptions/${subscriptionId}/builds/$codeNumber' --header 'Authorization: Bearer ${token}'"
              def cmdWin  = 'curl --location --request GET "https://portalrotapi.hana.ondemand.com/v2/subscriptions/${subscriptionId}/builds/$codeNumber" --header "Authorization: Bearer ${token}"'
              if (isUnix()) {
                  result = sh (script: cmdUnix, returnStdout:true)
              } else {
                  result = bat (script: cmdWin, returnStdout:true)
              }
          }
          echo "$result"
          statusResult = readJSON text: "$result"

          if("SUCCESS".equals(statusResult["status"])) {
            break;
          }

          if("FAIL".equals(statusResult["status"])) {
            error("Build was not completed successfully on SAP Commerce Cloud")
          }

          if (isUnix()) {
              sh('sleep 120s')
          } else {
              bat('ping -n 121 127.0.0.1 >nul')
          }

        }

        echo "Commerce Cloud Build Complete"
    }
}  