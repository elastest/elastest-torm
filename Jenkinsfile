node('docker'){
    
    stage "CI Container setup"

        echo("the node is up")
        def mycontainer = docker.image('elastest/ci-docker-compose-siblings')
        mycontainer.pull() // make sure we have the latest available from Docker Hub
        mycontainer.inside("-u jenkins -v /var/run/docker.sock:/var/run/docker.sock:rw") {
            
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
        
            sh 'docker ps -a'

            echo ("Remove epm* containers")

            sh 'docker rm -f $(docker inspect --format="{{.Name}}" $(docker ps -aq --no-trunc) | grep "epm" ) || echo "No images"'

            sh 'docker ps -a'
    
            projectName = 'etm'+ env.BUILD_NUMBER
            
            withEnv(['COMPOSE_PROJECT_NAME='+projectName]) {
                        
                try {
        
                    echo ('COMPOSE_PROJECT_NAME=' + env.COMPOSE_PROJECT_NAME)
                    sh 'docker-compose -f docker-compose-ci2.yml up -d'
                
                    containerId = sh (
                        script: 'cat /proc/self/cgroup | grep "docker" | sed s/\\\\//\\\\n/g | tail -1',
                        returnStdout: true
                    ).trim()

                    echo("containerId = ${containerId}")

                    sh "docker network connect ${projectName}_elastest ${containerId}"
                
                    script {
                        
                        MYSQL_IP = containerIp("mysql")
                        RABBIT_IP = containerIp("rabbit-MQ")
                        ELASTICSEARCH_IP = containerIp("elasticsearch")
                        LOGSTASH_IP = containerIp("logstash")
                        
                        echo ("Starting maven integration tests")
                        sh "cd ./elastest-torm; mvn -B "+
                            "-Dspring.datasource.url=jdbc:mysql://${MYSQL_IP}:3306/elastest-etm?useSSL=false "+
                            "-Dspring.rabbitmq.host=${RABBIT_IP} "+
                            "-Delastest.elasticsearch.host=http://${ELASTICSEARCH_IP}:9200/ "+
                            "-Dlogstash.host=${LOGSTASH_IP} "+
                            "-Delastest.incontainer=true "+
                            "clean verify;"
                        
                    }

                } finally {
        
                    sh "docker network disconnect ${projectName}_elastest ${containerId}"
        
                    echo ("docker-compose down")
                    sh 'docker-compose -f docker-compose-ci2.yml down'
        
                }
            }
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