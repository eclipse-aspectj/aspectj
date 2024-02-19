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

To publish a snapshot, set up your credentials in `~/.m2/settings.xml` something like:

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

Next, you simply call:

```shell
mvn clean deploy 

# OR: If you ran tests locally before, or the CI workflow on GitHub did 
mvn -DskipTests clean deploy 

# OR: Speed it up some more, skipping documentation generation. Depending on
# your shell, you might not have to escape the '!' character for deactivating
# the 'create-docs' profile. On a (Git) Bash you have to, though.
mvn -P \!create-docs -DskipTests clean deploy 
```

This only deploys the main artifacts
  - AspectJ runtime `aspectjrt-[VERSION].jar`,
  - AspectJ tools/compiler `aspecttools-[VERSION].jar`,
  - AspectJ weaver `aspectjweaver-[VERSION].jar`,
  - AspectJ matcher `aspectjmatcher-[VERSION].jar`.

The AspectJ installer (`installer/target/aspectj-[VERSION].jar`) needs to be published separately, if you wish to make
it available to the public for this snapshot.

To consume an AspectJ snapshot published this way, use the OSSRH repository in the dependent project's POM:

```xml
<repository>
  <id>ossrh</id>
  <url>https://oss.sonatype.org/content/repositories/snapshots</url>
</repository>
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

If the AspectJ release also includes support for a new Java version, then before releasing AspectJ, search for the
term `AspectJ_JDK_Update` across all files in the code base, also non-Java ones. Check, that you have not forgotten to
add any necessary infrastructure or to increment version numbers as appropriate.  

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
mvn versions:set -DnewVersion=1.9.8.M2

# Verify if the POM changes are OK, then remove the POM backup files
mvn versions:commit

# Build and deploy the release to a Nexus staging repository.
# The 'release' profile will activate:
#   - Maven GPG plugin for signing artifacts (stand by to enter your passpharase).
#     On Windows, a GUI password dialogue should pop up with a recent GnuPG version.
#     In case of error 'Failed to execute goal org.apache.maven.plugins:maven-gpg-plugin:1.6:sign',
#     try 'export GPG_TTY=$(tty)' before running the command.
#   - Maven Javadoc plugin
#   - Nexus Staging Maven plugin
# The 'create-docs profile will make sure to generate AspectJ docs to be included in the installer. 
# Optionally, use '-DskipTests', if you ran all tests before.
mvn -P release,create-docs clean deploy
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
git commit -am "Set version to 1.9.8.M2"

# Tag release
git tag V1_9_8_M2

# Set new snapshot version, increasing the version number after a final release
mvn versions:set -DnewVersion=1.9.8-SNAPSHOT

# Verify if the POM changes are OK, then remove the POM backup files
mvn versions:commit

# Commit the snapshot POMs to Git
git commit -am "Set version to 1.9.8-SNAPSHOT"

# Push the previous commits to GitHub
git push origin

# Push the release tag to GitHub
git push origin V1_9_8_M2
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
# [INFO] orgaspectj-1106      CLOSED   org.aspectj:aspectjrt:1.9.8.M2

# Because of problems in Nexus Staging Maven Plugin with more recent JDKs,
# we might need this first
export MAVEN_OPTS="--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED"

# Use the ID of the corresponding CLOSED staging repository for releasing to
# Maven Central
mvn nexus-staging:rc-release -DstagingRepositoryId=orgaspectj-1106
```

Tadaa! We have performed an AspectJ release. In a few minutes, the artifacts should appear on Maven Central somewhere
under https://repo1.maven.org/maven2/org/aspectj/, e.g. AspectJ Tools 1.9.8.M2 would appear under
https://repo1.maven.org/maven2/org/aspectj/aspectjtools/1.9.8.M2/. As soon as you see the artifacts there instead of
"404 not found", you can announce release availability on the AspectJ mailing list and wherever else appropriate.

Finally, remember to publish the [release on GitHub](https://github.com/eclipse-aspectj/aspectj/releases), attaching
the AspectJ installer (`installer/target/aspectj-[VERSION].jar`) to it.

## Publish documentation on website

Content for the [AspectJ website](https://eclipse.dev/aspectj/) is maintained in GitHub repository
[eclipse-aspectj/aspectj-website](https://github.com/eclipse-aspectj/aspectj-website). As of writing this, there are a
few basic PHP pages (to be migrated to plain HTML by the end of 2024 when PHP support expires), but the bulk of the
project documentation is generated by the AspectJ project, i.e. this project. The asciidoc content and other
resources in module `docs` are transformed into what we want to publish on the website. Besides, the documentation is
also packaged into the AspectJ installer and published for offline use. On top of the `docs` content, we also publish
javadocs for the runtime and weaver APIs. 

After a full build, you can find the generated documentation including javadocs in folder `aj-build/dist/docs/doc`.
Conveniently, docs are also attached to GitHub CI builds, albeit currently without javadocs. The content goes to
folder `doc/latest` in the website repository or maybe to a folder named after the release, if we decide to do that in
the future. Presently, we only publish the latest documentation, always overwriting the preceding version. The entry
page for the generated docs is published [here](https://eclipse.dev/aspectj/doc/latest/index.html).

Make sure to **check the diffs** before committing:
  * If a certain part of the documentation (e.g. developer's notebook, programming guide) has not changed HTML-wise,
    you also do not need to commit the corresponding PDF, even though it might be binarily different. But if the HTML
    content is unchanged, the PDF should look the same as before, too, unless you have reason to believe otherwise
    (e.g. changes in style in the generator options).
  * Depending on your local OS and Git configuration, some changes might only be caused by different line delimiters,
    i.e. CRLF on Windows and LF on Linux and MacOS. Please avoid committing those, changing the files back and forth.
    Rather run dos2unix or unix2dos on those files to eliminate unnecessary and possibly huge diffs.  
  * You also want to identify other generic changes, such as timestamps, years in copyright notices etc. It is an
    ongoing process to optimise such changes away to achieve stable docs and small commit diffs. If you spot
    something, make sure to improve the build process accordingly. 

After pushing changes to the website repository, they should be published by a batch process automatically after a
short delay (usually a few minutes).

**Caveat:** The publishing process currently relies on fast-forwarding changes, i.e. if you e.g. amend or squash website
commits and then force-push them, the changes might not be published at all, and you need to open an Eclipse helpdesk
issue like [this one]() for the infrastructure team to propagate the changes manually. Knowing that, it is best not to
force-push, unless absolutely necessary (e.g. removing commits with sensitive information). 

After the website has been published, smoke-test the changes, opening some pages you know must have changed. 
