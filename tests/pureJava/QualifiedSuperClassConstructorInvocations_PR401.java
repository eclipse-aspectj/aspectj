public class QualifiedSuperClassConstructorInvocations_PR401 {
    public static void main(String[] args) {
        ChildOfInner coi = new ChildOfInner();
        org.aspectj.testing.Tester.checkEqual(S.s, "Outer:Inner:ChildOfInner");
    }
}

class S {
    static String s = "";
}

class Outer {
    public Outer() {
        S.s += "Outer";
    }
         class Inner{
             public Inner() {
                 S.s += ":Inner";
             }
         }
}
class ChildOfInner extends Outer.Inner {
    ChildOfInner() {
        (new Outer()).super();
        S.s += ":ChildOfInner";
    }
}
