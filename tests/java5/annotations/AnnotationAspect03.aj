public aspect AnnotationAspect03 {
    
    declare warning : execution(* *.*(..)) && @annotation(SimpleAnnotation)
                    : "@annotation matched here";
    
    
}