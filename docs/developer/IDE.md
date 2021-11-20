# Setting up an AspectJ development environment

_by Alexander Kriegisch, 2021-08-05_

AspectJ, being an Eclipse project, can of course be used in the Eclipse IDE for Java developers, if you also install the
AspectJ Development Tools (AJDT). IntelliJ IDEA also offers AspectJ and Spring AOP support. Regarding build tools, there
are AspectJ plugins for Maven, Gradle and Ant.

Of course, you can use and other IDE and build tool or simply use your trusted text editor of choice and compile using
the AspectJ Compiler _ajc_ from the command line.

Please refer to the [AspectJ Development Environment Guide](https://www.eclipse.org/aspectj/doc/next/devguide/printable.html)
for more details about on-board AspectJ tools, such as _ajc_ (compiler), _ajdoc_ (aspect-enriched Javadoc), _aj_
(load-time weaving helper), AspectJ Browser (simple visualisation tool for cross-cutting aspect structure) as well as
basic information about load-time weaving configuration and the built-in Ant task for AspectJ.

## IDE support

We are going to focus on the two IDEs the author of this document is acquainted with, Eclipse and IntelliJ IDEA. That
does not mean that others such as NetBeans or the increasingly popular editor-on-steroids Visual Studio Code might not
also be excellent choices, but I simply do not know them, sorry.

### Eclipse

If you install [AspectJ Development Tools (AJDT)](https://www.eclipse.org/ajdt/), you can use AspectJ in the Eclipse
Java IDE. For the last few years, AJDT has rather been kept alive than actively developed, but still it works nicely for
the usual tasks, such as writing aspects in both native and annotation-style syntax, viewing cross-references between
aspects and Java code, applying all sorts of weaving strategies (compile-time weaving, post-compile binary weaving,
load-time weaving) and with an additional m2e (Maven to Eclipse) connector also for importing and refreshing from
projects using AspectJ Maven Plugin.

#### AspectJ Development Tools (AJDT)

Use an update sites corresponding to your Eclipse version (minimal version listed):
* Eclipse 2021-09 (4.21): https://aspectj.dev/eclipse/ajdt/421
* Eclipse 2021-03 (4.19): https://aspectj.dev/eclipse/ajdt/419
* Eclipse 2018-12 (4.10): https://download.eclipse.org/tools/ajdt/410/dev/update
* For older versions, please refer to https://www.eclipse.org/ajdt/downloads (page was not updated in a long time,
  and I have no write access).

#### Maven to Eclipse (m2e) connector

The connector between AspectJ Maven Plugin and AJDT has not been maintained by the AspectJ team for some years, but
there is a fairly up-to-date [fork by Miika Vesti (joker1)](https://github.com/joker1/m2eclipse-ajdt) with the following
update sites:
  * Eclipse 2020-12 (4.18): https://repo.t5.fi/public/eclipse/m2eclipse-ajdt/eclipse-2020-12/
  * Eclipse 2019-06 (4.12): https://repo.t5.fi/public/eclipse/m2eclipse-ajdt/eclipse-2019-06/

### IntelliJ IDEA

IDEA is a commercial IDE by JetBrains, which also has a free Community Edition. The author of this guide uses a
complementary Ultimate Edition provided by JetBrains (thank you!), because he is an active open source developer (ask
them if you are eligible, too). If you want to use the Ultimate Edition for commercial purposes, however, of course you
need to buy it. According to [this resource](https://www.jetbrains.com/help/idea/aspectj.html), the AspectJ plugin is
for the Ultimate Edition only, so probably the plugin is not going to work in the Community Edition. But if you are
using AspectJ in your daytime job, probably you use the Ultimate Edition anyway.

Make sure that the following plugins are active:
  * AOP Pointcut Language (bundled): basic AspectJ and Spring AOP pointcut syntax support (annotation style)
  * AspectJ (marketplace, author JetBrains): native AspectJ support

If the Maven support bundled into IntelliJ IDEA is active as well, Maven projects using the AspectJ Maven Plugin will
also be imported correctly. I never tested with any of the Gradle plugins or the AspectJ Ant task because I am a Maven
user, so please try for yourself if those combinations are supported.

## Build tools

### Maven

If you want to build your AspectJ projects with Maven, use [AspectJ Maven Plugin](https://github.com/dev-aspectj/aspectj-maven-plugin)
by AspectJ.dev, artifact ID `dev.aspectj:aspectj-maven-plugin`. It is a fork of the original plugin by MojoHaus, which
was inactive for 3.5 years and only recently (2021-07-30) had a new release. Because the AspectJ.dev version (maintained
by AspectJ contributor Alexander Kriegisch) has more features, we recommend using it instead of the original for
original's sake.

### Gradle

In contrast to Maven, there is no de-facto standard build plugin for Gradle, but a bunch of more or less well-maintained
plugins with a more or less complete feature set. I am not a Gradle user, but when answering related questions on
StackOverflow, I got the impression that [Freefair by Lars Grefer](https://docs.freefair.io/gradle-plugins/current/reference/)
is the one I want to recomment because of its good documentation, active development and feature set. It could well be
or become for the Gradle ecosystem what AspectJ Maven is for the Maven one. At the time of writing this, the most recent
milestone is [6.1.0-m3](https://docs.freefair.io/gradle-plugins/6.1.0-m3/reference/). 

### Ant

As mentioned in the introduction, AspectJ features its own Ant task, which is described in
[this chapter](https://www.eclipse.org/aspectj/doc/next/devguide/printable.html#antTasks) of the Development Environment
Guide.

## Biased recommendation

Feel free to be skeptical, because the author has both IDE and build tool biases, but I am recommending Maven as a build
tool, AspectJ Maven as a build plugin and then either of Eclipse or IDEA as integrated development environments (IDEs).
Why? Because Maven + (Eclipse or IDEA) probably has the most complete tooling and best end-to-end user experience for
AspectJ developers. Because I am also by far the most active AspectJ and Spring AOP supporter on StackOverflow, you also
have better chances to receive spot-on answers than if e.g. you ask why your Ant or Gradle build does not work so nicely
in NetBeans or VS Code. But by all means, please do choose whichever combination of tools is the prescribed standard in
your work environment or simply your personal pereference. I believe that diversity is good. 🙂
