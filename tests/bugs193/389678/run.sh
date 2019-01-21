echo "ow1"
ajc -1.8 -sourceroots OverWeave_1/src -outjar ow1.jar -showWeaveInfo
echo "ow2"
ajc -1.8 -sourceroots OverWeave_2/src -outjar ow2.jar -showWeaveInfo
echo "ow4 build"
ajc -1.8 -sourceroots OverWeave_4/src -outjar ow4.jar -showWeaveInfo
echo "ow3"
ajc -1.8 -d out -inpath ow1.jar -aspectpath ow2.jar -showWeaveInfo -sourceroots OverWeave_3/src -outjar ow3.jar

java -classpath out:/Users/aclement/installs/aspectj192/lib/aspectjrt.jar:ow2.jar  Application
