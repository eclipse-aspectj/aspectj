import org.sablecc.sablecc.node.*;
import org.sablecc.sablecc.analysis.Analysis;

public aspect NodeAspect {

    pointcut removes():     call(void removeChild(Node))     && target(Node);
    pointcut callstoSets(): call(void set*(Analysis,Object)) && target(Node);
    pointcut callstoGets(): call(void get*(Analysis))        && target(Node);

    before()             : removes()     {}
    void around() : removes()     { proceed(); }
    after ()             : removes()     {}

    before()             : callstoSets() {}
    void around() : callstoSets() { proceed(); }
    after ()             : callstoSets() {}

    before()             : callstoGets() {}
    void around() : callstoGets() { proceed(); }
    after ()             : callstoGets() {}
}
