import org.sablecc.sablecc.node.*;
import org.sablecc.sablecc.analysis.*;
import org.sablecc.sablecc.parser.*;

public aspect ParserAspect {

    pointcut pushes():      call(void push(int,Object)) && target(Parser);
    pointcut callstoSets(): call(void set*(Object))     && target(Node);

    before ()              : pushes() {}
    void around ()         : pushes() { proceed(); }

    before ()              : callstoSets() {}
    void around ()         : callstoSets() { proceed(); }

    after  ()              : callstoSets() {}
    after  ()              : pushes() {}
}
