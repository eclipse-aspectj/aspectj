
/** @testcase expect CE for unterminated declare error */
aspect UnterminatedDeclareErrorCE {
    declare error : execution(void run()) : "error" 
}// CE 5

class C {
    // bug - get CE here if uncommented, but declare error unterminated
    // public void run() { }    
}

