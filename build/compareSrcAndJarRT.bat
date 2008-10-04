@echo off
echo "AspectJ Runtime Jar:   <=src  >=jar"
jar -tvf ..\aj-build\src\aspectjrt-src.jar | cut -c37- | sed 's/.java$//' | sort > aspectjrtsrc.lst
jar -tvf ..\aj-build\dist\tools\lib\aspectjrt.jar | cut -c37- | sed 's/.class$//' | grep -v "\$" | sort > aspectjrtjar.lst
diff aspectjrtsrc.lst aspectjrtjar.lst
erase aspectjrtsrc.lst aspectjrtjar.lst
