package pack;

import org.aspectj.testing.Tester;

public aspect PackageWildcards {
    pointcut fooCut(): call(String foo());

    String around(): fooCut() && within(*) {
            String result = proceed();
            return result + ":fooCut";
    }

    pointcut allMethodsCut(): target(Foo) && call(!abstract String *(..));

    String around(): allMethodsCut() {
            String result = proceed();
            return result + ":allMethodsCut";
    }

    public static void test() {
        String message = new Foo().foo();
        //System.out.println(message);
        Tester.checkEqual(message, "foo:allMethodsCut:fooCut", "all advice active");
    }

    public static void main(String[] args) {
        test();
    }
}

class Foo {
    String foo() { return "foo"; }
}
