

public class SuperPointcutCE {
    public static void main(String[] a) {
        new C().run();
    }
}

class C {
    public void run(){ System.out.println("c");}
}

abstract aspect AA {
    pointcut pc() : call(public * *(..)) && !within(AA+);
    before() : pc() {
        System.out.println("here: " + thisJoinPointStaticPart);
    }
}

/** @testcase PR#40858 weaver trace on mis-qualified pointcut reference */
aspect B extends AA {
    
    pointcut pc() : super.pc()        // CE super not allowed in 1.1
        && !call(void println(..));
        
    pointcut blah() : UnknownType.pc();  // CE
}
