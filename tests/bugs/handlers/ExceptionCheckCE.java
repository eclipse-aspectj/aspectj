

public class ExceptionCheckCE {

    void go() {
        try {
            String s = new String("hello");
        } catch (RuntimeException e) { // CW 8: pointcut works, but can't throw Exceptions
            // body of handler here
            if (null == e) {           
                throw new Error("never happens");
            }
        }
    }
}

/** @testcase PR#37898 advice on handler join points should not throw unpermitted checked exceptions */
aspect A {

    pointcut goHandler() :
        handler(RuntimeException) && withincode(void go());

    declare warning : goHandler() : "expected warning"; // just verifying pointcut

    before() throws Exception : goHandler() { // CE 25 can't throw from handler
        throw new Exception("bad"); 
    }

}
    