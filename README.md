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
- We use scalastyle for checking scala code. It should be included by default in intelliJ, check its activation in File -> Settings -> Editor -> Inspection
- For Java code we use checkstyle and PMD. Please install and active them.
- (Only for Windows user) Set the line delimiter to LF. 
- We use space instead of tabs, and tabs of 2 spaces. Be sure you follow this rules before starting to code.

#### Import procedure
- Install git on your system, if you haven't yet.
- Clone your fork with `git clone https://github.com/manuelperuzzi/Collabora`
- We use git flow, so for every new feature have to be implemented in a new branch called `<feature-*>`.
- For building the project use grade. You can delegate the intelliJ run action to gradle, go to File -> Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> Runner, and check Delegate IDE button run/builds to gradle. The tests are executed automatically at every build.
    - For running it outiside the IDE, and generate all the artifacts, run `./gradlew` if you are a Linux or Mac user, `.\gradlew.bat` if you are a Windows user. This generates automatically the fatJar and the java/scala doc.
    
### Developing the project
Contributions to this project are welcome (see the important message above). Just some rules:
1. We use [git flow](https://github.com/nvie/gitflow), so if you write new features, please do so in a separate `feature-` branch.
1. We recommend forking the project, developing your stuff, then contributing back via pull request directly from GitHub
1. Commit often. Do not throw at us pull requests with a single giant commit adding or changing the world. Split it in multiple commits and request a merge to the mainline often.
1. Do not introduce low quality code. All the new code must comply with the checker rules (we include an Idea inspection profile in the distribution) and must not introduce any other warning. Resolutions of existing warnings (if any is present) are very welcome instead.


[idea]: https://www.jetbrains.com/idea/
[mongo]: https://www.mongodb.com/