package test;

public aspect TestAspect {
        Object around() : within(TestClass) {
                return proceed();
        }

        after() : within(TestClass) {
        }
}

class TestClass {
        public void test() {
                try {
                        new String();
                } catch (Exception ex) {
                }
        }
}

