node('TESTDOCKER'){
    
    stage "CI Container setup"

        echo("the node is up")
        def mycontainer = docker.image('elastest/ci-docker-compose-siblings')
        mycontainer.pull() // make sure we have the latest available from Docker Hub
        mycontainer.inside("-u jenkins -v /var/run/docker.sock:/var/run/docker.sock:rw -v ${WORKSPACE}:/home/jenkins/.m2 -v /home/ubuntu/.gnupg:/home/ubuntu/.gnupg") {
            
        git 'https://github.com/elastest/elastest-torm.git'

        stage "Test and deploy epm-client"
            echo ("Test and deploy epm-client")
            withMaven(maven: 'mvn3.3.9', mavenSettingsConfig: '0e7fd7e6-77b0-4ea2-b808-e8164667a6eb') {
                sh 'cd ./epm-client; mvn deploy -Djenkins=true;'
            }
        
        stage "Build elastest-torm-gui"
            echo ("Build elastest-torm-gui")
            sh 'cd ./elastest-torm-gui; npm install; mvn package;'
        
        stage "Build elastest-torm"
            echo ("Build elastest-torm")
            sh 'cd ./elastest-torm; mvn -Pci package;'
        
        stage "Unit Test elastest-torm"
            echo ("Starting TORM unit tests")
            sh 'cd ./elastest-torm; mvn -Pci-no-it-test test;'
            step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
            
        stage ("IT Test elastest-torm")
            echo ("Starting TORM integration tests")
            sh 'cd ./scripts; ./it.sh'
            step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])


        stage "Upload coverage and quality reports"
            echo ("Upload reports to SonarCloud and Codecov")
            sh 'mvn org.jacoco:jacoco-maven-plugin:prepare-agent sonar:sonar -Dsonar.host.url=https://sonarcloud.io -Dsonar.organization=elastest -Dsonar.login=${TORM_SONARCLOUD_TOKEN}'
            def codecovArgs = '-K '
            if (env.GITHUB_PR_NUMBER != '') {
              // This is a PR
              codecovArgs += "-B ${env.GITHUB_PR_TARGET_BRANCH} " +
                  "-C ${env.GITHUB_PR_HEAD_SHA} " +
                  "-P ${env.GITHUB_PR_NUMBER} "
            } else {
              // Not a PR
              codecovArgs += "-B ${env.GIT_BRANCH} " +
                  "-C ${env.GIT_COMMIT} "
            }
            sh "curl -s https://codecov.io/bash | bash -s - ${codecovArgs} -t ${TORM_CODECOV_TOKEN} || echo 'Codecov did not collect coverage reports'"

        stage "Create etm docker image"
            echo ("Creating elastest/etm image..")                
            sh 'cd ./docker/elastest-torm; ./build-image.sh;'

        stage "Publish etm docker image"
            echo ("Publish elastest/etm image")
            def myimage = docker.image('elastest/etm')
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'elastestci-dockerhub',
                usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                sh 'docker login -u "$USERNAME" -p "$PASSWORD"'
                myimage.push()
            }
        }
}

def containerIp(service) {
    containerIp = sh (
        script: "docker inspect --format=\"{{.NetworkSettings.Networks."+env.COMPOSE_PROJECT_NAME+"_elastest.IPAddress}}\" "+env.COMPOSE_PROJECT_NAME+"_"+service+"_1",
        returnStdout: true
    ).trim()
    
    echo service+" IP = " + containerIp;
    return containerIp;
}
