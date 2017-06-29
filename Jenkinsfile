node('docker'){
    stage "Container Prep"
        echo("the node is up")
        def mycontainer = docker.image('franciscordiaz/docker-in-docker-etm5')
        mycontainer.pull() // make sure we have the latest available from Docker Hub
        mycontainer.inside("-u jenkins -v /var/run/docker.sock:/var/run/docker.sock:rw") {
            git 'https://github.com/elastest/elastest-torm.git'
            
            stage "Build elastest-torm-gui"
				echo ("Build elastest-torm-gui")
				sh 'cd ./elastest-torm-gui; npm install; mvn clean package;'
            
            stage "Build elastes-torm"
				echo ("Build elastest-torm")
				sh 'cd ./elastest-torm; mvn clean -Pci package;'
			
			stage "Unit Test"
                echo ("Starting maven tests")
                echo ("No tests yet in this stage")
                
            stage "Prepare docker-compose"
                echo ("Preparing..")                
				sh 'cd ./elastest-torm; cp ./target/elastest*SNAPSHOT.jar ../docker/elastest-torm'
                
            stage "Run docker-compose"
                echo ("docker compose..")                
                sh 'docker-compose -f docker-compose-ci.yml up -d --build'
           
			stage "Integration Test"
				echo ("No Tests yet")
                
            stage "Publish"
				echo ("Publish......")
				def myimage = docker.image('elastest/elastest-torm')
				//this is work arround as withDockerRegistry is not working properly 
				withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'elastestci-dockerhub',
					usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
					sh 'docker login -u "$USERNAME" -p "$PASSWORD"'
					myimage.push()
				}
        }
}