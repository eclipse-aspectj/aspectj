import org.aspectj.testing.Tester;

public class AfterReturningParam {
    public static void main(String[] args) {
        
        AfterReturningParam p = new AfterReturningParam();
 //       Tester.checkAndClearEvents(new String[] { "constr exec as Object null" });
 //        see pr 103157 for reason why this no longer matches

        p.mInt();
        Tester.checkAndClearEvents(new String[] { "int as Object 2" });

        p.mObject();
        Tester.checkAndClearEvents(new String[] { "Object as Object ning", 
                                                  "Object (that is String) as String ning" });

        p.mVoid();
        Tester.checkAndClearEvents(new String[] { "void as Object null" });

    }

    public int mInt() { return 2; }
    public Object mObject() { return "ning"; }
    public void mVoid() { return; }

    AfterReturningParam() {
    }
}


aspect A {
    private void callEvent(String s, Object o) {
        Tester.event(s + " " + o);
    }

    after() returning (AfterReturningParam o) : execution(AfterReturningParam.new()) { // CW 35 in 1.0.4, no match
        callEvent("constr exec as constd object", o); 
    }
    after() returning (Object o) : execution(AfterReturningParam.new()) {  // CW 38 in 1.0.4, does match
    	                                                                       // in 1.5 does not match - no return value for this jp
        callEvent("constr exec as Object", o);
    }
    after() returning (String o) : execution(AfterReturningParam.new()) {  // CW 41 in 1.0.4, no match
        callEvent("constr exec as String", o);
    }

    after() returning (Object o) : execution(int AfterReturningParam.mInt()) {  // match
        callEvent("int as Object", o);
    }
    after() returning (String o) : execution(int AfterReturningParam.mInt()) {  // no match
        callEvent("int as String", o);
    }
    after() returning (Integer o) : execution(int AfterReturningParam.mInt()) { // no match
        callEvent("int as Integer", o);
    }

    after() returning (Object o) : execution(Object AfterReturningParam.mObject()) { // match
        callEvent("Object as Object", o);
    }
    after() returning (String o) : execution(Object AfterReturningParam.mObject()) { // match (interesting, matching by instanceof) 
        callEvent("Object (that is String) as String", o);
    }

    after() returning (Object o) : execution(void AfterReturningParam.mVoid()) {  // match
        callEvent("void as Object", o);
    }
    after() returning (String o) : execution(void AfterReturningParam.mVoid()) {  // CW 65 warning in 1.0.4 no match
        callEvent("void as String", o);
    }
}    
