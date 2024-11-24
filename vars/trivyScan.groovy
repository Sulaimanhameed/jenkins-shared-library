def kaniko() {
    script {
        // Check if the environment variable to skip Trivy scan is set
        if (env.SKIP_TRIVY_SCAN?.toBoolean()) {
            echo "Skipping Trivy scan as per configuration."
            return 0
        }

        // Fetch and write the HTML template for the Trivy report
        def tplContent = libraryResource "org/dcube/trivy/html.tpl"
        writeFile file: "${WORKSPACE}/html.tpl", text: tplContent

        // Fetch and write the Trivy configuration file
        def trivyConfigContent = libraryResource "org/dcube/trivy/trivy.yml"
        writeFile file: "${WORKSPACE}/trivy.yml", text: trivyConfigContent

        // Define the command to run the Trivy scan on the tarred image
        def command = "trivy image --config ${WORKSPACE}/trivy.yml --template '@${WORKSPACE}/html.tpl' -o ${WORKSPACE}/trivy-report.html --input ${WORKSPACE}/${BUILD_NUMBER}.tar"

        // Execute the command and capture the exit status
        def exitCode = sh(script: command, returnStatus: true)

        // Handle the exit status; log a warning if vulnerabilities are found
        if (exitCode != 0) {
            warnError('Trivy scan found vulnerabilities') {
                echo "Trivy scan found vulnerabilities"
            }
        } else {
            echo "Trivy scan completed successfully with no critical vulnerabilities."
        }

        // Return the exit code
        return exitCode
    }
}

def docker(String imageName, String imageTag) {
    try {
        // Check if the environment variable to skip Trivy scan is set
        if (env.SKIP_TRIVY_SCAN?.toBoolean()) {
            echo "Skipping Trivy scan as per configuration."
            return 0
        }

        setupTrivyFiles()

        def command = "trivy image --config ${WORKSPACE}/trivy.yml --template '@${WORKSPACE}/html.tpl' -o ${WORKSPACE}/trivy-report.html ${imageName}:${imageTag}"
        def exitCode = runTrivyCommand(command)

        return exitCode
    } catch (Exception e) {
        error "Exception during Trivy scan for Docker image ${imageName}:${imageTag}: ${e.getMessage()}"
    }
}
