
/** @testcase PR#57432 NPE's when writing some declare error */
aspect A {
    declare error : get(java.io.PrintStream System.out)
        || call(void Throwable.printStackTrace(..)) 
    : "NPE";
}
