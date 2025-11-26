def call(deployCode) {
    script {
        while (true) {
          wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: [[password: "${token}", var: 'PASSWD']]]) {
              def cmdUnix = "curl --location --request GET 'https://portalrotapi.hana.ondemand.com/v2/subscriptions/${subscriptionId}/deployments/$deployCode' --header 'Authorization: Bearer ${token}'"
              def cmdWin  = 'curl --location --request GET "https://portalrotapi.hana.ondemand.com/v2/subscriptions/${subscriptionId}/deployments/$deployCode" --header "Authorization: Bearer ${token}"'
              if (isUnix()) {
                  result = sh (script: cmdUnix, returnStdout:true)
              } else {
                  result = bat (script: cmdWin, returnStdout:true)
              }
          }
          echo "$result"
          statusResult = readJSON text: "$result"

          if("DEPLOYED".equals(statusResult["status"])) {
            break;
          }

          if("FAIL".equals(statusResult["status"])) {
            error("Deployment was not completed successfully on SAP Commerce Cloud")
          }

          if (isUnix()) {
              sh('sleep 120s')
          } else {
              bat('ping -n 121 127.0.0.1 >nul')
          }

        }

        echo "Commerce Cloud Deploy Complete"
    }
}  