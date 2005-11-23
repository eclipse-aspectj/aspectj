public class Pr114495 {
        public static void main(String[] args) {
                C.go();
        }
        static class C {
                static void go() {}
        }
}

abstract aspect AA_ParameterizedTypeInPointcut<Target> {
        pointcut going() :call(void Target.go()) ;
        before() : going() {
                System.out.println("AA.going()");
        }
}
aspect A_ParameterizedTypeInPointcut 
extends AA_ParameterizedTypeInPointcut<Pr114495.C> {
        declare warning : going() : "going()"; // works fine
        before() : going() { // advice not applied
                System.out.println("A.going()");
        }
}