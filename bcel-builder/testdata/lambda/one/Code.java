public class Code {
    public static void main(String[] args) {
        Runnable r = () -> { System.out.println("hello world!"); };
        r.run();
    }

}
