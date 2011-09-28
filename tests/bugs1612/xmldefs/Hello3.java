public class Hello3 {

        public static void main(String[] args) {
                say1("hello");
        } 

        public static void say1(String h) {
                System.out.println(h);
                String s = say2("world");
		System.out.println("from say2="+s);
        } 

        public static String say2(String w) {
                System.out.println(w);
                return "";
        }
}
