import org.aspectj.testing.Tester;

public class NewAnonymous {
    public static void main(String[] args) {
    	new C().m("foo");
    }
}


class C {
    private String f = "fC";
    public void m(final String s) {
        new Runnable() {
                public void run() {
                    System.out.println(s+":"+f);
                }
            }.run();
    }
}

aspect A {
    before(): call(Runnable+.new(..)) {
        System.out.println("new Runnable");
    }
}
