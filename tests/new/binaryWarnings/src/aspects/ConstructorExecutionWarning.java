
package aspects;

import app.Main;

// WARNING: do not change message text without changing test specification
public privileged aspect ConstructorExecutionWarning {

    declare warning : execution(Main.new()) // 23 (bug: 8)
        : "execution(Main.new())";

}