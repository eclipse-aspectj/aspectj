# Contributing to AspectJ

AspectJ is a maven project, as such it should import cleanly into your IDE. The project uses github for issue tracking ( https://github.com/eclipse/org.aspectj/issues ).

## Working on the codebase

### Importing the project

#### Eclipse

Simply run the maven project importer and point it at the root of the cloned AspectJ repository. This will import all of the AspectJ
modules.

Each module comes with its own testsuites however there is a module called `run-all-junit-tests` - within there is a file `RunTheseBeforeYouCommitTests` which you can launch as a JUnit test (rightclick -> RunAs -> Junit Test).
This will run a few thousand tests to verify your IDE import.

Some tests are conditional based on the JDK you are using in your IDE since they are exercising features only available in recent Java.
In order to execute all possible tests ensure you are running the tests with the latest available Java JDK release.
A JRE will likely not suffice because tools like javadoc will be invoked from the tests.

#### IntelliJ

TODO


### Developing tests

If developing system tests based on the sources for complete Java applications, follow the pattern in the `tests` module.
For each version of AspectJ there is a `bugsNNN` folder with subfolders for each issue.
Then there is a pair consisting of a test specification in a `ajcNNN.xml` file in the `src/test/resources` folder and a `AjcNNNTests.java` source file in the `src/main/java` folder.
Simply follow the pattern for previous versions to add a new suite for a new version of AspectJ.
Alongside the `AjcNNNTests` file you might add other test suites for particular new language features of Java.
All these suites are then pulled together in a `AllTestsAspectJNNN.java` suite.
In turn the suites are pulled together in a `AllTestsNN` suite for the major version of AspectJ, and so on.

Creating a test is then this basic process:
- create a new folder based on the issue number in the correct `bugsNNN` folder.
Add the relevant material into that folder (.java sources, any resources, xml files, etc).
- Create a definition of the test steps ('build these files', 'package this jar from these classes') in the correct `ajcNNN.xml` file in the `src/main/resources` area.
- Created a test in the correct `AjcNNNTests.java` file that references the specification written in the XML.

### Compiler issues

AspectJ is based on a modified Eclipse JDT that is using a modified grammar, extended to support AspectJ constructs.
This modified compiler exists in a separate repository: https://github.com/eclipse/aspectj.eclipse.jdt.core

Some issues involving compiler problems, for example if Java code isn't working because a variable was named after an AspectJ keyword - these issues must be worked on in that other project, even though the tests for that will likely live in this project.

## Contributions

Please contribute via Pull Request against the GitHub repository.

Contributors should ensure they have signed the [Eclipse Contributor Agreement](https://accounts.eclipse.org/user/7644/eca/3.1.0), this will be verified by automatic validation that occurs against any Pull Requests.