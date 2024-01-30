AspectJ Figures Exercises
------------------------------

This file is really out-of-date.  We're in the middle of recovery
from OOPSLA 2004, and we need to move stuff around in this
package.  In particular, we need to build in a new structure.

One idea is that we should have _four_ projects within the
workspace, one for each problem set.  There's still a bit of work
necessary to avoid duplicating code here (in CVS) even though we
want to duplicate code in the generated workspaces.

------------------------------
Generated Structure

We want a particular _generated_ structure for users.  We're
eventually going to dump to a zip file or a CD.

Folder: aj-<event>:
  /eclipse     (arch)
  /workspace   (noarch)
  /packages    (noarch)
    j2sdk for win, linux
    aspectj for everybody

------------------------------
Requirements for users:

* We learned at OOPSLA 2004 that the system doesn't work well at
  all under linux using the blackdown JDK.  Use Sun's jdk!


------------------------------
------------------------------

THIS FILE IS OUT-OF-DATE !!! (2003-3-17)

These exercises are designed to be used with AspectJ 1.0.6.

THEY MUST NOT BE DISTRIBUTED ELECTRONICALLY WITHOUT THINKING FIRST!!!
There may be licence issues with just sticking the junit jar in there
that I don't understand.

To build distribution zips, use ant -f build.xml.  This will create

   EV-exercises.zip     -- the contents of these exercises, minus AspectJ
   EV-answers.zip       -- some answers to these exercises.
   EV-setup.zip         -- the exercises bundled with AspectJ
   EV-allcontent.zip    -- the exercises bundled with the answers


If you don't want to be bothered with specifying where AspectJ is,
feel free to just do ant -f build.xml answers.zip.  By default it will
build both exercises and answers.

------------------------------
Required environment

In order to actually do the exercises, the three very important files
to have are:

  EV-exercises.zip
  aspectj-tools-1.0.6.jar
  <some installer of java>

It is almost certainly a good idea to hope that Java is already
installed on the students' systems, but to provide two installers
(windows and linux) anyway.

   http://java.sun.com

You may want to also include the rest of the AspectJ installers as
well, but that might be a bit of a distraction

   http://aspectj.org/dl

These require junit.jar.  I've included a copy in with the two zip
files, but they might want to be refreshed every now and again.

   http://junit.org


------------------------------
Printing

It would be really nice to have an automated solution to generate the
documents, but no such luck.

* index.html should print out with useful page breaks.  It should be
  separated into four chunks if possible.

* answers in four chunks.

* quick reference sheets.

Remember to have a one-sided copy of everything as a separate
clean-copy.


------------------------------
Distribution

   /j2sdk-1_4_1_01-linux-i586.bin
   /j2sdk-1_4_1_01-windows-i586.exe
   /exercises.zip containing:
       aj-EV/src/figures.zip
       aj-EV/src/aspectj-tools-1.0.6.jar
       aj-EV/src/aspectj-docs-1.0.6.jar
       aj-EV/<exploded version of figures.zip>
       aj-EV/aspectj/<exploded version of aspectj-tools>
       aj-EV/aspectj/<exploded version of aspectj-docs>
       aj-EV/setpaths
       aj-EV/setpaths.bat

/bin/ajc, are edited to make sure that JAVA_HOME is used, as the
defaults will almost certainly be wrong.  setpaths scripts do what
they look like they do.  All six of these scripts are stored in
scripts under CVS.

---- Instructors

Since the only difference is the answers, just overwrite the
extraction directory with answers.zip.

