public class Bugs {
        public static void main(String[] args) {
                System.out.println("" + (new C())); // + instanceof A.I));
        }
        static class C{}

        static aspect A {
                private interface I {}
                declare parents : C implements I;       
        }
}
