public aspect Code {

        pointcut init(): initialization(Object+.new(..));

        pointcut staticinit(): staticinitialization(Object+);

        Class around(String className): cflowbelow(init() || staticinit()) &&
call(Class Class.forName(String)) && args(className) {
                System.out.println("Test");
                return proceed(className);

        }
  public static void main(String[] argv) {
    new SomeClass();
  }
}

class SomeClass implements SomeInterface {

}

interface SomeInterface {
        Class ADAPTER = SomeInterface.class;
}

