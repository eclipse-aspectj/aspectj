public class SampleExceptionHandling1 {
    public void mumble() throws java.io.IOException { } // CE expected
}


/** @author Ron Bodkin */
aspect Library {
    public pointcut executionsThrowingChecked() : 
        execution(* *(..) throws (Exception+ && !RuntimeException));
}

/** @author Ron Bodkin */
aspect SampleExceptionHandling {
    public pointcut scope() : within(SampleExceptionHandling1);
    
    public pointcut executionsThrowingChecked() : 
        Library.executionsThrowingChecked() && scope();

    declare error : executionsThrowingChecked(): 
        "no checked exceptions";
}