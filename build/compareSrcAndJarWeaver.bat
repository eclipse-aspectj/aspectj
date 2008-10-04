@echo off
echo "AspectJ Weaver Jar:   <=src  >=jar"
jar -tvf ..\aj-build\src\aspectjweaver-src.jar | cut -c37- | sed 's/.java$//' | sort > aspectjweaversrc.lst
jar -tvf ..\aj-build\dist\tools\lib\aspectjweaver.jar | cut -c37- | sed 's/.class$//' | grep -v "\$" | sort > aspectjweaverjar.lst
diff aspectjweaversrc.lst aspectjweaverjar.lst
erase aspectjweaversrc.lst aspectjweaverjar.lst
