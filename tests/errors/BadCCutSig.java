package errors;

aspect BadCCutSig {
    int bar(int a, int b, int c) { return 0; }

    pointcut cut(BadCCutSig b): target(b) && call(int bar(int, int, int));
    
    before(BadCCutSig b): cut() {  // SHOULD BE: cut(b)
    }
}
