Map call(Map args = [:]) {
    def lib = library(identifier: "jenkins@20211123", retriever: legacySCM(scm))
    String manifest = args.manifest ?: "manifests/${INPUT_MANIFEST}"
    def inputManifest = lib.jenkins.InputManifest.new(readYaml(file: manifest))
    dockerImage = inputManifest.ci?.image?.name ?: 'opensearchstaging/ci-runner:ci-runner-centos7-v1'
    dockerArgs = inputManifest.ci?.image?.args
    // Using default javaVersion as jdk-17
    String javaVersion = 'jdk-17'
    java.util.regex.Matcher match = (dockerArgs =~ /jdk-\d+/) 
    if (match.find()) {
            def line = match[0]
           javaVersion = line
    }
    echo "Using Docker image ${dockerImage} (${dockerArgs})"
    echo "Using java version ${javaVersion}"
    return [
        image: dockerImage,
        args: dockerArgs,
        javaVersion: javaVersion
    ]
}
