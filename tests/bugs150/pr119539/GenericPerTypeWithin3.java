package bugs;
// this one matches the bug report ... apart from switching to before() advice as ctor-exec JPs have void return
public class GenericPerTypeWithin3 {

    public static void main(String[] args) {
        new C(); // fyi, compiler does nothing absent this call?
    }
    public static abstract aspect Singleton<Target> pertypewithin(Target) {
        pointcut creation() : execution(Target+.new()) ;
        before() : creation() { }
        // picks out constructor-execution below
        declare warning : creation() : "Singleton.creation()";
    }
    static class C {
        C(){}
    }
    static aspect A extends Singleton<C> {}
    
}