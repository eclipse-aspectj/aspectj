package libraries;

/** @author Wes Isberg */
public class PointcutLibraryTest {
    public static void main(String[] a) {
        new Test().run();
    }
}

class Test {
    public Test() {}
    public void run(){ prun(); }
    private void prun() {
        System.out.println("Test.prun()");
    }
}

// START-SAMPLE library-classPointcutLibrary Defining library pointcuts in a class
/** private default implementation of library */
class PrivatePointcutLibrary {
    pointcut adviceCflow() : cflow(adviceexecution());
    pointcut publicCalls() : call(public * *(..))
        && !adviceCflow()
        ;
}

/** public interface for library */
class PointcutLibrary extends PrivatePointcutLibrary {
}

// ---- different clients of the library

/** client by external reference to library */
aspect ExternalClientOfLibrary {
  before() : PointcutLibrary.publicCalls() {
      System.out.println("XCL: " 
          + thisJoinPointStaticPart);
  }
}

/** use library by inheriting scope in aspect */
aspect AEL extends PointcutLibrary {
    before() : publicCalls() {
        System.out.println("AEL: " 
            + thisJoinPointStaticPart);
    }
}

/** use library by inheriting scope in class */
class CEL extends PointcutLibrary {
    static aspect A {
        before() : publicCalls() {
            System.out.println("CEL: " 
                + thisJoinPointStaticPart);
        }
    }
}

/** more clients by inheritance */
aspect CELSubAspect extends CEL {
    before() : publicCalls() {
        System.out.println("CSA: " 
            + thisJoinPointStaticPart);
    }
}


// ---- redefining library pointcuts

//-- affect all clients of PointcutLibrary
// test: XCL advises Test()
class VendorPointcutLibrary extends PrivatePointcutLibrary {
    /** add calls to public constructors */
    pointcut publicCalls() : PrivatePointcutLibrary.publicCalls()
        || (call(public new(..)) && !adviceCflow());
    static aspect A {
        declare parents: 
            PointcutLibrary extends VendorPointcutLibrary;
    }
}

//-- only affect CEL, subtypes, & references thereto
// test: CSA does not advise call(* println(String))
// test: CSA advises call(* prun())
class CPlus extends PointcutLibrary {
    /** add calls to private methods, remove calls to java..* */
    pointcut publicCalls() : (PointcutLibrary.publicCalls()
        || (call(private * *(..)) && !adviceCflow()))
        && (!(call(* java..*.*(..)) || call(java..*.new(..))));
    static aspect A {
        declare parents: CEL extends CPlus;
    }
}
// END-SAMPLE library-classPointcutLibrary

