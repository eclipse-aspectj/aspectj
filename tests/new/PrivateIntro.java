import org.aspectj.testing.Tester;

public class PrivateIntro {
    public static void test() {
        Tester.checkEqual(new A1().getWhere(), "A1", "from A1");
        Tester.checkEqual(new A2().getWhere(), "A2", "from A2");

    }

    public static void main(String[] args) {
        test();
    }
}


class A1 {
    private introduction Foo {
        String fromWhere() {
            return "A1";
        }
    }

    public String getWhere() {
        return new Foo().fromWhere();
    }
}

class A2 {
    private introduction Foo {
        String fromWhere() {
            return "A2";
        }
    }

    public String getWhere() {
        return new Foo().fromWhere();
    }
}

class Foo {}
