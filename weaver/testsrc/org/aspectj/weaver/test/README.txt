Those are the source for the
modules\weaver\testdata\bin

Next time we update the class files in bin\, we have to follow this new package scheme
(to avoid 'no package' classes floating around)




The class files in this directory are an *input* to the weaver tests. They were generated
from the .java files in testsrc using an Eclipse 2.1.1 compiler (well, ajc actually). The
weaver tests are sensitive to implementation details in class file output that vary by compiler.
When we rev. the jdt.core to 3.0.0 these class files will need to be updated again.