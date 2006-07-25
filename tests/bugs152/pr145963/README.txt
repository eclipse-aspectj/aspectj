To recreate the jar files create AspectJ projects within Eclipse containing the
required files, right click and select 'Export > Java > JAR file with AspectJ Support'

jar file					files contained in the jar file
--------					-------------------------------
adviceAndDeow.jar			A.aj, Deow.aj, Itd.aj, NewClass.java
adviceLabels.jar			ConcreteAspect.aj
aspectInDefaultPackage.jar	AspectInDefaultPackage.aj



may need classpath entries on some of these: ?

ajc A.aj Deow.aj Itd.aj NewClass.java -outjar adviceAndDeow.jar
ajc ConcreteAspect.aj -outjar adviceLabels.jar
ajc AspectInDefaultPackage.aj -outjar aspectInDefaultPackage.jar