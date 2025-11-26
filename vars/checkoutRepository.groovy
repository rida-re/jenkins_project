def call(commerceDir, branch, projectRepository) {
<<<<<<< HEAD
    // Ensure branch has a default value if not provided
    branch = branch ?: "main"
    
    // Ensure repository URL is set
    repository = projectRepository ?: "https://github.com/SAP-samples/cloud-commerce-sample-setup.git"

    echo "##### Checkout repository: ${repository}, branch: ${branch} #####"

    try {
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
        echo "Repository checked out successfully"
    } catch (Exception e) {
        echo "ERROR: Failed to checkout repository: ${e.message}"
        throw e
=======
    urlPrefix = "https://"
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'githubCodeRepoCredentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {        
        repoDomainPart = projectRepository.substring(urlPrefix.size())
        repository = "https://github.com/SAP-samples/cloud-commerce-sample-setup.git"
        echo "##### Checkout repository #####"
		 sh "curl ${repository}"
		 sh "git clone ${repository} . && git fetch --all && git checkout origin/${branch}"       
>>>>>>> parent of 64d1acf (change)
    }
}
