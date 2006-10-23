This folder contains the code for two tests:

Case 1: A type that was on the classpath when a jar file was created is 
        not on the classpath when the aspects are being compiled and woven 
        with this jar on the inpath. This should result in a cantFindType 
        message. 
        
Case 2: The type exists but one of it's members no longer does. This should 
        result in an unresolvableMember message.

To recreate required files for Case 1 :

1. Compile the MyStringBuilder.java in folder pr149908 type: 

	javac MyStringBuilder.java

2. ajc C.java -outjar simple.jar


To recreate the required files for Case 2:

1. Navigate to the withoutMethod folder

2. Compile MyStringBuilder.java into ..\stringBuilder.jar:

	ajc MyStringBuilder.java -outjar ..\stringBuilder.jar
