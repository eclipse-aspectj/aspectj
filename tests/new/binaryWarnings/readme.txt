
Warning: brittle test!  Change carefully and rebuild binaries!

Bugs: 37020, 37021, 37023 

- there are 3+ associated test specifications:
  - source-only 
  - binary (javac) application, source aspect
  - binary (ajc) application, source aspect
  - binary (javac) application, binary aspect (todo)
  - binary (ajc) application, binary aspect   (todo)

- the lines in Main.java correspond with 
  warning line values in the test specifications

- the message text in MainWarnings.java correspond with 
  warning text values in the test specifications
  
- all test specifications should have exactly the same warnings
  as each other as as specified in MainWarnings.java

- the ExecStartLine variants show that for binary join points,
  we detect the first line of code in the associated block,
  not the first line of the block.  This is a known limitation.

To build the injar sources from the src directory:

Using javac:
  mkdir classes
  javac -d  classes app/Main.java
  jar cfM ../injars/app-javac-1.4.jar -C classes .
  rm -rf classes

Using ajc:
  ajc -classpath ../../../../lib/test/aspectjrt.jar  \
    -outjar ../injars/app-ajc-1.1.jar app/Main.java

Using ajc in eclipse, from a module directory:

  {ajc} -classpath ../lib/test/aspectjrt.jar  
    -outjar ../tests/new/binaryWarnings/injars/app-ajc-1.1.jar  
    ../test/new/binnaryWarnings/src/app/Main.java

