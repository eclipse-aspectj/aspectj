

/** @testcase compile error expected for abstract pointcut outside abstract aspect */
class C {
    abstract pointcut pc();  // CE 5
}


aspect B {
    abstract pointcut pc();  // CE 10
}

