def call(commerceDir, property){
    script {
        if (isUnix()) {
            sh("echo '${property}' >> ${commerceDir}/core-customize/hybris/config/local.properties")
        } else {
            bat("""
                echo ${property}>> "${commerceDir}\core-customize\hybris\config\local.properties"
            """)
        }
    }
}
