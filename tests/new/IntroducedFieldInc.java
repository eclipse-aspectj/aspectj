
import org.aspectj.testing.Tester;

interface I {}

/** @testcase PR#757 Incrementing interface-introduced field */
public class IntroducedFieldInc implements I{
    public static void main (String args[]) {
        IntroducedFieldInc i  = new IntroducedFieldInc();
        // no bug 
        Tester.check(1 == (((I)i).count = 1), "((I)i).count = 1");
        Tester.check(2 == (++(i).count), "++((I)i).count");
        // bug
        Tester.check(3 == (++((I)i).count), "++((I)i).count");
        Tester.check(3 == (((I)i).count++), "((I)i).count++");
        Tester.check(5 == (((I)i).count += 1), "((I)i).count += 1");

        Tester.checkEqual((getI().count += 1), 3, "getI().count += 1");
        Tester.checkEqual(getICount, 1, "getI() called");
    }

    static int getICount = 0;

    public static I getI() { 
        getICount++;
        return new IntroducedFieldInc(); 
    }

}

aspect A  {
    public int I.count = 2;
}
