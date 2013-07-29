import java.util.Arrays;


interface I {
    // Default method
    default void foo() {
        System.out.println("ABC");
    }
}

public class C implements I{
    public static void main(String[] args) {
        new C().foo();
        // Lambda
        Runnable r = () -> { System.out.println("hello world!"); };
        r.run();
        // Used Java8 b97
        Arrays.asList(MyClass.doSomething()).forEach((p) -> System.out.println(p));
    }
}

aspect X {
before(): execution(* I.foo()) {
   System.out.println("I.foo running");
}
before(): staticinitialization(!X) {
System.out.println("Clazz "+thisJoinPointStaticPart);
}
}


class Utils {
    public static int compareByLength(String in, String out) {
        return in.length() - out.length();
    }
}

class MyClass {
    public static String[] doSomething() {
        String []args = new String[]{"4444","333","22","1"};
        // Method reference
        Arrays.sort(args,Utils::compareByLength);
        return args;
    }
}
