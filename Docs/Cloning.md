# Cloning This Template

## Click "Use this template"

## Create new SonarQube project for the new repo

### Add repo to list of Projects on SonarQube
* Log into SonarQube at https://sonarcloud.io
* press the "+" on your home page and select "analyze new project" to create a new project
* Choose your organization
* Choose you repo. Select it with a checkmark if it is there. if not then:
  * Click "GitHub app configuration" at the bottom of the page.
  * Click SonarCloud - Configure.
  * Pull down menu that says "Select repositories"
  * Select your new repository.
  * Click "Save"
  * Close Previous SonarCloud browser tab, and start this section over, starting from "Log into sonarQube"
* Press setup, select "Previous version", click "Create project"
* On the new project page, under Administration, Analysis Method, turn off automatic analysis.
* Click "With GitHub Actions"
* Create secret name SONAR_TOKEN in your repo. Follow directions under "1 Create GitHub Secret"
* Under "2 Create or Update a build file", select Gradle and Groovy.
* Update the build.gradle and .github/workflows/main.yml as directed. Don't worry about the file name being different.

## Things to change in the Repo before first use

- Repo name in README.md. File find and replace "robot-template" to new name.
- Team number in settings.json
- Sonarqube account/project in README.md
- sonarqube account/project in .vscode/settings.json
- sonarqube account/project in build.gradle
