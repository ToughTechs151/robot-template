Open preferences page, search for "bracketpair". Look for the line that says:

Editor › Guides: Bracket Pairs
Controls whether bracket pair guides are enabled or not.

And set it to active.

## Pre-Installed extensions

- C/C++
- Debugger for Java
- Language Support for Java
- Project Manager for Java
- WPILIB

## Install these extensions

- Checkstyle for Java.
Once installed bring up the command palette and filter on "checkstyle". Select "Set the Checkstyle Configuration File". Select "/google_check.xml".
- Git Extension Pack (be sure to pick the one from Don Jayamanne, with over a million downloads.)
  - Git History
  - gitignore
  - GitLens
  - Open in Github, Bitbucket, Gitlab, Visualstudio.com
  - Project Manager
- Git Graph
- Github Pull Requests and Issues
- Intellicode
- Sonarlint
- Code Spell Checker


Spotbugs places report in the project directory under
build/reports/spotbugs in file main.html

Spotbugs exclusions are placed in config/spotbugs/excludeFilter.xml
Syntax is at https://spotbugs.readthedocs.io/en/stable/filter.html
