import org.aspectj.lang.annotation.SuppressAjWarnings;

public aspect Code {

        pointcut init(): initialization(Object+.new(..));

        pointcut staticinit(): staticinitialization(Object+);

//        Class around(String className): cflowbelow(init() || staticinit()) && call(Class Class.forName(String)) && args(className) {
//                System.out.println("Test");
//                return proceed(className);
//        }

    @SuppressAjWarnings("adviceDidNotMatch")
    Integer around(int i): cflowbelow(init() || staticinit()) && call(Integer Integer.valueOf(int)) && args(i) {
        System.out.println("Test");
        return proceed(i);
    }

    public static void main(String[] argv) {
    	new SomeClass();
    }
}

class SomeClass implements SomeInterface {

}

interface SomeInterface {
        Integer i = 45;
}
