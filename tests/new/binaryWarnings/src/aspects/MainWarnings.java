
package aspects;

import app.Main;

// WARNING: do not change message text without changing test specification
public privileged aspect MainWarnings {

    declare warning : staticinitialization(Main)
        : "staticinitializtion(Main)";
        
    declare warning : initialization(Main.new())
        : "initialization(Main.new())";

    declare warning : execution(Main.new())
        : "execution(Main.new())";

    declare warning : execution(void Main.go(String))
        : "execution(void Main.go(String))";

    declare warning : call(Main.new())
        : "call(Main.new())";

    declare warning : call(void Main.go(String))
        : "call(void Main.go(String))";

    declare warning : call(Main.new())
        && withincode(void Main.stop())
        : "call(Main.new()) && withincode(void Main.stop())";

    declare warning : call(void Main.stop())
    && withincode(void Main.go(String))
        : "call(void Main.stop()) && withincode(void Main.go(String))";

    declare warning : get(String Main.s)
        : "get(String Main.s)";

    declare warning : set(String Main.s)
        : "set(String Main.s)";

    declare warning : get(String Main.staticString)
        : "get(String Main.staticString)";

    declare warning : set(String Main.staticString)
        : "set(String Main.staticString)";

    declare warning : handler(RuntimeException)
        && within(Main)
        : "handler(RuntimeException) && within(Main)";

    declare warning : preinitialization(app.C.new())
        : "preinitialization(app.C.new())";
    
//    declare warning : adviceexecution() && within(app.AdvisingAspect)
//        : "adviceexecution() && within(app.AdvisingAspect)";
}