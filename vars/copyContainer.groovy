/**@
 * Copies a container from one docker registry to another
 *
 * @param args A map of the following parameters
 * @param args.sourceImage The url to the image to be copied from, supports any public docker registry
 * @param args.destinationImage Follows the format NAME[:TAG|@DIGEST], e.g. opensearchproject/opensearch:1.2.4
 * @param args.destinationRegistry The docker registry, currently supports 'docker' or 'ecr'
 * @param args.prod (true/false) to choose between production and staging environments
 */
void call(Map args = [:]) {


    if (args.destinationRegistry == 'docker') {
        def dockerJenkinsCredential = args.prod ? "jenkins-staging-docker-prod-token" : "jenkins-staging-docker-staging-credential"
        def dockerTargetRepo = args.prod ? "opensearchproject" : "opensearchstaging"
        withCredentials([usernamePassword(credentialsId: dockerJenkinsCredential, usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
            def dockerLogin = sh(returnStdout: true, script: "echo $DOCKER_PASSWORD | docker login --username $DOCKER_USERNAME --password-stdin").trim()
            sh """
                gcrane cp ${args.sourceImage} ${dockerTargetRepo}/${args.destinationImage}
                docker logout
            """
        }
    }
    if (args.destinationRegistry == 'ecr') {
        if(args.prod) {
            withCredentials([
                string(credentialsId: 'jenkins-artifact-promotion-role', variable: 'ARTIFACT_PROMOTION_ROLE_NAME'),
                string(credentialsId: 'jenkins-artifact-promotion-account', variable: 'AWS_ACCOUNT_ARTIFACT')]) 
                {
                    withAWS(role: "${ARTIFACT_PROMOTION_ROLE_NAME}", roleAccount: "${AWS_ACCOUNT_ARTIFACT}", duration: 900, roleSessionName: 'jenkins-session') {
                        def ecrLogin = sh(returnStdout: true, script: "aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin public.ecr.aws/opensearchproject").trim()
                        sh """
                            gcrane cp ${args.sourceImage} public.ecr.aws/opensearchproject/${args.destinationImage}
                            docker logout public.ecr.aws/opensearchproject
                        """
                    }
                }
        }
        if(!args.prod) {
            def ecrLogin = sh(returnStdout: true, script: "aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin public.ecr.aws/opensearchstaging").trim()
            sh """
                 gcrane cp ${args.sourceImage} public.ecr.aws/opensearchstaging/${args.destinationImage}
                 docker logout public.ecr.aws/opensearchstaging
            """
        }
    }
}