
This contains demonstration source code for the article
"Get Inoculated!" in the May 2002 issue of Software Development
magazine.  

To use it you will need the AspectJ tools available from
http://eclipse.org/aspectj.  We also recommend you download the
documentation bundle and support for the IDE of your choice.

Each file has a snippet for a section of the article.  To find
one in particular, see the back-references to "article page #":

   CompileTime.java:    // article page 40 - warning
   CompileTime.java:    // article page 41 - error
   RunTime.java:        // article page 41 - runtime NPE
   RuntimeWrites.java:  // article page 42 - field writes
   RecordingInput.java: // article page 42 - recording input
   MainFailure.java:    // article page 42 - recording failures from main
   BufferTest.java:     // article page 43 - input driver
   Injection.java:      // article page 43 - fault injection
   StubReplace.java:    // article page 76 - stubs
   RoundTrip.java:      // article page 76 - round trip

Compile and run as usual:

   > set AJ_HOME=c:\aspectj1.0
   > set PATH=%AJ_HOME%\bin;%PATH%
   > ajc -classpath "$AJ_HOME/lib/aspectjrt.jar" {file}
   > java -classpath ".;$AJ_HOME/lib/aspectjrt.jar" {class}

For email discussions and support, see http://eclipse.org/aspectj.


Enjoy!

the AspectJ team
