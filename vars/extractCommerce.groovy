def call(commerceDir) {
    echo "##### Extract commerce platform ##### -- ${commerceDir}"

    try {
        // Skip extraction if already unzipped 
        if (fileExists("${commerceDir}/hybris/bin/platform") && fileExists("${commerceDir}/hybris/bin/modules")) {
            echo "⚠️ Commerce platform already extracted. Skipping unzip."
            return
        }

        script {
            if (isUnix()) {
                sh """
                    set -e
                    echo "=== Extracting Commerce Platform ==="

                    # Install unzip if not available
                    if ! command -v unzip &> /dev/null; then
                        echo "Installing unzip..."
                        apt-get update -qq
                        apt-get install -y -qq unzip
                    fi

                    ZIP_FILE="/var/jenkins_home/workspace/CXCOM2211.zip"

                    if [ ! -f "\$ZIP_FILE" ]; then
                        echo "ERROR: CXCOM2211.zip not found at \$ZIP_FILE"
                        exit 1
                    fi

                    echo "Found ZIP file: \$ZIP_FILE"
                    echo "File size: \$(du -h \"\$ZIP_FILE\" | cut -f1)"
                    echo "Extracting hybris folder to ${commerceDir}/core-customize"

                    mkdir -p "${commerceDir}/core-customize"
                    unzip -o "\$ZIP_FILE" 'hybris/*' -d "${commerceDir}/core-customize"

                    echo "Extraction completed successfully"
                """
            } else {
                bat """
                        @echo off
                        setlocal enabledelayedexpansion

                        set ZIP_FILE=C:\\var\\jenkins_home\\workspace\\CXCOM2211.zip
                        set TEMP_DIR=C:\\var\\jenkins_home\\workspace\\temp\\commerce
                        set DEST_DIR=${commerceDir.replace('/', '\\')}\\core-customize

                        if not exist "%ZIP_FILE%" (
                            echo ERROR: CXCOM2211.zip not found at %ZIP_FILE%
                            exit /b 1
                        )

                        echo Cleaning temp directory...
                        if exist "%TEMP_DIR%" rmdir /S /Q "%TEMP_DIR%"
                        mkdir "%TEMP_DIR%"

                        echo Extracting ZIP to temporary folder using 7-Zip...
                        if not exist "C:\\Program Files\\7-Zip\\7z.exe" (
                            echo ERROR: 7-Zip is not installed
                            exit /b 1
                        )
                        "C:\\Program Files\\7-Zip\\7z.exe" x "%ZIP_FILE%" -o"%TEMP_DIR%" -y

                        echo Creating destination folder...
                        if not exist "%DEST_DIR%" mkdir "%DEST_DIR%"

                        echo Copying files from temp to destination using robocopy...
                        robocopy "%TEMP_DIR%\\hybris" "%DEST_DIR%\\hybris" /E /COPY:DAT /MT:16 /R:1 /W:1

         

                        echo Extraction completed successfully
                    """

            }
        }

        echo "Commerce extraction completed"
    } catch (Exception e) {
        echo "ERROR: Failed to extract commerce: ${e.message}"
        throw e
    }
}
