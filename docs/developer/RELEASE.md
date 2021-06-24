# How to release AspectJ

AspectJ is built and released with Maven. As of writing this, there is a Maven wrapper in the project root folder,
pointing to Maven 3.6.3, but we are going to use simple `mvn` commands instead of `./mvnw` here, assuming that there is
a local Maven installation on your workstation. You can easily substitute one for the other command.

When deploying final releases to Sonatype OSSRH, the build uses Nexus Staging Maven plugin instead of Maven Deploy
plugin. This helps to create a staging repository for the release and later release it to Maven Central without having
to log into the [Sonatype Nexus web UI](https://oss.sonatype.org/). Everything can be done from the command line.

Snapshots do not need to be staged and released separately, Maven Deploy does the job in this case. so let us begin with
the simple case:

## Snapshot releases

To publish a snapshot, set up your credentials in `.m2/settings.xml` something like:

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>USERNAME</username>
      <password>PASSWORD</password>
    </server>
  </servers>
</settings>
```

Assuming that you are currently working on version 1.9.7-SNAPSHOT, you simply call:

```shell
mvn clean deploy 

# OR: If you ran tests locally before, or the CI workflow on GitHub did 
mvn -DskipTests=true clean deploy 

# OR: Speed it up some more, skipping documentation generation. Depending on
# your shell, you might not have to escape the '!' character for deactivating
# the 'create-docs' profile. On a (Git) Bash you have to, though.
mvn -P \!create-docs -DskipTests=true clean deploy 
```

This only deploys the main artifacts
  - AspectJ runtime `aspectjrt-[VERSION].jar`,
  - AspectJ tools/compiler `aspecttools-[VERSION].jar`,
  - AspectJ weaver `aspectjweaver-[VERSION].jar`,
  - AspectJ matcher `aspectjmatcher-[VERSION].jar`.

The AspectJ installer (`installer/target/aspectj-[VERSION].jar`) needs to be published separately, if you wish to make
it available to the public for this snapshot.

To consume a snapshot, use the ossrh repository:

```xml
<repository>
  <id>ossrh</id>
  <url>https://oss.sonatype.org/content/repositories/snapshots</url>
</repository>
```

```xml
<pluginRepository>
  <id>ossrh</id>
  <url>https://oss.sonatype.org/content/repositories/snapshots</url>
</pluginRepository>
```


## Public releases (milestone, release candidate, final)

The artifacts released are the same as for snapshots, the procedure needs a few more steps, though. I am explaining the
manual versioning process without using Maven Release plugin. It might work using Maven Release too, i.e.
  - setting the release version in all POMs,
  - building a release,
  - running tests (can be skipped),
  - committing the release POMs,
  - tagging the release,
  - deploying the release,
  - setting the next snapshot version in all POMs,
  - committing the snapshot POMs,
  - pushing the previous commits and the release tag to the upstream Git repository.

In order to show the details and give you more control over the process, you can do it step by step as follows:

```shell
# Make sure we are on JDK 16, because javadoc generation is JDK version sensitive
# and might throw unexpected errors on other versions
java -version
# java version "16" 2021-03-16 (...)

# Verify that we are working on a clean working directory.
# There should be no staged, unstaged or untracked files.
git status

# Set release version in all POMs
mvn versions:set -DnewVersion=1.9.7.M2

# Verify if the POM changes are OK, then remove the POM backup files
mvn versions:commit

# Set some environment variables needed by Nexus Staging Maven plugin on JDK 16,
# until https://issues.sonatype.org/browse/OSSRH-66257 is resolved
export MAVEN_OPTS="--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED"

# Build and deploy the release to a Nexus staging repository.
# The 'release' profile will activate
#   - Maven GPG plugin for signing artifacts (stand by to enter your passpharase!),
#   - Maven Javadoc plugin,
#   - Nexus Staging Maven plugin.
# Optionally, use '-DskipTests=true', if you ran all tests before.
mvn -P release clean deploy
```

If this command was successful, it means we have created a staging repository on Sonatype OSSRH, uploaded all artifacts
and all pre-release checks on the Sonatype server passed, i.e. if the POMs contain all necessary information and if
there are source and javadoc artifacts attached to the build. Now the only step left is to release the staging
repository to Maven Central.  

Actually, Nexus Staging Maven plugin can also be configured deploy and release to Maven Central in a single command, but
in order to give you a chance to manually download and verify the artifacts from the staging repository, the default
plugin configuration in the parent POM is `<autoReleaseAfterClose>false</autoReleaseAfterClose>`. Switching the value to
`true` would release straight to Maven Central, given all previous steps were successful. 

Before we release the staging repository though, we want to commit and tag the release, then set a new snapshot version:

```shell
# Commit the release POMs to Git (better do this from your IDE, verifying the
# changes before staging them for Git commit)
git commit -am "Set version to 1.9.7.M2"

# Tag release
git tag V1_9_7_M2

# Set new snapshot version, increasing the version number after a final release
mvn versions:set -DnewVersion=1.9.7-SNAPSHOT

# Verify if the POM changes are OK, then remove the POM backup files
mvn versions:commit

# Commit the snapshot POMs to Git
git commit -am "Set version to 1.9.7-SNAPSHOT"

# Push the previous commits to GitHub
git push origin

# Push the release tag to GitHub
git push origin V1_9_7_M2
```

OK, the Git house-keeping is done. Now finally, let us enjoy the fruits of our work and release the staging repository
to Maven Central:

```shell
# Probably we forgot to write down the staging repository ID before.
# It was written somewhere in the Maven log:
#   [INFO]  * Created staging repository with ID "orgaspectj-1106".
#   [INFO]  * Staging repository at https://oss.sonatype.org:443/service/local/staging/deployByRepositoryId/orgaspectj-1106
#   ...
#   [INFO]  * Uploading locally staged artifacts to profile org.aspectj
#   [INFO]  * Upload of locally staged artifacts finished.
#   [INFO]  * Closing staging repository with ID "orgaspectj-1106".
#
# But it is too far to scroll up. So let us just ask Nexus, which staging
# repositories there are.
mvn nexus-staging:rc-list
# [INFO] ID                   State    Description
# [INFO] orgaspectj-1106      CLOSED   org.aspectj:aspectjrt:1.9.7.M2

# Use the ID of the corresponding CLOSED staging repository for releasing to
# Maven Central
mvn nexus-staging:rc-release -DstagingRepositoryId=orgaspectj-1106
```

Tadaa! We have performed an AspectJ release. In a few minutes, the artifacts should appear on Maven Central somewhere
under https://repo1.maven.org/maven2/org/aspectj/, e.g. AspectJ Tools 1.9.7.M2 would appear under
https://repo1.maven.org/maven2/org/aspectj/aspectjtools/1.9.7.M2/. As soon as you see the artifacts there instead of
"404 not found", you can announce release availability on the AspectJ mailing list and wherever else appropriate.

Finally, you probably want to publish the AspectJ installer (`installer/target/aspectj-[VERSION].jar`), e.g. by creating a
GitHub release and attaching artifacts and/or updating the Eclipse AspectJ website. You also want to update the AspectJ
documentation, if there were any changes.

## Deploying the AspectJ installer to aspectj.dev

An easy way to quickly publish the installer is to simply deploy it to the Maven repository aspectj.dev. In order to do
that, you need to mount the target directory as a WebDAV share first (ask an AspectJ maintainer for credentials). This
can be done on all operating systems, for this example let us assume we are working on Windows and already have mounted
the share to drive letter M: (M like Maven). Command `net use` would show something like this (sorry, in German):

```text
C:\Users\me>net use
...
Status       Lokal     Remote                    Netzwerk
-------------------------------------------------------------------------------
OK           M:        \\s000b153.kasserver.com\s000b153
                                                Microsoft Windows Network
...
```

Next, we need to tell Maven to
  - actually deploy the installer (remember, by default only the artifacts listed above are deployed),
  - override the default deployment repository (Sonatype OSSRH) by our WebDAV share.

Before issuing the following command, make sure that you successfully built AspectJ before. Otherwise, Maven cannot find
the artifacts it needs to create the installer JAR.

```shell
mvn --projects installer -Dmaven.deploy.skip=false -DaltDeploymentRepository=aspectj-dev::default::file:///M: deploy
```
