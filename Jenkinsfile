node('docker'){
    
    stage "Container Prep"
        echo("the node is up")
        def mycontainer = docker.image('elastest/ci-docker-compose-siblings')
        mycontainer.pull() // make sure we have the latest available from Docker Hub
        mycontainer.inside("-u jenkins -v /var/run/docker.sock:/var/run/docker.sock:rw") {
            
            environment {
                COMPOSE_PROJECT_NAME = 'etm-'+${env.BUILD_NUMBER}
            }
            
            git 'https://github.com/elastest/elastest-torm.git'
            
            stage "Build elastest-torm-gui"
				echo ("Build elastest-torm-gui")
				sh 'cd ./elastest-torm-gui; npm install; mvn clean package;'
            
            stage "Build elastes-torm"
				echo ("Build elastest-torm")
				sh 'cd ./elastest-torm; mvn clean -Pci package;'
			
			stage "Unit Test"
                echo ("Starting maven unit tests")
               sh 'cd ./elastest-torm; mvn clean -Pci-no-it-test package;'                
                
            stage "Creating etm image"
                echo ("Creating elastest/etm image..")                
 				sh 'cd ./docker/elastest-torm; ./build-image.sh;'

            stage "Publish"
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