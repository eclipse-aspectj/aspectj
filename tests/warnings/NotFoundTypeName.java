aspect NotFoundTypeName {

    pointcut foo1(): this(X); // this should be a warning under -Xlint

    pointcut foo2(): this(java.util.*); // NOT a warning
    pointcut foo3(): this(*..TimeZone); // NOT a warning

    pointcut foo4(): this(java.util.X*Y); // this should be a warning under -Xlint
    pointcut foo5(): this(java.XYZ*.*); // this should be a warning under -Xlint


    pointcut foo6(): this(X*Y); // this should be a warning under -Xlint

    pointcut foo7(): this(NotFound*Name); // NOT a warning

    pointcut foo8(): this(NotFoundTypeNameInDirectory); // NOT a warning

    pointcut foo9a(): this(a.b.c.NotFoundTypeNameInDirectory2); // NOT a warning

    pointcut foo9b(): this(*..NotFoundTypeNameInDirectory2); // NOT a warning

    pointcut foo10(): this(a.b.c.Ningo*); // this should be a warning under -Xlint
    
    public static void main(String[] args) {
        System.out.println("not found");
    }
}

