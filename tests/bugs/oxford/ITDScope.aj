class C {
    
    static int static_c = 0;
    int c = 0;
    
}

aspect A {
    static int static_a = 0;
    int a = 0;
    
    private void C.itdFromA() {
        c = 1;  // ok
        static_c = 1; // not ok - use C.static_c;
        static_a = 1; // ok
        a = 1; // not ok
    }
}