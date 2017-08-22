node('docker'){
    
    stage "CI Container setup"

        echo("the node is up")
        def mycontainer = docker.image('elastest/ci-docker-compose-siblings')
        mycontainer.pull() // make sure we have the latest available from Docker Hub
        mycontainer.inside("-u jenkins -v /var/run/docker.sock:/var/run/docker.sock:rw -v $
{WORKSPACE}/m2:/home/jenkins/.m2") {
            
        git 'https://github.com/elastest/elastest-torm.git'
        
        stage "Build elastest-torm-gui"
            echo ("Build elastest-torm-gui")
            sh 'cd ./elastest-torm-gui; npm install; mvn clean package;'
        
        stage "Build elastest-torm"
            echo ("Build elastest-torm")
            sh 'cd ./elastest-torm; mvn -B clean -Pci package;'
        
        stage "Unit Test elastest-torm"
            echo ("Starting maven unit tests")
            sh 'cd ./elastest-torm; mvn -B clean -Pci-no-it-test package;'                
            
        stage ("IT Test elastest-torm") { 

            sh 'cd ./scripts; ./it.sh'

        }

        stage "Create etm docker image"
        
            echo ("Creating elastest/etm image..")                
            sh 'cd ./docker/elastest-torm; ./build-image.sh;'

        stage "Publish etm docker image"

            echo ("Publish elastest/etm image")
            def myimage = docker.image('elastest/etm')
            //this is work arround as withDockerRegistry is not working properly 
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