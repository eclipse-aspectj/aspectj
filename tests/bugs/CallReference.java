
import org.aspectj.testing.Tester;
import org.aspectj.lang.JoinPoint;


/** @testcase PR#41888 call PCD fails when given subtype of defining type */
public class CallReference {
    public static void main (String[] args) {
        // don't move these lines without changing expectEvents below
        new Sub().run();
        new SubSub().run();
        new SubSubSub().run();
        ((Super) new Sub()).run();
        ((Super) new SubSub()).run();
        ((Super) new SubSubSub()).run();
        ((Sub) new SubSub()).run();
        ((Sub) new SubSubSub()).run();
        ((SubSub) new SubSubSub()).run();
    } 
}

class Super { void run() {} }
class Sub extends Super { void run() {} }
class SubSub extends Sub { }
class SubSubSub extends SubSub { }

aspect A {
    static {
        // generated from System.out call below
        Tester.expectEvent("Super  1 10");
        Tester.expectEvent("Sub  2 10");
        Tester.expectEvent("Super  3 11");
        Tester.expectEvent("Sub  4 11");
        Tester.expectEvent("SubSub 5 11");
        Tester.expectEvent("Super  6 12");
        Tester.expectEvent("Sub  7 12");
        Tester.expectEvent("SubSub 8 12");
        Tester.expectEvent("SubSubSub 9 12");
        Tester.expectEvent("Super  10 13");
        Tester.expectEvent("Super  11 14");
        Tester.expectEvent("Super  12 15");
        Tester.expectEvent("Super  13 16");
        Tester.expectEvent("Sub  14 16");
        Tester.expectEvent("Super  15 17");
        Tester.expectEvent("Sub  16 17");
        Tester.expectEvent("Super  17 18");
        Tester.expectEvent("Sub  18 18");
        Tester.expectEvent("SubSub 19 18");
    }
    int count;
    void advice(String s, JoinPoint.StaticPart jp) {
        s = s 
            + " " 
            + ++count 
            + " " 
            + jp.getSourceLocation().getLine();
        Tester.event(s);
        // use after moving code lines above
        //System.out.println("Tester.expectEvent(\"" + s + "\");");
    }
    before() : call (void Super.run()) {
        advice("Super ", thisJoinPointStaticPart);
    }
    before() : call (void Sub.run()) {
        advice("Sub ", thisJoinPointStaticPart);
    }
    before() : call (void SubSub.run()) {
        advice("SubSub", thisJoinPointStaticPart);
    }
    before() : call (void SubSubSub.run()) {
        advice("SubSubSub", thisJoinPointStaticPart);
    }
    after() returning : execution(void CallReference.main(String[])) {
        Tester.checkAllEvents();
    }
}
