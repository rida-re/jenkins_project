def relativeJunitLogsPath = 'core-customize/hybris/log/junit'
def projectDir = "${env.WORKSPACE}"

pipeline {
    libraries {
        //lib("shared-library@${params.LIBRARY_BRANCH}")
		lib("shared-library@main")
    }
   agent {
        node {
            label 'subordinate'
        }
    } 

    /**agent {
        docker {
            image 'ubuntu:22.04'
            args '-u root --entrypoint=""'
        }
    } **/

    triggers {
        cron('H 18 * * *')
    }
    options {
        skipDefaultCheckout(true) // No more 'Declarative: Checkout' stage
        timestamps()
        timeout(time: 4, unit: 'HOURS')
    }

    stages {
       /** stage('System Setup') {
            steps {
                sh '''
                    set -e
                    echo "=== Starting System Setup ==="
                    apt-get update
                    apt-get install -y --no-install-recommends \
                        unzip \
                        git \
                        curl \
                        jq \
                        openjdk-11-jdk \
                        ant \
                        maven \
                        build-essential
                    echo "=== System Setup Complete ==="
                '''
            }
        } **/
 
        stage('Prepare') {
            steps {
                script {
                    projectDir = "${env.WORKSPACE}"
                    echo "Project directory: ${projectDir}"
                }
                cleanWs deleteDirs: true, disableDeferredWipeout: true
                script {
                    if (isUnix()) {
                        sh '''
                            echo "=== Workspace cleaned ==="
                            ls -la ${WORKSPACE}
                        '''
                    } else {
                        def windowsWorkspace = env.WORKSPACE.replace('/', '\\')
                        bat """
                            echo === Workspace cleaned ===
                            dir "${windowsWorkspace}"
                        """
                    }
                }
                checkoutRepository("${projectDir}", "${params.PROJECT_TAG}", "${params.PROJECT_REPO}")
                extractCommerce(projectDir)
                prepareConfig(projectDir,"${params.PROFILE_CONFIG}")
            }
        }

        stage('Platform Setup') {
            steps {
                script {
                    try {
                        executeCommerceBuild(projectDir)
                    } catch (Exception e) {
                        echo "Platform setup failed: ${e.message}"
                        throw e
                    }
                }
            }
        }

        stage('Run sonarqube') {
            steps {
                sonarqubeCheck("${BUILD_TAG}_develop", projectDir, "${params.SONAR_REPO_NAME}", "${params.SONAR_URL}") // Pipeline status is set as UNSTABLE if Sonar Quality Gate fails but build is SUCCESSFUL
                failIfBuildUnstable() // Fails build if Quality Gate fails
            }
        }

        stage('Run all tests') {
            steps {
                executeAntTasks(projectDir, "yunitinit alltests -Dtestclasses.packages=${params.PACKAGE_TO_TEST}", 'dev')
            }
        }
    }

    // post build actions
    post {
        always {
            script {
                if (isUnix()) {
                    sh '''
                        echo "=== Build Summary ==="
                        echo "Workspace: ${WORKSPACE}"
                        echo "Build Status: ${currentBuild.result}"
                    '''
                } else {
                    def buildStatus = currentBuild.result
                    bat """
                        echo === Build Summary ===
                        echo Workspace: %WORKSPACE%
                        echo Build Status: ${buildStatus}
                    """
                }
            }
            junit allowEmptyResults: true, testResults: "${relativeJunitLogsPath}/*.xml"
        }
        failure {
            script {
                if (isUnix()) {
                    sh '''
                        echo "=== Build Failed ==="
                        echo "Checking workspace contents:"
                        find ${WORKSPACE} -type f -name "*.log" | head -20
                    '''
                } else {
                    bat '''
                        echo === Build Failed ===
                        echo Checking workspace contents:
                        setlocal enabledelayedexpansion
                        set count=0
                        for /r "%WORKSPACE%" %%%%F in (*.log) do (
                          echo %%%%F
                          set /a count+=1
                          if !count! geq 20 goto :done
                        )
                        :done
                    '''
                }
            }
        }
    }
}
