# Cloning This Template

## Click "Use this template"

## Create new SonarQube project for the new repo
Add new repo to list of repos accessible to SonarQube
    Repo github settings
    Github apps
    SonarCloud - configure
    Add any missing permissions.
    add repo to list.

Log into SonarQube at https://sonarcloud.io
press the "+" on your home page to create a new project
Choose your organization
choose you repo.
press setup
github workflow analysis
create secret name SONAR_TOKEN, value from setup
update project key in build.gradle



## Things to change

- repo name in README.md
- Team number in settings.json
- Sonarqube account/project in README.md
- sonarqube account/project in .vscode/settings.json
- sonarqube account/project in build.gradle
