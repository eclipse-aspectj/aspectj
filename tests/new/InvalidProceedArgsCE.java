
class Target {
    public void resize(int i) {}
}

/** @testcase invalid number and type of proceed arguments */
aspect A {
    void around(Target buffer)  
        : call(void Target.resize(..)) && target(buffer) {
        proceed(); // expect CE here 
    }
    void around(int i, Target buffer)  
        : call(void Target.resize(..)) && args(i) && target(buffer) {
        /** @testcase invalid proceed args - none, 2 expected */
        proceed();      // expect CE here
        /** @testcase invalid proceed args - 1, 2 expected */
        proceed(i);      // expect CE here
        // coercion is passing
        /** @testcase invalid proceed args - wrong type 1, 2 expected */
        proceed(buffer); // expect CE here
        /** @testcase invalid proceed args - wrong type 2, 2 expected */
        proceed(buffer, i); // expect CE here
    }
    void around(int i)  
        : call(void Target.resize(..)) && args(i) {
        /** @testcase invalid proceed args - wrong type */
        proceed(new Integer(0)); // expect CE here
        /** @testcase invalid proceed args - wrong type 2, 1 expected */
        proceed(new Integer(0), 0); // expect CE here
        /** @testcase invalid proceed args - wrong type 2, 1 expected */
        proceed(0, new Integer(0)); // expect CE here
    }

    void around(int i, Target buffer)
        : call(void Target.resize(..)) && args(i) && target(buffer) {
        /** @testcase invalid proceed args - float -> int */
        proceed(3.1, buffer);      // expect CE here
        /** @testcase invalid proceed args - String -> int */
        proceed("1", buffer);      // expect CE here
        proceed('a', buffer);      // char -> int is legal
        // coercion is passing
        /** @testcase invalid proceed args - Object -> Target */
        proceed(i, (Object)null); // expect CE here
        proceed(i, null); //null -> Target is legal
        /** @testcase invalid proceed args - wrong type 3-> Target */
        proceed(i, 3); // expect CE here
    }
} 
