public class Application {

    @Foo
    Runnable fromInnerClass() {
      return new Runnable() {
        @Override
        public void run() {
        }
      };
    }

    @Foo
    Runnable fromLambdaExpression() {
      return () -> { };
    }
}
