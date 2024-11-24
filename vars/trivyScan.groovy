def kaniko() {
    script {
        // Fetch and write HTML template for Trivy report
        def tplContent = libraryResource "org/dcube/trivy/html.tpl"
        writeFile file: "${WORKSPACE}/html.tpl", text: tplContent

        // Fetch and write Trivy configuration file
        def trivyConfigContent = libraryResource "org/dcube/trivy/trivy.yml"
        writeFile file: "${WORKSPACE}/trivy.yml", text: trivyConfigContent

        // Define Trivy command
        def command = "trivy image --config ${WORKSPACE}/trivy.yml --template '@${WORKSPACE}/html.tpl' -o ${WORKSPACE}/trivy-report.html --input ${WORKSPACE}/${BUILD_NUMBER}.tar"

        // Execute the Trivy command and capture the exit status
        def exitCode = sh(script: command, returnStatus: true)

        // Handle the exit status without stopping the pipeline
        if (exitCode != 0) {
            warnError('Trivy scan found vulnerabilities') { 
                echo "Trivy scan found vulnerabilities"
            }
        } else {
            echo "Trivy scan completed successfully with no critical vulnerabilities."
        }

        // Return the exit code of the command
        return exitCode
    }
}
