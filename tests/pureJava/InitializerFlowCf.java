public class InitializerFlowCf {
    { throw new Error("bad error"); }  // ERR initializer can't complete normally

    static { throw new Error("bad error #2"); } // ERR static initializer can't complete normally
}


    
