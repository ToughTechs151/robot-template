# Using the Robot Template repository

## Is this repository currently building correctly?
<!---
See https://docs.github.com/en/actions/monitoring-and-troubleshooting-workflows/adding-a-workflow-status-badge
for details on how to update the badge. The general format for the badge is
https://github.com/<OWNER>/<REPOSITORY>/actions/workflows/<WORKFLOW_FILE>/badge.svg

Be sure to change this when forking or renaming.
-->
[![CI][github-CI-badge]][github-CI]
[![Bugs][sonar-bugs]][sonar-project]
[![Code Smells][sonar-code-smells]][sonar-project]
[![Quality Gate Status][sonar-quality-gate]][sonar-project]
[![Duplicated Lines (%)][sonar-duplicated-lines]][sonar-project]
[![Reliability Rating][sonar-reliability-rating]][sonar-project]
[![Lines of Code][sonar-lines-of-code]][sonar-project]
[![Maintainability Rating][sonar-maintainability-rating]][sonar-project]
[![Issues][issues-image]][issues-url]

## Setting up your VScode environment

[Instructions on using VSCode with this template.](Docs/VSCode.md)

## Fork the robot-template

After you fork the template, you need to change repository for the CI badge in the README.md file,
and then set up a project in SonarCloud and change the badge URLs to the new project.

[github-CI-badge]: https://github.com/ToughTechs151/robot-template/actions/workflows/main.yml/badge.svg
[github-CI]: https://github.com/ToughTechs151/robot-template/actions/workflows/main.yml
[sonar-project]: https://sonarcloud.io/summary/new_code?id=ToughTechs151_robot-template
[sonar-bugs]: https://sonarcloud.io/api/project_badges/measure?project=ToughTechs151_robot-template&metric=bugs
[sonar-code-smells]: https://sonarcloud.io/api/project_badges/measure?project=ToughTechs151_robot-template&metric=code_smells
[sonar-quality-gate]: https://sonarcloud.io/api/project_badges/measure?project=ToughTechs151_robot-template&metric=alert_status
[sonar-duplicated-lines]: https://sonarcloud.io/api/project_badges/measure?project=ToughTechs151_robot-template&metric=duplicated_lines_density
[sonar-reliability-rating]: https://sonarcloud.io/api/project_badges/measure?project=ToughTechs151_robot-template&metric=reliability_rating
[sonar-lines-of-code]: https://sonarcloud.io/api/project_badges/measure?project=ToughTechs151_robot-template&metric=ncloc
[sonar-maintainability-rating]: https://sonarcloud.io/api/project_badges/measure?project=ToughTechs151_robot-template&metric=sqale_rating
[issues-image]: https://img.shields.io/github/issues-raw/ToughTechs151/robot-template.svg
[issues-url]: https://github.com/ToughTechs151/robot-template/issues
