
public aspect InpathAspect {
    after() returning : staticinitialization(Default)
        || staticinitialization(pack.Pack) {
        System.out.println("initialized " + thisJoinPoint);
    }
}