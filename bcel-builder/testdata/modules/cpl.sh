cd one
javac module-info.java
cd ..
cd two/a
javac module-info.java
cd ../..
cd two/b
javac module-info.java
cd ../..
cd two/c
javac module-info.java
cd ../..
cd two/d
javac module-info.java -modulepath ../a:../b
cd ../..
cd two/e
javac module-info.java C1.java C2.java C3.java -d . -modulepath ../a:../b
cd ../..
cd two/f
javac module-info.java I1.java -d . 
cd ../..
cd two/g
javac module-info.java I1.java I2.java C1.java C2.java -d . 
cd ../..
