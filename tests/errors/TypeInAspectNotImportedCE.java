
class C { void m() {  } }

/** @testcase type not imported in aspect */
aspect A {
    Rectangle C.bounds = null;  // CE 6
}
