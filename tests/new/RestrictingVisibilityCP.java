import org.aspectj.testing.Tester;

/** @testcase PR#536 overriding subclass method may be protected when superclass method has default access  */
public class RestrictingVisibilityCP {
    public static void main(String[] args) {
        Tester.expectEventsInString(Const.EXPECTED);
        Parent[] tests = new Parent[] 
            { new Parent()
              , new ValidChild()
              , new ValidChild2()
              , new ValidChild3()
              , new ValidChildTest()
              , new ValidChildTest2()
              , new ValidChildTest3()
                  };
        Parent child;
        for (int i = 0; i< tests.length;i++) {
            child = tests[i];
            Tester.event(child.getClass().getName());
            //System.err.println(", \"" + child.getClass().getName() + "\"");
            child.publicAccess(); 
            child.defaultAccess(); 
            child.protectedAccess(); 
            child.drivePrivateAccess(); 
        }
        Tester.checkAllEvents();
    }
}
/** aspect used to log progess - test case otherwise purejava */
aspect LogAll {
    before() : within(Parent+) && execution(* *(..)) {
        String name = thisJoinPointStaticPart.toString();
        Tester.event(name);
        // System.err.println(", \"" + name + "\"");
    }
}
class Parent {
    public int publicAccess = 1;
    protected int protectedAccess = 1;
    int defaultAccess = 1;
    private int privateAccess = 1;

    public void publicAccess() {}
    protected void protectedAccess() {}
    void defaultAccess() {}
    private void privateAccess() {}
    void drivePrivateAccess() {
        privateAccess();
    }
}

class ValidChild extends Parent {

    /** @testcase subclass public implementation of public method */
    public void publicAccess() { super.publicAccess(); }
    /** @testcase subclass protected implementation of protected method */
    protected void protectedAccess() { super.protectedAccess(); }
    /** @testcase subclass default implementation of default method */
    void defaultAccess() { super.defaultAccess(); }
    /** @testcase subclass implementation of private method - not overriding */
    private void privateAccess() {  }
    void drivePrivateAccess() {
        privateAccess();
        super.drivePrivateAccess();
    }

    int publicAccessSub = publicAccess;
    int protectedAccessSub = protectedAccess;
    int defaultAccessSub = defaultAccess;
}

class ValidChild2 extends Parent {
    /** @testcase subclass public implementation of protected method */
    public void protectedAccess() { super.protectedAccess(); }
    /** @testcase subclass public implementation of default method */
    public void defaultAccess() { super.defaultAccess(); }
}

class ValidChild3 extends Parent {
    /** @testcase subclass protected implementation of default method */
    // todo: restore to restore ce
    protected void defaultAccess() { super.defaultAccess(); }
}

class ValidChildTest extends ValidChild {
    /** @testcase subsubclass public implementation of public method */
    public void publicAccess() { super.publicAccess(); }
    /** @testcase subsubclass protected implementation of protected method */
    protected void protectedAccess() { super.protectedAccess(); }
    /** @testcase subsubclass default implementation of default method */
    void defaultAccess() { super.defaultAccess(); }
    /** @testcase subsubclass implementation of private method - not overriding */
    private void privateAccess() { } 
    void drivePrivateAccess() {
        privateAccess();
        super.drivePrivateAccess();
    }
}
class ValidChildTest2 extends ValidChild {
    /** @testcase subsubclass public implementation of protected method */
    public void protectedAccess() { super.protectedAccess(); }
    /** @testcase subsubclass public implementation of default method */
    public void defaultAccess() { super.defaultAccess(); }
}
class ValidChildTest3 extends ValidChild {
    /** @testcase PR#536 subsubclass protected implementation of default method */
    // todo protected void defaultAccess() { super.defaultAccess(); }
}
class ValidChild5 extends Parent {
    int one = publicAccess;
    int two = protectedAccess;
    int three = defaultAccess;
}
class ValidClass {
    static int[] ra;
    static {
        ra = new int[]
        { (new Parent()).publicAccess
          , (new ValidChild()).publicAccess
          , (new ValidChildTest()).publicAccess
          , (new Parent()).defaultAccess
          , (new ValidChild()).defaultAccess
          , (new ValidChildTest()).defaultAccess
          , (new Parent()).protectedAccess
          , (new ValidChild()).protectedAccess
          , (new ValidChildTest()).protectedAccess
        };
        for (int i = 0; i < ra.length; i++) {
            Tester.check(1 == ra[i], 
                         "expected 1 at " + i + " got i" + ra[i]);
        }
    }
}

