echo "Build simple empty module definition"
cd one
javac module-info.java

# A pre java9 jar e.g. a-b-c-1.6.10.jar would become a module a.b.c (automated module)
echo "Build empty module definition with automated name a.b.c"
cd ..
cd two/a
javac module-info.java

echo "Build helper module: b.c.d"
cd ../..
cd two/b
javac module-info.java

echo "Build helper module: c.d.e"
cd ../..
cd two/c
javac module-info.java

echo "Build code using require variants"
cd ../..
cd two/d
javac module-info.java --module-path ../a:../b:../c

echo "Exports variants"
cd ../..
cd two/e
javac module-info.java C1.java C2.java C3.java -d . --module-path ../a:../b

echo "Uses variants"
cd ../..
cd two/f
javac module-info.java I1.java -d . 

echo "Provides variants"
cd ../..
cd two/g
javac module-info.java I1.java I2.java C1.java C2.java -d . 

echo "Opens variants"
cd ../..
cd two/h
javac module-info.java C1.java C2.java C3.java --module-path ../a:../b -d . 
cd ../..
