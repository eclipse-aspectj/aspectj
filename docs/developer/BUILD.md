# Maven build options

AspectJ is based on a multi-module Maven build with several options influencing

  * what to build, 
  * whether to run tests,
  * whether to created documentation for the AspectJ website,
  * whether to create source and javadoc artifacts,
  * whether to GPG-sign artifacts for a release.

## Typical build scenarios

As a developer, which modules or artifacts you want to build depends on your situation. For example:

  * When building a stable, non-snapshot release (milestone, release candidate, final), your goal is to publish a full
    set of artifacts on Maven Central (MC). Because MC requires you to publish javadocs and source code together with
    the corresponding binaries and to sign all artifacts with GPG, this is the most complex and complete, but also the
    slowest build you are about to encounter. Probably, you also want to build the AspectJ documentation to be deployed
    to the website.

  * When building a snapshot, the requirements are less strict, even if you are planning to make the release publicly
    available in the Sonatype OSSRH snapshots repository. In this case, you can skip creating javadocs and source JARs
    and also do not need to sign the artifacts. You might want to decide to sign anyway or at least to publish source
    JARs (which modern IDEs can also use in order to display javadoc information). So even if you are not working with
    an active Maven release profile, you want to have control over those options by setting properties. Probably, you do
    not wish to generate the AspectJ documentation for the website, because presently there is not even a dedicated
    place to deploy snapshot versions of it on the Eclipse webserver.

  * When building during development without the need to publish it, you probably want to skip as many non-essential
    parts of the build as possible. Firstly, you do not need them. Secondly, you want your build and run development
    cycles to be as quick as possible. So in this case, definitely you are going to skip javadoc and source JARs,
    website documentation and GPG signatures - unless you are in the process of changing and/or testing those parts of
    the build.

  * Independently of the above scenarios, you want to have control over whether to run (or even compile) any tests, and
    if so, which one(s).

## How to customise the build process

You can customise the Maven build process by using build profiles and/or set corresponding system properties for
fine-tuning.

### Build profiles

#### Main profiles

