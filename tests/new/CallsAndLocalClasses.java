import org.aspectj.testing.Tester;

public class CallsAndLocalClasses {
    public static void main(String[] args) {
        Runnable r = new Outer().makeRunnable();
        r.run();

        Outer o = new Outer();
        o.toString();
        ((Comparable)o).toString();

        Tester.check("run from Outer");
        Tester.check("m");
        Tester.check("before run");
        Tester.check("before m");
    }
}


class Outer implements Comparable {
    public int compareTo(Object other) { Tester.note("m"); return 0; }

    public Runnable makeRunnable() {
        return new Runnable() {
                public void run() {
                    Tester.note("run from Outer");
                    compareTo(this);
                }
            };
    }
}

final class Foo {
    public String toString() { return "Foo"; }
}

aspect A {
    before(): call(void Runnable.run()) {
        Tester.note("before run");
    }
    before(): call(int compareTo(Object)) {
        Tester.note("before m");
    }
    before(): call(String Object.toString()) {
        System.out.println("before toString");
    }
}
