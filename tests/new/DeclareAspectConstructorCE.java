

/** @testcase PR851 declaring an aspect constructor with argument should be prohibited */

aspect A {
    A() {}
}

aspect B {
    A.new(int i) {}  // CE 10
}
