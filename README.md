# Collabora
The universal collaboration tool, from big projects to shopping lists.

## Important message
This repository contains a university course project. Please, **do not fork it or send us your ideas until this message is here** (~ mid September 2017).


## Status badge
### Stable branch
[![Build Status](https://travis-ci.org/manuelperuzzi/Collabora.svg?branch=master)](https://travis-ci.org/manuelperuzzi/Collabora)
### Develop branch
[![Build Status](https://travis-ci.org/manuelperuzzi/Collabora.svg?branch=develop)](https://travis-ci.org/manuelperuzzi/Collabora)


## Features
- Store information about a note inserted by a client
    - textual content
    - location, to handle location-based notifications
    - expiration, to allow custom alarms
    - previous notes, to introduce a bond of precedence between notes
    - state, to promote the organization of work
- Organization of clients in collaborations
    - private, unique collaboration for the personal user notes
    - groups, simple collaborations for everyday situations
    - projects, elaborate collaborations, structured in modules, for work and university related projects
- Send notifications to the clients about the state of the notes in their collaborations
- Management of an access rights policy in a collaboration
    - a client with a read access can only visualize notes and modules
    - a client with a write access can also create, update and delete notes and modules
    - an admin client, in addition to having a write access, can edit/remove the collaboration, add/remove members and change their access rights
- Management of client data
    - the user sensible information, acquired at the registration stage, are not divulged
    - login required in order to access private data

## Notes for developers

### Importing the project
The project has been developed using IntelliJ Idea, and can be easily imported in such IDE.
#### Recommended configuration
- Download the latest [latest intelliJ version][idea], please use the 2017 version for a smooth import.
- In order to run the project, rabbitmq is needed. Please, install and run it.
- For working with the database you need mongo. Please, follow the installation guide [here][mongo].
- Be sure that your java version is at least 1.8, and scala version is at least 2.12.0.

#### IDE settings
- We use scalastyle for checking scala code. It should be included by default in IntelliJ, so check its activation in File -> Settings -> Editor -> Inspection. We include an inspector profile with the source code. Be sure to follow our code rules.
- For Java code we use checkstyle and PMD. Please install and active them.
- (Only for Windows user) Set the line delimiter to LF.
- We use space instead of tabs, and tab of 2 spaces. Be sure you follow this rules before starting to code.

#### Import procedure
- Install git on your system, if you haven't yet.
- Fork this repository in github.
- Clone your forked project with `git clone https://github.com/<your-username>/Collabora`
- Open IntelliJ, and go to File -> Open. In the window select the folder of the project, the import procedure should be smooth.
- For building the project use grade. You can delegate the IntelliJ run action to gradle, go to File -> Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> Runner, and check Delegate IDE button run/builds to gradle. The tests are executed automatically after every build.
    - For bulding it outiside the IDE, and generate all the artifacts, run `./gradlew` if you are a Linux or Mac user, `.\gradlew.bat` if you are a Windows user. This generates automatically the fatJar and the java/scala doc.

#### Run the collabora server
- Be sure you have `mongo`, `mongorestore` and `rabbimq` installed on your system.
- First run mongo, pull up a terminal, and type `mongod -dbpath <your-working-mongo-directory>`. You can run it without `-dbpath` option, mongo will create its working directory somewhere in the system.
- Restore the database. Run `mongorestore --drop -d collabora ./MongoDBBackup/collabora`. A dumped version of the DB is shipped with the source code, in the directory indicated.
- Run rabbitmq. In \*nix systems open a terminal and type `sudo rabbitmq-server`.
- Now you are able to run the server.
  - If you want to run it from the jar, pull up a terminal, and type `java -jar ./<jar-name> <your-ip-address>`. If yu don't pass any argument, the default IP address is `localhost`.
  - If you want to run it from IntelliJ, just right click on the object `EntryPoint` and click on `Run EntryPoint`. You should pass your ip as command line argument. Go to Run -> Edit Configurations and type your IP in program arguments text box.
  - If you want to run it with gradle, open a terminal, cd to the main project directory (the directory that contains this file), and type:
    - `./gradlew run` if you are a linux or mac user.
    - `.\gradlew.bat run` if you are a windows user.

### Developing the project
Contributions to this project are welcome (see the important message above). Just some rules:
1. We use [git flow](https://github.com/nvie/gitflow), so if you write new features, please do so in a separate `feature-` branch.
1. We recommend forking the project, developing your stuff, then contributing back via pull request directly from GitHub
1. Commit often. Do not throw at us pull requests with a single giant commit adding or changing the world. Split it in multiple commits and request a merge to the mainline often.
1. Do not introduce low quality code. All the new code must comply with the checker rules (we include an Idea inspection profile in the distribution) and must not introduce any other warning. Resolutions of existing warnings (if any is present) are very welcome instead.

### Related projects
The android application of the collabora project can be found [here][app]


### FAQ
- I found a bug/mistake/something wrong. How can I report that?
  - Open an issue in `https://github.com/manuelperuzzi/Collabora`, or, if you are interested in the project, fix it and send us a pull request.
- The server is running, but the app isn't connected.
  - First be sure that you provide at the server the correct IP of your machine. Second be sure that the IP of the server is correcty set in the app. Third rabbitmq, in some systems, is configured by default to not accept requests outside localhost. Try to add `[{rabbit, [{loopback_users, []}]}].` to your rabbitmq config file.
- Are mongo and rabbitmq necessary for running the server?
  - Yes, the server can't run without them.
- The application don't build. What can I do?
  - Check if the travis build is ok, in status badges above. If not is our problem. If the remote build is ok be sure to have mongo and rabbitmq running in your system. If the problem persist open an issue in `https://github.com/manuelperuzzi/Collabora`.
- Some tests fails, but the build is ok, What can I do?
  - See the question above.
- Is it mandatory to run all the tests at every build?
  - No, but is strongly suggested.
- I only have the source code, how can I produce the Jar?
  - Pull up a terminal, in the main project directory, and type `./gradlew shadowJar`. The jar will be in build/libs.
- I don't have gradle in my system, is a problem?
  - No! Just use the gradle wrapper, or the gradle plugin in IntelliJ.
- IntelliJ marks some code as errored, but its compiling. Why?
  - It's an IDE problem. We can do nothing about that :disappointed:
- Can I use another IDE/texteditor for developing the project?
  - Yes, but at your own risk. Just be sure that your code follow our code rules.
- I want the HTML version of the scaladoc. Where it is?
  - You can download it from the release tab of the project, on GitHub, or you can generete it by yourself, running `./gradlew scalaDoc`, the docs appears in build/doc
- I am interested in the project, how can I contact a member of the team?
  - Send us an email, or start contributing to the project. In any case you are very welcome!


[idea]: https://www.jetbrains.com/idea/
[mongo]: https://www.mongodb.com/
[app]: https://github.com/manuelperuzzi/Collabora-android-app
