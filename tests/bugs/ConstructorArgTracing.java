// from Bug#:  28852  

public class ConstructorArgTracing {
    public ConstructorArgTracing(int arg) {
    }

    public static void main(String[] args) {
        ConstructorArgTracing account = new ConstructorArgTracing(12345);
    }
}

aspect TraceAspect {
    before() : !within(TraceAspect) {
        System.out.println(thisJoinPoint);
    }
}
