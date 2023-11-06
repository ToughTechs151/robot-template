# Cloning This Template

## Click "Use this template"

## Create new SonarQube project for the new repo
### Add new repo to list of repos accessible to SonarQube:
* Navigate to the repo on Github.com
* Select repo settings
* Select Github apps
* Click SonarCloud - configure
* Add any missing permissions.
* Add repo to list under "Select repositories"

### Add repo to list of Projects on SonarQube
* Log into SonarQube at https://sonarcloud.io
* press the "+" on your home page to create a new project
* Choose your organization
* Choose you repo.
* Press setup
* Under Administration, Analysis Method, turn off automatic analysis.
* Github workflow analysis
* Create secret name SONAR_TOKEN, value from setup
  * Click on your profile picture
  * Click on "My Account"
  * Select the "Security Tab"
  * Fill in the new token name and click "Generate Token"
  * Copy the token value
  * Go back to Github, open repo settings.
  * Under "Security", open "Secrets and variables".
  * Select "Actions", and clicK "New Repository Secret".
  * Create a secret called "SONAR_TOKEN" with the value you copied before, then click "Add Secret".
## Things to change in the Repo before first use

- Repo name in README.md. File find and replace "robot-template" to new name.
- Team number in settings.json
- Sonarqube account/project in README.md
- sonarqube account/project in .vscode/settings.json
- sonarqube account/project in build.gradle
