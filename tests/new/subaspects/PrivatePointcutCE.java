
/** @testcase PR#647 aspect with private abstract pointcut */
abstract aspect PrivatePointcutCE {
    /** @testcase abstract private pointcut */
    abstract private pointcut defined(); // CE expected here
}
