

/** @testcase PR#40814 compile error expected for pointcuts in interfaces
 *    revised to check for error on abstract pointcuts in interfaces or classes
 **/
interface I {
    abstract pointcut pc(); // CE
}

abstract class C {
	abstract pointcut pc(); // CE
}

class Concrete {
	abstract pointcut pc(); // CE
}
