public class TestMain {
    public static void main(String args[]) {
        TestMain main = new TestMain();
        System.out.println(main.foo());
        System.out.println(main.foo());
        System.out.println(new TestMain().foo());
        System.out.println(main.foo());
    }

    public Object foo() {
        return ctr;
    }

    Integer ctr = new Integer(0);
}

