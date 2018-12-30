:warning: This project is effectively abandoned, this repository is here for archival purpose. Feel free to contact `tomer at tomergabel dot com` for any questions.

# Overview
ant-intellij-tasks is a self-contained build system for IntelliJ IDEA projects based around [Apache Ant](). In essence, ant-intellij-tasks comprises three components:

1. An Ant task library that can extract and resolve the IntelliJ IDEA project and module files (.ipr and .iml respectively), and provides a set of tasks and conditions around the project structure;
2. A common build script which provides the four major build targets for modules: clean, build, test and package (see the quickstart guide);
3. A master build script which extends these targets to the entire project.
The build system is designed to be extensible (e.g. by adding targets), customizable (e.g. by overriding a target's behavior for a specific module) and self contained in that it's a drop-in solution that should not require any significant modifications to the code base.

This project is fully open source (distributed under an Apache license) and hosted at Google Code. Please report any bugs or issues on the project issue tracker.

ant-intellij-tasks makes use of, and redistributes, the [ant-contrib](http://ant-contrib.sourceforge.net/) task library.

# Rationale
While IntelliJ IDEA excels at managing your project structure, it cannot be cheaply and reliably automated by build scripts. To use continuous integration servers (such as CruiseControl, Hudson or TeamCity) a developer is forced to write, maintain and keep synchronized a dual project structure consisting of IntelliJ project/module files and Ant build scripts.

ant-intellij-tasks aims to bridge the gap: it provides a flexible set of Ant tasks which can parse and resolve IntelliJ project/module files, as well as a common build script that automates common project- and module-level tasks, primarily building and testing.

# Quickstart Guide
To use ant-intellij-tasks you need to extract the archive to your project directory. Specifically, the following four files are required:

* ant-intellij-tasks-1.0.jar (replace 1.0 with the appropriate version)
* ant-contrib.jar
* common.xml
* build-all.xml

You can invoke the build directly by specifying the build-all.xml script (e.g. `ant -f build-all.xml`), or you can import build-all.xml to roll out your own build script:

```xml
<?xml version="1.0" ?>
<project default="build">
  <import file="resources/build-all.xml" /> 
</project>
```

The build targets provided are:

1. **clean** target: cleans up the build output for all modules.
2. **build** target: builds the project.
3. **test** target: Builds and tests the project. Tests are run for files matching one of the following patterns:
   * Test?*.java
   * ?*Test.java
   * ?*Tests.java Where ? resolves to a single character and * resolves to zero or more characters. Tests may be further customized; please refer to the documentation for the `test-module` macro in common.xml for details.
* **package** target: Builds and packages all modules in the project. The exact output is determined by the module and project settings in IntelliJ IDEA:
  * If the project specifies "Build JARs on make" and JAR settings arespecified for the module, a JAR is created;
  * If the module has a Web facet with WAR or exploded output enabled, the module output (including web descriptor and output settings as defined in IntelliJ IDEA) is created accordingly.

Each module may specify its own build.xml, which will override the default behavior for these targets. The script will be invoked using the same target, with the `profile-file` property pointing at the project file used. Please refer to the documentation in common.xml for details on how to customize your builds further.
