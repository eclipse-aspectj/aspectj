public class Hello2 {

        public static void main(String[] args) {
                say1("hello");
        } 

        public static void say1(String h) {
                System.out.println(h);
                say2("world");
        } 

        public static int say2(String w) {
                System.out.println(w);
                return 0;
        }
}
