


class C { void m() {  } }


/** @testcase type not imported in around advice */
aspect A {
    void around() : execution(void m()) {
        Rectangle expected = null;      // CE 10
    }
}
