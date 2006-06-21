to rebuild foo.jar:


Build Foo.java - it includes a definition of PreparedStatement with no method specified

mkdir out
javac -d out Foo.java

Build a new PreparedStatement that includes the method

javac -d out PreparedStatement.java

Build the jar

cd out
jar -cvMf ../foo.jar *

You now have a jar where the Foo.class contains an invalid override...