The main profiles you are going to use are:

  * By default, when not specifying any profiles or properties, the build skips a few non-essential, time-consuming
    steps, but runs all tests: no javadocs, no source JARs, no GPG signatures, but generate documentation.

  * `release` - Run tests, create javadoc and source JARs, generate documentation, activate GPG artifact signing.
    Furthermore, each module creating one of the main build artifacts individually uses Nexus Staging Maven Plugin in
    order to take care of deploying non-snapshot artifacts to Sonatype OSSRH staging repositories and subsequently
    releasing them to Maven Central. See [How to release AspectJ](RELEASE.md) for more information. Snapshot artifacts
    are being deployed normally, using Maven Deploy Plugin. See description of
    [property `maven.deploy.skip`](#build-properties) below for more information.

  * `fast-build` - In a way, this is the opposite of the release profile, trying to build the product as quickly as
    possible by skipping all non-essential build steps: no tests (skipping even test compilation), no javadocs, no
    source JARs, no GPG signatures, no documentation.

  * `create-docs` - If you intend to run a build with all tests, but still wish to skip generating documentation,
    deactivate this profile by
    ```shell
    mvn -P !create-docs ...
    ```
    On UNIX-like shells like Bash (also Git Bash under Windows), you probably need to escape the "!":
    ```shell
    mvn -P \!create-docs ...
    ```
    You can also deactivate the profile using a system property instead:
    ```shell
    mvn -DcreateDocs=false ...
    ```

#### Special profiles for `lib` module

Defined in the `lib` module, there are two special profiles, helping to make the build more efficient: 

  * `provision-libs` - Downloads and installs software used during tests, such as Apache Ant and several libraries. Some
    are downloaded from Maven Central, others directly from product download sites. Additionally, the build downloads
    several missing source packages, so developers can use them during development in order to access source code and
    javadoc. Because this build step is costly and should be performed only once after cloning the AspectJ repository
    or when other circumstances require re-provisioning at least one of those libraries, it is activated automatically,
    if marker file `lib/provisioned.marker` does not exist. After successful provisioning, the marker file is created,
    helping to avoid repeating this build step henceforth.

  * `clean-libs` - By default, `mvn clean` will not delete any of the libraries provisioned in profile `provision-libs`.
    This is intentional and one of the reasons why the libraries are not provisioned into the `target` directory but
    directly into `lib` subdirectories. If you wish to re-provision the libraries, simply run
    ```shell
    mvn -pl lib -P clean-libs clean
    ```
    Now you have a clean slate and during the next build, the libraries will be freshly downloaded and installed into
    their respective `lib` subdirectories.

Please note: An additional build step using Maven Enforcer Plugin also verifies the existence of several key files which
ought to exist after a successful download. This heuristic check runs independently of the two build profiles mentioned
above. It helps to detect accidental corruption of the provisioned libs, e.g., due to manual deletion or a previously
failed provisioning build step (network problems, manually interrupted build).  

#### Other profiles

Other existing profiles, which developers are less likely to actively use because they are applied automatically, are:

  * `repeat-all-unit-tests` - Maven module `run-all-junit-tests` has the sole purpose of providing a convenient means of
    running all tests across modules from an IDE instead of from Maven, in order to get JUnit build reporting directly
    there instead of on the console. As a developer, you simply run test suite `RunTheseBeforeYouCommitTests`. This
    profile is inactive by default, because in the context of a Maven build it would cause all tests to be run twice
    (during module build and again when running the big suite), hence the profile name.

  * `jdk-8-to-15` - Activated automatically on JDKs 8-15, setting properties `jvm.arg.addOpens` and
    `jvm.arg.allowSecurityManager` to empty values, because they are only needed on JDK 16+ or 18+, respectively. See
    next bullet point.

  * `jdk-16-to-xx` - Activated automatically on JDKs 16+, setting property `jvm.arg.addOpens` to value
    `--add-opens java.base/java.lang=ALL-UNNAMED`, which is needed in order to run LTW tests.

  * `jdk-18-to-xx` - Activated automatically on JDKs 18+, setting property `jvm.arg.allowSecurityManager` to value
    `-Djava.security.manager=allow`, which is needed by some tests in order to override `System.exit`.

### Build properties

The following properties and their default values in different profile are used in order to activate or skip Maven
plugin executions:

  * `maven.deploy.skip` (default `true`) - By default, do not deploy artifacts, because only the main AspectJ artifacts
    are meant to be shared with the general public, i.e. deployed to Sonatype OSSRH Snapshots or Maven Central artifact
    repositories. The main AspectJ artifact modules override the default, setting the value to `false`. This property is
    used independently of build profiles, it simply has a global default and module-specific overrides.

  * `maven.gpg.skip` (default: `true`) - By default, do not GPG-sign artifacts, because only the main AspectJ artifacts
    need to be signed before publishing them on Maven Central. The main AspectJ artifact modules override the default,
    setting the value to `false`. This property is used independently of build profiles, it simply has a global default
    and module-specific overrides. Given the additional fact that Maven GPG Plugin is only active in the `release`
    profile, it also means that the build globally skips signing if that profile is inactive. So if you wish to sign
    snapshot artifacts, you need to activate the `release` profile (also activating all the other build steps that
    profile has).
  
  * `maven.javadoc.skip` (default: `true`) - By default, do not create javadoc. Overridden in the `release` profile.
    When javadoc generation is skipped while producing the uber JAR assemblies for the main AspectJ artifacts, also
    unzipping of source uber JARs is skipped, because that step is only needed in order to create uber JAR javadocs in
    the first place. (Do not worry too much, if you do not fully understand what I just wrote.)

  * `maven.source.skip` (default: `true`) - By default, do not create source JARs. Overridden in the `release` profile.
    Actually, this property is meant to be used in order to skip execution of Maven Source Plugin, but currently the
    AspectJ build does not even use that plugin, because the build does not create source JARs for individual modules.
    That might change in the future, though, so we use this property to also influence Maven Assembly Plugin, which is
    responsible for creating source uber JARs for the main AspectJ artifacts.

  * `skipTests` (default: `false`) - By default, execute tests. Profile `fast-build` overrides this property.

  * `maven.test.skip` (default: `false`) - By default, compile and execute tests. Profile `fast-build` overrides this
    property. Actually, activating this property also implies `skipTests`, but `fast-build` sets both of them in order
    to be explicit about its intentions.

  * `createDocs` (default: `true`) - By default, create user documentation for the AspectJ website. Profile `fast-build`
    overrides this property.

### Examples

In addition to the examples above, concerning how to skip website documentation generation in the `docs` module and how
to clean downloaded libraries in the `lib` module, here are a few more:

  * Run a clean default build including tests and generating Aspect documentation: 
    ```shell
    mvn clean verify
    ```
    If you wish to install all artifacts in the local Maven repository, because subsquently maybe you want to run builds
    for submodules you are working on and which need to find other artifacts in the repository for a successful build,
    you rather use:
    ```shell
    mvn clean install
    ```

  * Run a fast build, no test compilation and execution, no AspectJ documentation, no javadoc, no source JARs 
    ```shell
    mvn -P fast-build package
    ```

  * Run a release build incl. tests, GPG artifact signing and deployment:
    ```shell
    mvn -P release clean deploy
    ```

  * Run a release build incl. deployment, but without compiling and running tests because you ran all tests before
    successfully already:
    ```shell
    mvn -P release,fast-build clean deploy
    ```
    This is effectively the same as:
    ```shell
    mvn -P release -Dmaven.test.skip=true clean deploy
    ```
    In a UNIX shell, you probably have to double-quote when using properties containing dots:  
    ```shell
    mvn -P release "-Dmaven.test.skip=true" clean deploy
    ```

In general, you should not combine profiles setting the same properties in contradictory ways. If you need a very
specific build configuration, you might want to use a profile matching your needs most closely and override specific
properties. However, I am not going to share examples for this approach, because generally it is not necessary and also
both error-prone and sensitive to even small changes in Maven POMs.  
