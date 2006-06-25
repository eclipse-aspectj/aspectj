public class MyClass {

        public MyEnum getValue() {
                return MyEnum.ONE;
        }

        @MyAnnotation({ MyEnum.ONE, MyEnum.TWO })
        public void test() {
        }

        public static void main(String[] args) {
                new MyClass().test();
        }
}