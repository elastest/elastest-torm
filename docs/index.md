# Elastest Test Orchestrator and Recommendation Manager

The Test Orchestration and Recommendation Manager (TORM) is the brain of ElasTest and the main entry point for developers. TORM will provide a web interface to be used by testers, developers and administrators, to managing tests, test executions, systems under test, orchestrate tests and analyze logs. It will also provide a remote API that will used by ElasTest-CLI (a command line interface tool), by ElasTest Jenkins Plugin and by ElasTest Eclipse Plugin.

Before you start using ElasTest, you need to know the following terms:

- **Project:** Set of test specifications.
- **TJob:** Specification of a Test to run against any software.
- **SuT (System under Test):** Specification of the System that is being tested for correct operation.
- **TSS (Test Support Services):** On-demand services that provide additional functionality such as dynamic provisioning of web browsers, or devices for testing. These services can be provisioned associated with a TJob, to be used by the tests.
- **TE (Test Engines):** On-demand services that unlike TSS, will not be associated with a particular TJob, but will serve all TJobs running on the platform.

## Features

Check [here](https://elastest.io/docs/releases/) the features of current version of ElasTest

## How to run

To start using ElasTest, you need to follow the next steps. Several of these steps, are specific for Windows, Mac or Linux Operating Systems in the [official documentation](https://elastest.io/docs/try-elastest/)

## Development documentation

### Arquitecture

The ElasTest TORM Platform is divide in three parts:

- ElasTest TORM Web Client Application.
- ElasTest TORM Server Application.
- ElasTest Services.

In the next diagram, you can to see The ElasTest TORM Components Architecture.

![ElasTest TORM Arquitecture](imgs/ElasTest_Torm_Development_Architecture.png)

#### ElasTest TORM Web Client Application

This appication provides a friendly GUI to ElasTest TORM Platform, allowing to the users managment theirs test in a simple way. 

#### ElasTest TORM Server Application 

This application is the ElasTest TORM backend that provides the API to the Web Client Application to access the resources and implements integration with the rest of the ElasTest services. It is a Java Application developed with SpringBoot, the Spring solution for creating stand-alone applications as quickly as possible.

#### ElasTest TORM Services

ElasTest TORM uses several external components to implement the features it offers. These services are shown in the diagram and described below.

- **[MySql DB:](https://www.mysql.com/)** The DDBB System that uses the ElasTest TORM to store the persistent data, necessary to manage the Projects, TJobs, Suts and Executions.
- **[Logstash:](https://www.elastic.co/products/logstash)** As indicated on its website *"It is a server-side data processing pipeline that ingests data from a multitude of sources simultaneously, transforms it, and then sends it to your favorite *stash*"*. ElasTest TORM uses it to gather and parse logs and metrics produced by the execution of TJobs and SuTs. The logs and metrics are sent to Elasticsearch and RabbitMq servers.
- **[Dockbeat:](https://www.elastic.co/products/beats)** As indicated on its website "*Beats is the platform for single-purpose data shippers. They install as lightweight agents and send data from hundreds or thousands of machines to Logstash or Elasticsearch*". ElasTest TORM uses it to retrive container metrics generated from the docker containers executing TJobs and SuTs and send them to Logstash service.
- **[RabbitMQ:](https://www.rabbitmq.com/)** It is a message broker used as communication bus in ElasTest. It is used by ElasTest TORM to show in the web GUI metrics and logs.

### Prepare development environment

First, be sure you can execute ElasTest TORM in production as specified in section [How to run](https://github.com/elastest/elastest-torm/blob/master/docs/index.md#how-to-run).

Then, install the following development tools:

- [Java JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven 3.3.9](https://maven.apache.org/download.cgi)
- [Eclipse IDE](https://eclipse.org/ide/) or similar for Javan development.
- [Visual Studio Code](https://code.visualstudio.com/) or similar for Angular development.
- [Angular CLI](https://cli.angular.io/)

Last, clone the repository in a local folder (for example, `/git`):

```
cd /git
git clone https://github.com/elastest/elastest-torm
```

> **Note:** In windows, only folders within `C:\Users\` can be used inside Docker VM. If you clone the git repository outside of `C:\Users\`, then you need to share git folder with the VM in VirtualBox interface following [these instructions](http://support.divio.com/local-development/docker/how-to-use-a-directory-outside-cusers-with-docker-toolbox-on-windows).

### Development procedure

First, you need to start the ElasTest Services, before you can execute TORM Server and Client applications. You can do it in two ways:

- Start the services using the [*ElasTest Platform*](https://github.com/elastest/elastest-toolbox) tool. This is the preferred option as it can be used on any operating system with docker installed.
- Start the services using the *docker-compose* tool. On Windows it will be necessary to install Docker Compose inside the boo2docker VM.

#### Start ElasTest TORM Services using ElasTest Platform

If you choose this option, you only need to execute the following command:

```docker run --rm -v /var/run/docker.sock:/var/run/docker.sock elastest/platform start```

If you want ports binding, add `--dev` option after `start`

>**Note:** For more information about this command you can see the ElasTest Platform [documentation](https://github.com/elastest/elastest-toolbox/blob/master/docs/index.md#start-command).

#### Stop ElasTest TORM Services using ElasTest Platform

To stop the services, you must run the following command:

- `docker run --rm -v /var/run/docker.sock:/var/run/docker.sock elastest/platform stop`

>**Note:** Possibly also pressing `Ctrl+C` in the shell should work, but make sure all the docker environment has been actually clean.

#### Start ElasTest TORM Server Application

Depending on whether you are using [Docker](https://www.docker.com/what-docker) or [Docker Toolbox Tool](https://www.docker.com/products/docker-toolbox), you must define the following environment variables:

|                       | With Docker Toolbox                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   | With Docker                                                                                                                           |
| --------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| Environment Variables | <ul><li>`ET_PUBLIC_HOST=docker-machine-ip` => VM Ip where ElasTest is running. The *docker-machine-ip* value, should be changed to the value returned by the execution of the `docker-machine ip` command. </li><li>`ET_IN_PROD`=false => The false value of this variable indicates that you want to run ElasTest in development mode</li><li>`DOCKER_TLS_VERIFY=1`</li><li>`SET DOCKER_HOST=tcp://docker-machine-ip:2376` (replace docker-machine-ip with result of execute the command `docker-machine ip`)</li><li>`DOCKER_CERT_PATH=/Users/logedUser\.docker\machine\machines\default`</li></ul> | <ul><li>`ET_IN_PROD`=false => The false value of this variable indicates that you want to run ElasTest in development mode.</li></ul> |

You can develop ElasTest TORM Server Application using an editor and the command line or using Eclipse IDE:

- Using *Eclipse IDE*:
  - **Load project** in the IDE:
    - Import *elastest-torm* project from local Git Repository using `File > Import... > Maven > Import existing project` option and select the `/git/elastest-torm/elastest-torm` folder.
  - **Build project**:
    - First, you need build `epm-client`. Right click over the project and select `Run as..> Maven build`. In Goals type `clean install -Dgpg.skip`.
    - After, if you want to run ElasTest mini, you need [build `EUS`](https://github.com/elastest/elastest-user-emulator-service/blob/master/docs/dev-docs.md#eus-server-application-1) as dependency.
    - Lastly, build ETM. Right click over the project and select `Run as..> Maven build`. In Goals type `clean package -Dmaven.javadoc.skip=true -DskipTests`.
  - **Import Launch configurations**. Go to `File..>Import..` and select `Run/Debug..>Launch configurations`. Select `elastest-torm/docs` folder and import all files with `.launch` extension
    - Note: edit `ET_DATA_IN_HOST` and `ET_SHARED_FOLDER` to set your `.elastest` folder path
  - **Execute the project**, doing right click over the project and select `Run as ..> SpringBoot Application`

- Using editor and console:
  - Compile and execute the project: 
    - Go to the root directory of the project with `cd /git/elastest-torm/elastest-torm`
    - Configure the property file in `src/main/resources/application.properties`
    - Compile the project `mvn clean package`
    - Execute the expring boot application with the command `mvn spring-boot:run` by adding the necessary environmet variables `mvn spring-boot:run -DmyEnvVariable=value....`

     >**Note:** Building the project may require root privileges for running the test `DockerServiceItTest`. Either execute the TORM as root or skip such test.

The server application can be used from the web interface [(see next section)](https://github.com/elastest/elastest-torm/blob/master/docs/index.md#start-elastest-torm-client-application). By default the endpoint of the ElasTest TORM Server Application should be located at URL `http://localhost:8091`, 
the exposed API could be checked at `http://localhost:8091/swagger-ui.html#/`.

If you change any source file, you need to stop the service and start it again.

#### Start ElasTest TORM Client Application

You can develop ElasTest TORM Web Client Application using and editor and the command line or using Visual Studio Code:

- Using *Visual Studio Code*:
  - Load project in the IDE:
    - Open the project folder using `File > Open folder` option and select the `/git/elastest-torm/elastest-torm-gui`.
    - Open the integrated terminal with `View > Integrated Terminal`
    - Execute `npm install` to download the libraries
  - Compile and execute the project:
    - Execute `npm start`

- Using editor and console:
  - Prepare the project:
    - Go to the project folder with `cd /git/elastest-torm/elastest-torm-gui`
    - Execute `npm install` to download the libraries
  - Compile and execute the project:
    - Execute `npm start`

The client application can be used loading the URL http://localhost:4200 in a browser.

If you change any source file, the client will be restarted automatically on save.
