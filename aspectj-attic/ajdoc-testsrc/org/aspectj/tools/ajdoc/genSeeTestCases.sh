#!/bin/sh
# generate test cases for {@link} tags

## permitted variants:
# - everything but member name can be empty
# - spaces between everything except around sharp #
# - 0..n parm types
# - simple or qualified type names
# - Type[ ][ ] but not Type [ ] [ ]
# todo: 
# - URL's acceptable for link??

count=0
pre="        "
echo "    static final SeeTestCase[] CASES = new SeeTestCase[] { null // first is null"

for type in "" Type junit.Type org.aspectj.Type; do 
for name in memberName; do
for parms in "" "()" "(int)" "(int,String)" "( int , String[ ][ ] )" "( foo.Bar , com.sun.X )" "(foo.Bar)"  ; do
for label in "" label "a label"; do

   # method, field X no spaces, spaces
   echo "$pre, new SeeTestCase(\"$type#$name$parms $label\",  \"$type\", \"$name\", \"$parms\", \"$label\")  // $count"
   echo "$pre, new SeeTestCase(\" $type#$name $parms $label \",  \"$type\", \"$name\", \"$parms\", \"$label\")"
   count=`expr $count + 2`
done; done; done; done;
echo "${pre}};";

i=0
while [ $i -lt $count ] ; do
   i=`expr $i + 1` # first is null
   echo "    public void testLink$i() { CASES[$i].run(); }"
done

    
