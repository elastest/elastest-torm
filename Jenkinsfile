node('docker'){
    stage "Container Prep"
        echo("the node is up")
        def mycontainer = docker.image('franciscordiaz/docker-in-docker-etm3')
        mycontainer.pull() // make sure we have the latest available from Docker Hub
        mycontainer.inside("-u jenkins -v /var/run/docker.sock:/var/run/docker.sock:rw") {
            git 'https://github.com/elastest/elastest-torm.git'
            
            stage "Build project"
				echo ("Build project")
				sh 'cd ./elastest-torm; ls -la; mvn clean -Pci package;'
			
			stage "Unit Test"
                sh 'ls -la'
                echo ("Starting maven tests")
                echo ("No tests yet in this stage")
                
            stage "Prepare docker-compose"
                echo ("Preparing..")                
				sh 'cd ./elastest-torm; cp ./target/elastest*OT.jar ../docker/elastest-torm'
                
            stage "Run docker-compose"
                echo ("docker compose..")
                sh 'ls -la;'// cd ./docker; ls -la'
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