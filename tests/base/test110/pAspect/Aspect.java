package pAspect;

public aspect Aspect {
    public static boolean ranAdvice = false;

     before (): target(pClass.Class) && call(* foo(..)) {
        ranAdvice = true;
    }
}
