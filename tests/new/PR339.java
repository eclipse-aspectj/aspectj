public class PR339 {
    public static void main(String[] args) {
        new MemberInitSet().foo();
        org.aspectj.testing.Tester.check(true, "compiled");
    }
}

class MemberInitSet {
    String s = "S";
    { s="s"; }
    void foo() { s = "asdf"; }
}

aspect Setter pertarget(target(MemberInitSet)) {
    pointcut allSets(): set(String MemberInitSet.s);
}
