def call(commerceDir, branch, projectRepository) {
    // Ensure branch has a default value if not provided
    branch = branch ?: "main"
    
    // Ensure repository URL is set
    repository = projectRepository ?: "https://github.com/SAP-samples/cloud-commerce-sample-setup.git"

    echo "##### Checkout repository: ${repository}, branch: ${branch} #####"

    // Use credentials if needed (for private repos). For public repos, credentialsId can be empty.
    withCredentials([[$class: 'UsernamePasswordMultiBinding', 
                      credentialsId: 'githubCodeRepoCredentials', 
                      usernameVariable: 'USERNAME', 
                      passwordVariable: 'PASSWORD']]) {

        checkout([$class: 'GitSCM',
            branches: [[name: "*/${branch}"]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [
                // Optional: clean before checkout to avoid stale files
                [$class: 'CleanBeforeCheckout']
            ],
            userRemoteConfigs: [[
                url: repository,
                // Use credentials only if needed
                credentialsId: 'githubCodeRepoCredentials'
            ]]
        ])
    }
}
