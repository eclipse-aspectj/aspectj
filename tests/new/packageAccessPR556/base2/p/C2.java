package p;

aspect A2 {
    private static int privateOne = 1;
    static int defaultOne = 1;
    protected static int protectedOne = 1;
    public static int publicOne = 1;
    pointcut p() : within (p..*) ;
}

class C2 {
    private static int privateOne = 1;
    static int defaultOne = 1;
    protected static int protectedOne = 1;
    public static int publicOne = 1;
    pointcut p() : within (p..*) ;
}
