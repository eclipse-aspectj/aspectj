import org.aspectj.testing.Tester;

public class Statics {
    public static void main(String[] args) {
        new C();
        Tester.checkEqual(C.getCount(), 1, "C.getCount()");
        Tester.checkEqual(SubC.getCount(), 0, "SubC.getCount()");
        new SubC();
        Tester.checkEqual(C.getCount(), 1, "C.getCount()");
        Tester.checkEqual(SubC.getCount(), 1, "SubC.getCount()");
        new SubC();
        Tester.checkEqual(C.getCount(), 1, "C.getCount()");
        Tester.checkEqual(SubC.getCount(), 2, "SubC.getCount()");

        I.count += 1;
        Tester.checkEqual(I.getCount(), 1, "I.getCount()");

        Tester.checkEqual(I1.count, 10, "I1.count"); 
        Tester.checkEqual(SubI.count, 20, "SubI.count"); 
        Tester.checkEqual(SubI.getCount(), 1, "SubI.getCount()");
    }
}


class C { }

class SubC extends C {}

interface I {}

interface I1 {
    int N = -1;
    Object o = new Integer(2);
}

interface SubI extends I, I1 { }

aspect Counter {
    private static int C.instanceCount = 0;
    private void C.incCount() { instanceCount++; }
    static int C.getCount() { return instanceCount; }
    private static int SubC.instanceCount = 0;
    private void SubC.incCount() { instanceCount++; }
    static int SubC.getCount() { return instanceCount; }

    static int I.count = 0;
    static int I.getCount() { return count; }

    static int I1.count = 10;

    static int SubI.count = 20;

    after() returning (C c) : call(C+.new(..)) {
        c.incCount();
    }
}
