package one;

/** @testcase PR#548 introduced methods have incorrect package */
public aspect Aspect {
    public void C.foo() { // workaround: add qualification: one.C.foo
        /** @testcase PR#548 introduction method casting this to introduction type */
        ((C) this).protectedMethod();  // bad CE: Can't convert from two.C to one.C
        /** @testcase PR#548 introduction method accessing protected method */
        protectedMethod();             // bad CE: can't find any method with name: protectedMethod
        /** @testcase PR#548 introduction method accessing public method */
        publicMethod();                // bad CE: can't find any method with name: publicMethod
        /** @testcase PR#548 introduction method accessing default method */
        defaultMethod();               // bad CE: can't find any method with name: defaultMethod

        /** @testcase PR#548 introduction method accessing protected field */
        int i = protectedInt;          // bad CE: can't bind name: protectedInt
        /** @testcase PR#548 introduction method accessing private field */
        int j = publicInt;             // bad CE: can't bind name: publicInt
        /** @testcase PR#548 introduction method accessing default field */
        int k = defaultInt;            // bad CE: can't bind name: defaultInt
        int l = i * j * k;           
        //privateMethod();             // todo error case
        //int p = privateInt;          // todo error case
    }
}
