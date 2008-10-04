@echo off
echo "AspectJ Tools Jar:   <=src  >=jar"
jar -tvf ..\aj-build\src\aspectjtools-src.jar | cut -c37- | sed 's/.java$//' | sort > aspectjtoolssrc.lst
jar -tvf ..\aj-build\dist\tools\lib\aspectjtools.jar | cut -c37- | sed 's/.class$//' | grep -v "\$" | sort > aspectjtoolsjar.lst
diff aspectjtoolssrc.lst aspectjtoolsjar.lst
erase aspectjtoolssrc.lst aspectjtoolsjar.lst
