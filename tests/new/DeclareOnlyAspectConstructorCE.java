


/** @testcase PR851 declaring an aspect constructor with argument should be prohibited - sole constructor */

aspect A {
}

aspect B {
    A.new(int i) { this(); }  // CE 10
}
