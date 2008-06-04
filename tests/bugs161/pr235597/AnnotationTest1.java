public class AnnotationTest1 {

    @SomeAnnotation
    public void test() {
        System.out.println("test 1");
    }

    public static void main(String[] args) {
        //CASE 1
        AnnotationTest1 test1 = new AnnotationTest1();
        test1.test();
        //CASE 2
        AnnotationTest2<Integer> test2 = new AnnotationTest2<Integer>();
        test2.test2();
        //CASE 3
        AnnotationTest3 test3 = new AnnotationTest3();
        test3.test3();
    }

    public static class AnnotationTest2<Type extends Object> {

        @SomeAnnotation
        public void test2() {
            System.out.println("test 2");
        }
    }

    public static class AnnotationTest3 extends AnnotationTest2<Double> {

        @SomeAnnotation
        public void test3() {
            System.out.println("test 3");
        }
    }
}
