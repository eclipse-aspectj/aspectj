package pack;

public class C {

        public C() {
        }

        public void method1() {
                new C().method2();
        }

        public void method2() {
        }

        public static void main(String[] args) {
                new C().method1();
        }

}
