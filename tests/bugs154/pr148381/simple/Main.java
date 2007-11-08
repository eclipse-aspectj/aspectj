package test;

public class Main {
        public static void main(String[] args) {
                new Main().foo();
        }

        @PerformanceMonitor(expected=1000)
        public void foo() {

        }
}
