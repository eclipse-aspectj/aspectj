import org.sablecc.sablecc.lexer.*;
import org.sablecc.sablecc.node.*;

public aspect LexerAspect {

    pointcut callstoSets():
        call(void set*(Object)) && target(Token);

    before ()              : callstoSets() {}
    void around () : callstoSets() { proceed(); }
    after  ()              : callstoSets() {}
}
