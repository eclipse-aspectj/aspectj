ajc -1.8 -sourceroots OverWeave_1/src -outjar ow1.jar
ajc -1.8 -sourceroots OverWeave_2/src -outjar ow2.jar
ajc -1.8 -Xset:overWeaving=true -d out -inpath ow1.jar -aspectpath ow2.jar -sourceroots OverWeave_3/src
java -classpath out:/Users/aclement/installs/aspectj192/lib/aspectjrt.jar:ow2.jar  Application
