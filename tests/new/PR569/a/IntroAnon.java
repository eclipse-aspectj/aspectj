package a;

import org.aspectj.testing.Tester; 

import b.Dest;

/** @testcase PR#569 anon class written to wrong directory */
public class IntroAnon {
    private static aspect MI {
        public Object MyInterface.foo () {
            Tester.event("foo ran");
            return new Object(){};
        }
    }
    public static void main (String args []) {
        Tester.expectEvent("foo ran");
        new Dest ().foo ();
        Tester.checkAllEvents();
    }
} // end of class IntroAnon


