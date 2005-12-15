
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.aspectj.lang.JoinPoint;

public aspect pr119749 {
        // not inherited
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)     
        @interface Me { String value() default "Me"; }

        static class C {
                @Me()
                void m() throws Exception {}
        }
        
        static class D extends C{
                void m() {}
        }
        
        static class E {
                D d(){return null;}
                C c(){return null;}
                static aspect A {
                        declare warning: execution(C E.*()) : "C E.*()";  //L26
                        declare warning: execution(D E.*()) : "D E.*()";  // L25
                }
        }
        
        public static void main(String[] args) {
                C c = new C();
                D d = new D();
                C cd = d;
                try {c.m();} catch (Exception e) {}
                try {cd.m();} catch (Exception e) {}
                d.m();
        }
        
        static aspect A {
                static void log(JoinPoint jp, Object o) {
                        System.out.println("" + jp + ": " + o);
                }
                pointcut scope() : within(pr119749);
                pointcut execMe() :execution(@Me void m()) && scope();  // L17
                pointcut execEx() :execution(void m() throws Exception) && scope(); // L17
                pointcut execAnyEx() :execution(* *(..) throws Exception) && scope(); // L17
                pointcut callEx() :call(void m() throws Exception) && scope(); // L37,38
                declare warning : execMe() : "aa @Me void m()";
                declare warning : execEx() : "aa void m() throws Exception";
                declare warning : execAnyEx() : "aa * *(..) throws Exception";
                declare warning : callEx() : "aa call void m() throws Exception";
                before(Me me) : @annotation(me) && execMe() {
                    log(thisJoinPoint, "execMe[" + me.value() + "]");
                }
                before() : execEx() {
                        log(thisJoinPoint, "execEx");
                }
        }
}
