

/** @testcase PR#40814 compile error expected for pointcuts in interfaces */
interface I {
    abstract pointcut pc(); // CE
    pointcut publicCalls() : call(public * *(..)) || call(public new(..)); // CE
}