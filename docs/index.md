# Elastest Test Recommendation and Orchestrator Manager

[High level description of the version 0.1 of the component]

## Features

[Features provided in the version 0.1 of the component. Planned features can be specified, but in a different subsection from implemented features]

## Execution

[How to install the version 0.1 of component. This version should be installable wihout using any other ElasTest component. It is ok to provide a docker-compose.yml file to start the component dockerized with its dependencies.]

## Basic usage

[Basic instructions on how to use 0.1 version of the component]

## Development documentation

[Documentation about how to develop the component. Programming language, frameworks, libs, etc.]

### Development instructions for working on Elastes-etm
Following are the necessary instructions to configure the elastest-etm module development environment. Some of the actions to be performed will depend on the SO. But the first thing is to download the repository code from GitHub:

 - Create a Fork
 - Clone the new repository `git clone https://github.com/forkrepository/elastest-torm.git` 
	
### Prerequisites
It is necessary to have installed the following tools:

- Eclipse IDE or similar
- Visual Studio Code or similar
- Angular CLI (View Angular CLI [Prerequisites](https://github.com/angular/angular-cli)) 
- Maven 3.3.9

#### Windows

- Install Docker Toolbox.

#### Linux 

- Install docker-compose.

### Docker configuration on Windows

#### Enable Docker API access from Windows

- docker-machine ssh from power shell or any terminal emulator
- `sudo vi /var/lib/boot2docker/profile`
- set `DOCKER_TLS=no`
- exit from docker host
- `docker-machine restart`

When access to the API is active, the graphical client for Docker Toolbox and Windows CLI, will no longer be operational.
Now, if you want to work with docker you need to do `docker-machine ssh` from the terminal.

#### Install docker-compose on boo2docker

- `docker-machine ssh from Windows terminal
- `curl -L https://github.com/docker/compose/releases/download/1.14.0/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose`
- `chmod +x /usr/local/bin/docker-compose`

#### Add shared folder to boot2docker on Virtual box
To provide access to project files from the docker host, you must share the project folder with boot2docker from the virtual box.

#### Running elastest-etm services

- `docker-machine ssh` from windows terminal
- change the working directory to the shared folder of the project.
- `sudo sysctl -w vm.max_map_count=262144` (It is necessary for elasticsearch service)
- `docker-compose up -d`

### Docker configuration on Linux

- change the working directory to the project folder.
- `docker-compose up -d

### Running elastest etm in development mode

#### Elastest-etm server application

- Build project with `mvn clean -Pdev package`
- Run Spring Boot application


#### Elastest-etm client application

- Open a terminal and change the working directory to the project folder
- `npm install`
- `npm start`

The graphical client will be accessible at http://localhost:4200 
 
### How to execute tests 

To execute a test on ElasTest, you need to create at least two elements:

- One Project
- One TJob associated to the project (you must attach to this, the docker image with the test to run)

To do this, you can use the API provided by the elastest-etm application or insert them directly into BBDD. 

At this point, you can run the TJob using the REST API provided by elatest-etm at http://localhost:8091/swagger-ui.html or the ElasTest graphical interface at http://localhost:4200/. 


### Component arquitecture

[Description of the architecture/structure of the 0.1 version of the component. This description should allow a developer to understand how source code is structured to start working on it if he/she wants to.]
