

public class CompileWarning { 
    void run() {
    }
    static aspect A {
        declare warning : execution(void CompileWarning.run()) : 
            "expected warning";
    }
}