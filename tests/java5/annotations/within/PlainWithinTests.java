public aspect PlainWithinTests {
    
    // CW L 21
    declare warning : execution(* foo()) && within(@MyAnnotation *)
       : "positive within match on annotation";
    
    // CW L25
    declare warning : execution(* foo()) && !within(@MyAnnotation *)
       : "negative within match on annotation";
    
    
}