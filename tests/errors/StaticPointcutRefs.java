public class StaticPointcutRefs {
}

abstract aspect A1 {
    abstract pointcut ocut(); //: call(* *(..));

    static aspect AI {
        before(): ocut() {} //ERROR static reference to abstract PCD
    }
}
