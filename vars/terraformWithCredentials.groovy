// This would typically be in a separate shared library file (vars/terraformWithCredentials.groovy)
def call(String action, String path) {
    withCredentials([
        usernamePassword(
            credentialsId: 'aws-jenkins-credentials', 
            usernameVariable: 'AWS_ACCESS_KEY_ID', 
            passwordVariable: 'AWS_SECRET_ACCESS_KEY'
        )
    ]) {
        // Set environment variables for Terraform
        withEnv([
            "TF_VAR_aws_access_key=${env.AWS_ACCESS_KEY_ID}",
            "TF_VAR_aws_secret_key=${env.AWS_SECRET_ACCESS_KEY}"
        ]) {
            switch(action) {
                case 'init':
                    terraform.init(path, 'cat-jenkins-terraform', 'jenkins/terraform.tfstate', 'us-west-2')
                    break
                case 'plan':
                    terraform.plan(path)
                    break
                case 'apply':
                    terraform.apply(path)
                    break
                case 'destroy':
                    terraform.destroy(path)
                    break
                default:
                    error "Unsupported Terraform action: ${action}"
            }
        }
    }
}
