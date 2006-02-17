
public class A {
    static Object s;
    public static void main(String[] args) {
        String t = "Hello, World!";
        t.toString();
        if (s != t) throw new Error();
    }
    static abstract aspect GenericAspect<T> {
        abstract pointcut checkpoint(T t);

        // advice declaration causes error
        after(T t): checkpoint(t) { s = t;}
    }
    static aspect AAA extends GenericAspect<String>{
        pointcut checkpoint(String s) : target(s) && 
            call(String String.toString());
    }  
}
