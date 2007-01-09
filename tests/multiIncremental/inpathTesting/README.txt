To regenerate the class file in injarBin\pkg:

	javac origInpathClass\InpathClass.java

and copy it to injarBin\pkg.

To regenerate the class copied over as part of the test:

	javac newInpathClass\InpathClass.java  

To regenerate the jar file base\inpathJar.jar

	javac origInpathClass\InpathClass.java
	cd origInpathClass
	jar cf inpathJar.jar InpathClass.class inpathResource.txt

copy resultant jar file to ..\base
