def call(buildName, dbUpdateMode, environmentId, strategy) {
    echo "##### Initiate Deployment to SAP Commerce Cloud Environment #####"
    //deploy tag 
    script{
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'commerceCloudCredentials', usernameVariable: 'subscriptionId', passwordVariable: 'token']]) {
            def cmdUnix = "curl --location --request POST 'https://portalrotapi.hana.ondemand.com/v2/subscriptions/${subscriptionId}/deployments' --header 'Content-Type: application/json' --header 'Authorization: Bearer ${token}' --header 'Content-Type: text/plain' --data-raw '{\"buildCode\": \"${buildName}\",\"databaseUpdateMode\": \"${dbUpdateMode}\",\"environmentCode\": \"${environmentId}\",\"strategy\": \"${strategy}\"}'"
            def cmdWin  = 'curl --location --request POST "https://portalrotapi.hana.ondemand.com/v2/subscriptions/${subscriptionId}/deployments" --header "Content-Type: application/json" --header "Authorization: Bearer ${token}" --header "Content-Type: text/plain" --data-raw "{\"buildCode\": \"${buildName}\",\"databaseUpdateMode\": \"${dbUpdateMode}\",\"environmentCode\": \"${environmentId}\",\"strategy\": \"${strategy}\"}"'
            def deploy
            if (isUnix()) {
                deploy = sh (script: cmdUnix, returnStdout:true)
            } else {
                deploy = bat (script: cmdWin, returnStdout:true)
            }
            echo "$deploy"
            deploy_result = readJSON text: "$deploy"
            deploy_code = deploy_result["code"]
            return deploy_code
        }
    }
}  