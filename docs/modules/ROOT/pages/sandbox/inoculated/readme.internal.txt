// XXX do not distribute

------ contents
05sd.Isberg40-43,76.pdf    # not for distribution
BufferTest.java
CompileTime.java
Injection.java
MainFailure.java
RecordingInput.java
RoundTrip.java
RunTime.java
RuntimeWrites.java
StubReplace.java
buildRun.sh
readme.internal.txt       # not for distribution
readme.txt

------ summary of todo's
- consider moving to packages, combining PrinterStream, etc. 
- use DOS linefeeds - check throughout (also line length)
- see XXX
  - assess handling of one style mistake
  - see if second mistake was actually in article - corrected in code

------ fyi
- standard of care: show language, not problem
- formatting: lineation, line width, DOS linefeeds, etc.
- organization: 
  - code currently compiles/runs one at a time
    and does not compile all at once b/c of
    common fixtures (PrinterStream...)
  - currently packages (com.xerox.printing) in base dir

- Copyright/license: examples, ,but PARC Inc.
- article code unit flagged with "article page #"

------ style fyi
- flagging style mistake in StubReplace.java:

    // XXX style mistake in article code
    //pointcut printerStreamTestCalls() : call(* PrinterStream+.write());

- leaving CompileTime.java use of + in call for factory pointcut:

    call(Point+ SubPoint+.create(..))

  - for static methods where the method name specification 
    involves no * but does reflect a factory naming convention 
    (and not polymorphism)
    (though not restricting factory methods to being static)

  - for referring to the return value when I want to pick out 
    the type and all subtypes 

