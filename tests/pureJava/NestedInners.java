import org.aspectj.testing.Tester;

public class NestedInners {
    public static void main(String[] args) {
        Runnable r = new Outer().m("arg:");
        r.run();
        Tester.check("arg:varg:0");
        Tester.check("deep-arg:varg:1");
        r.run();
        Tester.check("arg:varg:2");
        Tester.check("deep-arg:varg:3");
    }
}


class Outer {
    public Runnable m(final String sarg) {
        final String svar = "v"+sarg;

        return new Runnable() {
                int counter = 0;

                public void run() {
                    Tester.note(sarg + svar+counter++);
                    new Runnable() {
                            public void run() {
                                Tester.note("deep-" + sarg + svar+counter++);
                            }
                        }.run();
                }
            };
    }
}
