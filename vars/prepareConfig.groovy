def call(commerceDir, profileConfig) {
    echo "##### Prepare config folder ##### -- ${commerceDir}/hybris"
    //sh " mkdir -p ${commerceDir}/hybris/config && cp -r ${commerceDir}/hybris/bin/custom/gl/core-customize/config/profiles/local/* ${commerceDir}/hybris/config"
    //sh " cp ${commerceDir}/hybris/bin/custom/gl/core-customize/config/profiles/${profileConfig}/local.properties ${commerceDir}/hybris/config/local.properties"
}