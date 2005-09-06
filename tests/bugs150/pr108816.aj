aspect BadAdvice {
    after(Object controller) returning (Object foo): 
      cflow(adviceexecution() && args(controller, ..) && this(BadAdvice)) && 
      call(Bar+.new(..)) 
    {
    }    
    
    Object around(Object controller) : call(* whoKnows()) && target(controller) 
    {
        return new Bar();
    }
    
    public static void main(String args[]) {
        (new Bar()).whoKnows();
    }
}

class Bar {
    void whoKnows() {}
}