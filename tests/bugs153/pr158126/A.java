public class A {

        @MyAnnotation
        public A() {
                new B();
        }

        @MyAnnotation
        public A(int i) {
                new B(i);
        }

        public static void main(String[] args) {
                new A();
                new A(1);
        }
}
