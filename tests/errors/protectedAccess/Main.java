package protectedAccess;

import org.aspectj.testing.Tester;
import protectedAccess.p1.C1;

public class Main {
    public static void main(String[] args) {
        SubC1 subc1 = new SubC1();
        subc1.m(subc1, subc1);
    }
}

class SubC1 extends C1 {
    public void m(SubC1 subc1, C1 c1) {
        Tester.checkEqual(this.s, "protected");
        Tester.checkEqual(this.m(), "protected");

        Tester.checkEqual(s, "protected");
        Tester.checkEqual(m(), "protected");

        Tester.checkEqual(subc1.s, "protected");
        Tester.checkEqual(subc1.m(), "protected");

        C1 c1a = new C1() { };

        C1 c1b = new C1(); //ERROR: illegal protected access

        Tester.checkEqual(c1.s, "protected"); //ERROR: illegal protected access
        Tester.checkEqual(c1.m(), "protected"); //ERROR: illegal protected access

        Tester.checkEqual(c1.m(), "protected"); //ERROR: illegal protected access
    }

    class SubI1 extends I1 {
        public void m(SubC1 subc1, C1 c1, I1 i1) {
            Tester.checkEqual(s, "protected");
            Tester.checkEqual(m(), "protected"); //ERROR: method not found
            
            Tester.checkEqual(SubC1.this.s, "protected");
            Tester.checkEqual(SubC1.this.m(), "protected");

            Tester.checkEqual(subc1.s, "protected");
            Tester.checkEqual(subc1.m(), "protected");
            
            Tester.checkEqual(c1.s, "protected"); //ERROR: illegal protected access
            Tester.checkEqual(c1.m(), "protected"); //ERROR: illegal protected access

            Tester.checkEqual(si, "ip");
            Tester.checkEqual(mi(), "ip");

            Tester.checkEqual(this.si, "ip");
            Tester.checkEqual(this.mi(), "ip");

            Tester.checkEqual(i1.si, "ip"); //ERROR: illegal protected access
            Tester.checkEqual(i1.mi(), "ip"); //ERROR: illegal protected access
        }
    }

    protected String mString(Object o) {
        return o.toString();
    }
}
