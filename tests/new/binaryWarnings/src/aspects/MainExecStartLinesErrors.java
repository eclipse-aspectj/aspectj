
package aspects;

import app.MainExecStartLines;

// WARNING: do not change message text without changing test specification
public privileged aspect MainExecStartLinesErrors {

    declare error : execution(void MainExecStartLines.main(String[]))
        : "execution(void MainExecStartLines.main(String[]))";

    declare error : handler(RuntimeException)
        && within(MainExecStartLines)
        : "handler(RuntimeException) && within(MainExecStartLines)";

}