# How to release AspectJ

AspectJ is built and released with Maven. As of writing this, there is a Maven wrapper in the project root folder,
pointing to Maven 3.6.3, but we are going to use simple `mvn` commands instead of `./mvnw` here, assuming that there is
a local Maven installation on your workstation. You can easily substitute one for the other command.

Final releases are published to Maven Central via the [Central Publisher Portal](https://central.sonatype.com/publish/publish-portal-upload/).
The build process creates a bundle archive following the Maven Repository Layout, which is then uploaded manually through the portal.

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
  - preparing the portal bundle,
  - uploading to Maven Central Portal,
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
```

### Preparing the Portal Bundle

Before building the release bundle, you can do a dry-run to verify everything will work correctly:

```shell
# Dry-run: Build artifacts and create bundle without GPG signatures
# This allows you to verify the bundle structure before the actual release
./scripts/prepare-portal-bundle.sh --dry-run

# Verify the bundle structure
unzip -l target/portal-bundle-1.9.8.M2.zip | grep 'org/aspectj'
```

The dry-run creates a bundle identical to the production bundle, except it lacks GPG signatures. This allows you to:
- Verify the Maven Repository Layout structure is correct
- Check that all required artifacts are included (JARs, sources, javadoc, POMs)
- Confirm checksums are present and correctly formatted
- Test the bundle extraction

Once you're satisfied with the dry-run, prepare the production bundle:

```shell
# Build, sign, and create the portal bundle
# The 'release' profile will activate:
#   - Maven GPG plugin for signing artifacts (stand by to enter your passphrase).
#     On Windows, a GUI password dialogue should pop up with a recent GnuPG version.
#     In case of error 'Failed to execute goal org.apache.maven.plugins:maven-gpg-plugin:1.6:sign',
#     try 'export GPG_TTY=$(tty)' before running the command.
#   - Maven Javadoc plugin
# The 'create-docs' profile will make sure to generate AspectJ docs to be included in the installer.
# Optionally, use '--skip-tests', if you ran all tests before.
# Note: The script explicitly skips Maven deploy (-Dmaven.deploy.skip=true) since releases
# are uploaded via Central Portal, not through Maven deploy.
./scripts/prepare-portal-bundle.sh

# OR: Run Maven commands directly (deploy is skipped)
# mvn -P release,create-docs clean verify -Dmaven.deploy.skip=true
# mvn assembly:single -Ddescriptor=portal-bundle
```

The script will:
1. Build all artifacts (`mvn clean verify -P release`)
2. Generate MD5 and SHA1 checksums (via checksum-maven-plugin)
3. Create GPG signatures for all artifacts (via maven-gpg-plugin)
4. Package everything into a Maven Repository Layout archive

The bundle will be created in the project root as:
- `target/portal-bundle-${VERSION}.zip`
- `target/portal-bundle-${VERSION}.tar.gz`

### Verifying the Bundle

Before uploading, verify the bundle contains all required files:

```shell
# Extract and inspect the bundle structure
unzip -l target/portal-bundle-1.9.8.M2.zip | head -50

# Verify the Maven Repository Layout structure
unzip -l target/portal-bundle-1.9.8.M2.zip | grep 'org/aspectj'

# Check that all artifacts are present for each module
# Each module should have:
#   - {artifactId}-{version}.jar + .asc + .md5 + .sha1
#   - {artifactId}-{version}-sources.jar + .asc + .md5 + .sha1
#   - {artifactId}-{version}-javadoc.jar + .asc + .md5 + .sha1
#   - {artifactId}-{version}.pom + .asc + .md5 + .sha1
```

### Installing Dry-Run Bundle to Local Maven Repository

To verify that the bundle works correctly, you can install it into your local Maven repository
and test it with a local project:

```shell
# Extract the dry-run bundle to a temporary directory
TEMP_DIR=$(mktemp -d)
unzip -q target/portal-bundle-1.9.8.M2.zip -d "$TEMP_DIR"

# Copy the extracted Maven Repository Layout structure to your local repository
# This assumes your local repository is at ~/.m2/repository (default)
cp -r "$TEMP_DIR/org" ~/.m2/repository/

# Clean up temporary directory
rm -rf "$TEMP_DIR"

# Verify the artifacts are installed
ls -la ~/.m2/repository/org/aspectj/aspectjrt/1.9.8.M2/
ls -la ~/.m2/repository/org/aspectj/aspectjweaver/1.9.8.M2/
ls -la ~/.m2/repository/org/aspectj/aspectjtools/1.9.8.M2/
ls -la ~/.m2/repository/org/aspectj/aspectjmatcher/1.9.8.M2/
```

Now you can test the installed artifacts with a local project:

```shell
# In a test project that depends on AspectJ, update the version in pom.xml:
# <dependency>
#   <groupId>org.aspectj</groupId>
#   <artifactId>aspectjrt</artifactId>
#   <version>1.9.8.M2</version>
# </dependency>

# Build the test project to verify the artifacts resolve correctly
cd /path/to/test-project
mvn clean compile

# If using AspectJ compiler, verify it works:
# mvn clean compile -Dmaven.compiler.compiler=org.aspectj.ajc.AjcCompiler
```

**Important:** The dry-run bundle does **NOT** include GPG signatures (`.asc` files) and **CANNOT** be uploaded to Maven Central. It is for local verification only.

However, Maven will still resolve and use the artifacts from your local repository without signatures. This allows you to verify:
- Artifacts are correctly structured
- POMs are valid and contain correct metadata
- Dependencies resolve correctly
- The artifacts can be used in a real project build

If the test project builds successfully, you can proceed with creating the production bundle
with GPG signatures for the actual release. **Do not attempt to upload the dry-run bundle to Maven Central.**

### Committing and Tagging

Before uploading to the portal, commit and tag the release:

```shell
# Commit the release POMs to Git (better do this from your IDE, verifying the
# changes before staging them for Git commit)
git commit -am "Release AspectJ 1.9.8.M2"

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

### Uploading to Maven Central Portal

You can upload the bundle either manually through the web UI or programmatically using the Publisher API.

#### Option 1: Manual Upload (Web UI)

1. Go to the [Maven Central Publisher Portal](https://central.sonatype.com/publish/publish-portal-upload/)
2. Click "Publish Component" (requires at least one verified namespace)
3. Enter a deployment name (e.g., `org.aspectj:aspectjrt:1.9.8.M2`)
4. Optionally provide a description
5. Click "Upload File" and select the bundle archive (`target/portal-bundle-1.9.8.M2.zip` or `.tar.gz`)
6. Click "Publish Component" to begin the upload

#### Option 2: Programmatic Upload (Publisher API)

The portal provides a REST API for programmatic uploads. You'll need a portal token for authentication.

1. Generate a portal token:
   - Go to [Central Portal](https://central.sonatype.com/) and navigate to your profile
   - Generate a token for publishing (see [Portal Token Documentation](https://central.sonatype.com/publish/publish-portal-upload/#generating-a-portal-token-for-publishing))

2. Upload using curl:

```shell
# Set your portal token (keep this secure, don't commit to version control)
export CENTRAL_TOKEN="your-portal-token-here"

# Upload the bundle
curl -X POST \
  -H "Authorization: Bearer ${CENTRAL_TOKEN}" \
  -F "bundle=@target/portal-bundle-1.9.8.M2.zip" \
  -F "name=org.aspectj:aspectjrt:1.9.8.M2" \
  -F "description=AspectJ 1.9.8.M2 release" \
  https://central.sonatype.com/api/v1/publish
```

For more details on the Publisher API, see the [Publisher API documentation](https://central.sonatype.com/publish/publish-portal-upload/#publisher-api).

**Note:** The API endpoint and authentication method may change. Check the latest portal documentation for current API details.

#### Verification

The portal will validate the bundle and process the upload. Once successful, the artifacts will be available on Maven Central
within a few minutes under https://repo1.maven.org/maven2/org/aspectj/, e.g. AspectJ Tools 1.9.8.M2 would appear under
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
