import org.aspectj.testing.Tester;
public class MethodIntroductions {
    public static void main(String[] args) {
        new MethodIntroductions().realMain(args);
    }
    public void realMain(String[] args) {

        AbstractSuper as0 = new AbstractSuper() { public int foo() { return 1; } };
        AbstractSuper as1 = new ExtendsAbstractSuper();
        AbstractSuper as2 = new ExtendsExtendsAbstractSuper();
        ExtendsAbstractSuper eas = new ExtendsAbstractSuper();
        ExtendsExtendsAbstractSuper eeas = new ExtendsExtendsAbstractSuper();
        Tester.checkEqual(as0.foo(),  1, "as0");
        Tester.checkEqual(as1.foo(),  2, "as1");
        Tester.checkEqual(as2.foo(),  3, "as2");
        Tester.checkEqual(eas.foo(),  2, "eas");
        Tester.checkEqual(eeas.foo(), 3, "eeas");

        Super s0 = new Super() {};
        Super s1 = new ExtendsSuper();
        Super s2 = new ExtendsExtendsSuper();
        ExtendsSuper es = new ExtendsSuper();
        ExtendsExtendsSuper ees = new ExtendsExtendsSuper();
        Tester.checkEqual(s0.foo(),  4, "s0");
        Tester.checkEqual(s1.foo(),  5, "s1");
        Tester.checkEqual(s2.foo(),  6, "s2");
        Tester.checkEqual(es.foo(),  5, "es");
        Tester.checkEqual(ees.foo(), 6, "ees");

        AbstractSuperNoIntro as0n = new AbstractSuperNoIntro() { public int foo() { return 7; } };
        AbstractSuperNoIntro as1n = new ExtendsAbstractSuperNoIntro();
        AbstractSuperNoIntro as2n = new ExtendsExtendsAbstractSuperNoIntro();
        ExtendsAbstractSuperNoIntro easn = new ExtendsAbstractSuperNoIntro();
        ExtendsExtendsAbstractSuperNoIntro eeasn = new ExtendsExtendsAbstractSuperNoIntro();
        Tester.checkEqual(as0n.foo(),  7, "as0n");
        Tester.checkEqual(as1n.foo(),  8, "as1n");
        Tester.checkEqual(as2n.foo(),  9, "as2n");
        Tester.checkEqual(easn.foo(),  8, "easn");
        Tester.checkEqual(eeasn.foo(), 9, "eeasn");
    }
}

interface I {
    public int foo();
}

abstract class AbstractSuper {}
class ExtendsAbstractSuper extends AbstractSuper {}
class ExtendsExtendsAbstractSuper extends ExtendsAbstractSuper {}

class Super {}
class ExtendsSuper extends Super {}
class ExtendsExtendsSuper extends ExtendsSuper {}

abstract class AbstractSuperNoIntro {}
class ExtendsAbstractSuperNoIntro extends AbstractSuperNoIntro {}
class ExtendsExtendsAbstractSuperNoIntro extends ExtendsAbstractSuperNoIntro {}

aspect Introducer {

    declare parents: AbstractSuper implements I;
    public int AbstractSuper.foo() { return 1; }
    public int ExtendsAbstractSuper.foo() { return 2; }
    public int ExtendsExtendsAbstractSuper.foo() { return 3; }

    declare parents: Super implements I;
    public int Super.foo() { return 4; }
    public int ExtendsSuper.foo() { return 5; }
    public int ExtendsExtendsSuper.foo() { return 6; }

    declare parents: AbstractSuperNoIntro implements I;
    public int ExtendsAbstractSuperNoIntro.foo() { return 8; }
    public int ExtendsExtendsAbstractSuperNoIntro.foo() { return 9; }

}
