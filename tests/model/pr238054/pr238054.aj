class A {
        void x() {}
        void y() { x();x();}
}

aspect B {
        before() : call(* A.x()) {}
}
