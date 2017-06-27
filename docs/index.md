# Elastest Test Recommendation and Orchestrator Manager

The Test Orchestration and Recommendation Manager (TORM) is the brain of ElasTest and the main entry point for developers. TORM allows developers and testers to manage Projects, TJobs, SuTs, TOJobs, analyze logs, execute TJobs and SuTs and much more (the terms mentioned above are described in the [Features](#Features) section).

In then next diagram, you can to see The ElasTest TORM Components Architecture.

![ElasTest TORM Arquitecture](imgs/ElasTest_Torm_Architecture.png)

## Features

Features provided in the version 0.1 of the TORM component.

- Project Creation. Project is a set of TJobs and SuTs. 
- TJob Creation. TJob represents a test to execute over a SuT.
- SuT Creation.  SUT (System Under Test) represents a system to test. 
- TJob Execution. Is the action to execute a TJob (a test). This action create a TJob Execution entity and generate several information as a test logs, sut logs, metrics and test results, at this moment.
- Log Manager. Tool for analize stored logs of past executions. 

## Execution

[How to install the version 0.1 of component. This version should be installable wihout using any other ElasTest component. It is ok to provide a docker-compose.yml file to start the component dockerized with its dependencies.]

## Basic usage

To use ElasTest and run your first test, you need to create at least one project and a TJob associated to the project.

- **Create Project.** To do this, you can use the API provided by the elastest-torm application, use the GUI of elastest-torm or insert them directly into DDBB. 
    - *Using TORM API.* Go to http://localhost:8091/swagger-ui.html#!/Project/createProjectUsingPOST fill in the field `body` with the next JSON.
    ```json
    {
        "id": "0",
        "name": "projectX"
    }
    ```
    Click the button with the text `Try it out!`. Displays the `Response body` field with the response.
    ```json
    {
        "tojobs": null,
        "tjobs": null,
        "id": 1,
        "name": "prjectX",
        "suts": null
    }
    ```
    - *Using TORM GUI.* Go to http://localhost:8091/index.html and create a project from project management.

    ![ElasTest TORM Project Management](imgs/project-management.PNG)

    - *Using DB client.* Open any DB client to conneting to mysql (the mysql port is 3306). Execute the query:

    ```sql
    INSERT INTO `elastest-etm`.`Project` (name) VALUES ('name');
    ```

- **Create TJob.** To do this, you can use the API provided by the elastest-torm application, or insert them directly into DDBB.

    - *Using TORM API.*
    - *Using DB client.*

- **Run Test.** At this point, you can run the TJob using the REST API provided by elatest-torm at http://localhost:8091/swagger-ui.html or using the ElasTest graphical interface at http://localhost:4200/. 
    
    - *Using TORM API.*
    - *Using TORM GUI.*    

## Development documentation

Following are the necessary instructions to configure the elastest-etm component development environment. Some of the actions to be performed will depend on the SO. But the first thing is to download the repository code from GitHub:

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

- `docker-machine ssh` from power shell or any terminal emulator
- `sudo vi /var/lib/boot2docker/profile`
- set `DOCKER_TLS=no`
- `exit` from docker host
- `docker-machine restart`

When access to the API is active, the graphical client for Docker Toolbox and Windows CLI, will no longer be operational.
Now, if you want to work with docker you need to do `docker-machine ssh` from the terminal.

#### Install docker-compose on boo2docker

- `docker-machine ssh from Windows terminal
- ``curl -L https://github.com/docker/compose/releases/download/1.14.0/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose``
- `chmod +x /usr/local/bin/docker-compose`

#### Add shared folder to boot2docker on Virtual box
To provide access to project files from the docker host, you must share the project folder with boot2docker from virtual box.

#### Running elastest-etm services

- `docker-machine ssh` from windows terminal
- change the working directory to the shared folder of the project.
- `sudo sysctl -w vm.max_map_count=262144` (It is necessary for elasticsearch service)
- `docker-compose up -d`

### Docker configuration on Linux

- change the working directory to the project folder.
- `docker-compose up -d`

### Running elastest etm in development mode

#### Elastest-etm server application

- Build project with `mvn clean -Pdev package`
- Run Spring Boot application


#### Elastest-etm client application

- Open a terminal and change the working directory to the project folder
- `npm install`
- `npm start`

The graphical client will be accessible at http://localhost:4200 
 
### How to compile and execute tests 

[Precise instructions on how to compile the repository code and how to execute tests.]

### Component arquitecture

[Description of the architecture/structure of the 0.1 version of the component. This description should allow a developer to understand how source code is structured to start working on it if he/she wants to.]
