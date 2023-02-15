# Preparing your VSCode environment for use with the robot-template.
## Install extensions

### How to install extensions in VSCode
To install extensions in VSCode, you first click on the extensions side bar (or type Ctrl+Shift+X). ![extension tab icon](img/ext-tab.jpg) It will show you a list of your installed extensions. Use this list to verify that you have the extensions in the following section installed. To search of an extension, simply type its name in the search field at the top of the extensions panel and it will display all the extensions in the extension marketplace that have those words. When you find the one you want, click on it to bring up an editor window with its description page. To install it, click on the "Install" button, either on the description page, or the one next to its name in the extensions list. After the extension is install, the install button on the list will change to settings gear icon, and the button on description will change to "Uninstall". You can use this "Uninstall" button to uninstall the extension if you no longer want it.
### Pre-Installed extensions
The following extensions come preinstalled in the WPILIB VSCode environment. If they are not installed, then you are not using the correct version of VSCode.

- C/C++
- Debugger for Java
- Language Support for Java
- Project Manager for Java
- WPILIB

### Install these extensions
These extensions need to be installed in you WPILIB VSCode. Some of are extension packs, which means that installing them actually causes a set of extensions to be installed. Extension packs here will have an indented list of extensions that the pack will install. You will not need to install these individually. In a later section in this document we will deal with how to set these extensions up, but for now, just install them. 



- Checkstyle for Java.
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
- Gradle Extension Pack
  - Gradle for Java
  - Gradle Language Support

## Configure settings for VSCode with extensions.
### How to set VSCode preferences.
VSCode stores its settings in files called settings.json. 


Spotbugs places report in the project directory under
build/reports/spotbugs in file main.html

Spotbugs exclusions are placed in config/spotbugs/excludeFilter.xml
Syntax is at https://spotbugs.readthedocs.io/en/stable/filter.html
Open preferences page, search for "bracketpair". Look for the line that says:

Editor › Guides: Bracket Pairs
Controls whether bracket pair guides are enabled or not.

And set it to active.


- Checkstyle for Java.
Once installed bring up the command palette and filter on "checkstyle". Select "Set the Checkstyle Configuration File". Select "/google_check.xml".