

public class MultiArgHelloWorld {

    public static void main(String[] args) {
    	foo("Hello", "World");
    }
    
    static void foo(Object s, Object t) {
    	System.out.println(s + " " + t);
    }
}
