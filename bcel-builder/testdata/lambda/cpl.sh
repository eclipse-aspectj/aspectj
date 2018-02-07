
# Use Java9 or later to compile this
cd one
javac Code.java

jar -cvMf ../../lambda.jar Code.class
rm Code.class
cd ..
