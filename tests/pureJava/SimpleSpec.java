public class SimpleSpec {
    public void m() {
        throw new RuntimeException();
        throw new Integer(1);

        for (;;) { System.out.println("hi"); }
        for (;1;) { System.out.println("hi"); }
    }
}