class Const {
    public static final String[] EXPECTED = new String[]
    {  "Parent"
       , "execution(void Parent.publicAccess())"
       , "execution(void Parent.defaultAccess())"
       , "execution(void Parent.protectedAccess())"
       , "execution(void Parent.drivePrivateAccess())"
       , "execution(void Parent.privateAccess())"
       ,  "ValidChild"
       , "execution(void ValidChild.publicAccess())"
       , "execution(void Parent.publicAccess())"
       , "execution(void ValidChild.defaultAccess())"
       , "execution(void Parent.defaultAccess())"
       , "execution(void ValidChild.protectedAccess())"
       , "execution(void Parent.protectedAccess())"
       , "execution(void ValidChild.drivePrivateAccess())"
       , "execution(void ValidChild.privateAccess())"
       , "execution(void Parent.drivePrivateAccess())"
       , "execution(void Parent.privateAccess())"
       ,  "ValidChild2"
       , "execution(void Parent.publicAccess())"
       , "execution(void ValidChild2.defaultAccess())"
       , "execution(void Parent.defaultAccess())"
       , "execution(void ValidChild2.protectedAccess())"
       , "execution(void Parent.protectedAccess())"
       , "execution(void Parent.drivePrivateAccess())"
       , "execution(void Parent.privateAccess())"
       ,  "ValidChild3"
       , "execution(void Parent.publicAccess())"
       , "execution(void ValidChild3.defaultAccess())"
       , "execution(void Parent.defaultAccess())"
       , "execution(void Parent.protectedAccess())"
       , "execution(void Parent.drivePrivateAccess())"
       , "execution(void Parent.privateAccess())"
       , "execution(void ValidChildTest.publicAccess())"
       , "execution(void ValidChild.publicAccess())"
       , "execution(void Parent.publicAccess())"
       ,  "ValidChildTest"
       , "execution(void ValidChildTest.defaultAccess())"
       , "execution(void ValidChild.defaultAccess())"
       , "execution(void Parent.defaultAccess())"
       , "execution(void ValidChildTest.protectedAccess())"
       , "execution(void ValidChild.protectedAccess())"
       , "execution(void Parent.protectedAccess())"
       , "execution(void ValidChildTest.drivePrivateAccess())"
       , "execution(void ValidChildTest.privateAccess())"
       , "execution(void ValidChild.drivePrivateAccess())"
       , "execution(void ValidChild.privateAccess())"
       , "execution(void Parent.drivePrivateAccess())"
       , "execution(void Parent.privateAccess())"
       , "ValidChildTest2"
       , "execution(void ValidChild.publicAccess())"
       , "execution(void Parent.publicAccess())"
       , "execution(void ValidChildTest2.defaultAccess())"
       , "execution(void ValidChild.defaultAccess())"
       , "execution(void Parent.defaultAccess())"
       , "execution(void ValidChildTest2.protectedAccess())"
       , "execution(void ValidChild.protectedAccess())"
       , "execution(void Parent.protectedAccess())"
       , "execution(void ValidChild.drivePrivateAccess())"
       , "execution(void ValidChild.privateAccess())"
       , "execution(void Parent.drivePrivateAccess())"
       , "execution(void Parent.privateAccess())"
       , "ValidChildTest3"
       , "execution(void ValidChild.publicAccess())"
       , "execution(void Parent.publicAccess())"
       , "execution(void ValidChild.defaultAccess())"
       , "execution(void Parent.defaultAccess())"
       , "execution(void ValidChild.protectedAccess())"
       , "execution(void Parent.protectedAccess())"
       , "execution(void ValidChild.drivePrivateAccess())"
       , "execution(void ValidChild.privateAccess())"
       , "execution(void Parent.drivePrivateAccess())"
       , "execution(void Parent.privateAccess())"

    };
}
