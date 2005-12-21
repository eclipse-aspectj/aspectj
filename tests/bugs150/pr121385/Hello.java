public class Hello {

        public static void main(String[] args) {
                sayHello();
        } 

        public static void sayHello() {
                System.out.println("Hello");
                sayWorld();
        } 

        public static int sayWorld() {
                System.out.println("World");
                return 0;
        }
}
