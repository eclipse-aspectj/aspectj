
// in 10rc2, this provokes a NPE at ConstructorDec.java:41
class error {
    class error2 {
        /** @testcase PR#588 PUREJAVA Undefined inner class constructor */
        public error2(); // expecting error here
    }
}

