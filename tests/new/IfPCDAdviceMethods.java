
import org.aspectj.testing.Tester; 
import org.aspectj.testing.Tester;

class TestContext {
	public static void signal(String event) {
		Tester.event(event);
	}
	public static void expectSignal(String event) {
		Tester.expectEvent(event);
	}
	public static void startTest() {
	}
	public static void endTest() {
        Tester.checkAllEventsIgnoreDups();
	}
	public static void testFailed(String failureMessage) {
        Tester.check(false,failureMessage);
	}
}

class BaseApp {
	int i;
    int get()  { return i; }
    void set(int i)  {  this.i = i; }
    void uncountedCall()  { }
	// permits call to be restricted to within(BaseApp)
    void callFromOutside(int i)  { 
		call(i);
	} 
    private void call(int i)  { TestContext.signal("call(int)"); } 
}

/**
 * Test touching pcd if() variants: 
 * <table>
 * <tr><td>pcd if(expr)</td><td>anonymous, named</td></tr>
 * <tr><td>pcd combination</td><td>execution, call, callTyped,initialization,set,get</td></tr>
 * <tr><td>advice</td><td></td>before, after, around</tr>
 * </table>
 * (callTyped is probably not a relevant difference).
 * Currently passing.
 * @author wes
 * History
 *   8/20/01  initial draft
 *   8/21/01  fixed namedIf call, added initializations
 * Todo
 *       
 */
public class IfPCDAdviceMethods {
    public static void main(String[] args) {
        TestContext.startTest();
        BaseApp target = new BaseApp();
		target.callFromOutside(0);
		target.uncountedCall();
		target.set(1);
		if (!(1 == target.get())) {
			TestContext.testFailed("1 != target.get()");
		}
        TestContext.endTest();
    }
    static {
		// variants of advice defined below
        String[] cuts = { "call_pc", "callType_pc", "execution_pc",
						  "get_pc", "set_pc", "initialization_pc" } ; 
        String[] kinds = { "before", "after", "around" };
        String[] ifs = { "if(true)", "namedIf()", "anonymous" };
        for (int i = 0; i < cuts.length; i++) {
            for (int j = 0; j < kinds.length; j++) {
				for (int k = 0; k < ifs.length; k++) {
					//XXX no around on initialization yet
					if (kinds[j].equals("around") && cuts[i].equals("initialization_pc")) continue;
					TestContext.expectSignal(kinds[j] + "." + cuts[i] + "." + ifs[k]);
				}
            }
        }
		// ensure BaseApp method was called
		TestContext.expectSignal("call(int)");

		// Aspect namedIf delegate should have been called this many times
        // (todo: only checks for at-least, not for extra calls)
         // -1 for anonymous, 2* for two calls, -1 for around initialization
		final int namedIfCalls = 2*cuts.length * (ifs.length-1) - 1;
        for (int i = 0; i < namedIfCalls; i++) {
			TestContext.expectSignal("executedNamedIf:"+i);
		}
    }
}

/** Catch test failures using aspects - mainly to surface caller/join point easily */
aspect TestSignals {
	/** identify methods that should never be called */
	pointcut errorIfCalled () : call(boolean *..executedNamedIfNever(..));
	/** signal failure if method that wasn't supposed to be called is in fact invoked */
	after () : errorIfCalled() {
		// todo: will StaticPart will always have null for source ?
		StringBuffer sb = new StringBuffer();
		sb.append("TestSignals.after() : errorIfCalled()");
		org.aspectj.lang.JoinPoint.StaticPart sp = thisJoinPointStaticPart;
		if (null == sp) {
			sb.append("null thisJoinPointStaticPart");
		} else {
			sb.append(" kind=" + sp.getKind());
			sb.append(" signature=" + sp.getSignature());
			sb.append(" source=" + sp.getSourceLocation());
		}
		TestContext.testFailed(sb.toString());
	}
}

/** named pointcuts including if(expr) - call, execution only */
aspect Aspect {
	static int namedIfCounter;
	static int namedIfNeverCounter;
	static int i;
	static boolean executedNamedIf() {
		//System.err.println("named if + " + namedIfCounter);
		a("executedNamedIf:"+namedIfCounter++);
		return true;
	}
	static boolean executedNamedIfNever() {
		a("executedNamedIfNever:"+namedIfNeverCounter++);
		return true;
	}

	/**
	 * should not short-circuit, but instead call executedNamedIf
	 * @testTarget ifpcd.run.expr.java.group no short-circuit of false "or"
	 * @testTarget ifpcd.run.expr.java.group method invocation in if-expression
	 */
	pointcut namedIf () 
		: !within(Aspect) && if( ((1 == 0) || executedNamedIf()) ) ; 

	/**
	 * should short-circuit, never calling executedNamedIfNever
	 * @testTarget ifpcd.run.expr.java.group short-circuit of false "and"
	 * @testTarget ifpcd.run.expr.java.group assignment in if-expression
	 */
	pointcut namedIfNever () // should short-circuit, never calling executedNamedIfNever
		: if( ((1 == 1) && executedNamedIfNever()) ) ;

	/**
	 * @testTarget ifpcd.run.expr.literal literal operations not optimized away fyi
	 */
    pointcut ifTrue () : if(true);

	/**
	 * @testTarget ifpcd.run.expr.literal literal operations not optimized away fyi
	 */
    pointcut ifFalse () : if(false);

	// ------------------------------------- pointcuts
	/** @testTarget ifpcd.compile.pcds.named.set */
    pointcut set_pc     (): if (true) && set(int BaseApp.i) ; 
	/** @testTarget ifpcd.compile.pcds.named.get */
    pointcut get_pc     (): if (true) && get(int BaseApp.i) ; 
	/** @testTarget ifpcd.compile.pcds.named.call */
    pointcut call_pc     (): if (true) && call(void *.call(int)) && within(BaseApp);
	/** @testTarget ifpcd.compile.pcds.named.call with Type */
    pointcut callType_pc(): if(true) && call(void BaseApp.call(int));
	/** @testTarget ifpcd.compile.pcds.named.execution */
    pointcut execution_pc(): if(true) && within(BaseApp) && execution(void *(int));
	/** @testTarget ifpcd.compile.pcds.named.initialization */
    pointcut initialization_pc(): if(true) && initialization(BaseApp.new(..));
	// currently broken
	/** @testTarget ifpcd.compile.pcds.named.initialization */
    //pointcut staticinitialization_pc(): if(true) && staticinitialization(BaseApp);

	/** @testTarget ifpcd.compile.pcds.namedIf.set */
    pointcut named_set_pc     (): namedIf() && set(int BaseApp.i) ; 
	/** @testTarget ifpcd.compile.pcds.namedIf.get */
    pointcut named_get_pc     (): namedIf() && get(int BaseApp.i) ; 
	/** @testTarget ifpcd.compile.pcds.namedIf.call if pcd by name composition */
    pointcut named_call_pc()     : namedIf() && call(void *.call(int)) && within(BaseApp);
	/** @testTarget ifpcd.compile.pcds.namedIf.call with Type, if pcd by name composition */
    pointcut named_callType_pc() : namedIf() && call(void BaseApp.call(int)) && within(BaseApp);;
	/** @testTarget ifpcd.compile.pcds.namedIf.execution if pcd by name composition */
    pointcut named_execution_pc(): namedIf() && execution(void *(int));
	/** @testTarget ifpcd.compile.pcds.namedIf.initialization */
    pointcut named_initialization_pc(): namedIf() && initialization(BaseApp.new(..));

    before(): set_pc     () { a("before.set_pc.if(true)");      }
    before(): get_pc     () { a("before.get_pc.if(true)");      }
    before(): call_pc     () { a("before.call_pc.if(true)");      }
    before(): callType_pc() { a("before.callType_pc.if(true)"); }
    before(): execution_pc() { a("before.execution_pc.if(true)"); }
    before(): initialization_pc() { a("before.initialization_pc.if(true)"); }
    //before(): staticinitialization_pc() { a("before.staticinitialization_pc.if(true)"); }

    before(): named_set_pc     () { a("before.set_pc.namedIf()");      }
    before(): named_get_pc     () { a("before.get_pc.namedIf()");      }
    before(): named_call_pc     () { a("before.call_pc.namedIf()");      }
    before(): named_callType_pc() { a("before.callType_pc.namedIf()"); }
    before(): named_execution_pc() { a("before.execution_pc.namedIf()"); }
    before(): named_initialization_pc() { a("before.initialization_pc.namedIf()"); }

    Object around() : set_pc     () { a("around.set_pc.if(true)");  return proceed(); }
    int around() : get_pc     () { a("around.get_pc.if(true)");  return proceed(); }
    void around() : call_pc     () { a("around.call_pc.if(true)");      proceed(); }
    void around() : callType_pc() { a("around.callType_pc.if(true)"); proceed(); }
    void around() : execution_pc() { a("around.execution_pc.if(true)"); proceed(); }
    //XXXvoid around() : initialization_pc() { a("around.initialization_pc.if(true)"); proceed(); }

    Object around() : named_set_pc     () { a("around.set_pc.namedIf()"); return proceed(); }
    int around() : named_get_pc     () { a("around.get_pc.namedIf()"); return proceed(); }
    void around() : named_call_pc     () { a("around.call_pc.namedIf()");      proceed(); }
    void around() : named_callType_pc() { a("around.callType_pc.namedIf()"); proceed(); }
    void around() : named_execution_pc() { a("around.execution_pc.namedIf()"); proceed(); }
    //XXXvoid around() : named_initialization_pc() { a("around.initialization_pc.namedIf()"); proceed(); }

	// ------------------------------------- after 
    after(): set_pc     () { a("after.set_pc.if(true)");      }
    after(): get_pc     () { a("after.get_pc.if(true)");      }
    after(): call_pc     () { a("after.call_pc.if(true)");      }
    after(): callType_pc() { a("after.callType_pc.if(true)"); }
    after(): execution_pc() { a("after.execution_pc.if(true)"); }
    after(): initialization_pc() { a("after.initialization_pc.if(true)"); }
    //after(): staticinitialization_pc() { a("after.staticinitialization_pc.if(true)"); }

    after(): named_set_pc     () { a("after.set_pc.namedIf()");      }
    after(): named_get_pc     () { a("after.get_pc.namedIf()");      }
    after(): named_call_pc     () { a("after.call_pc.namedIf()");      }
    after(): named_callType_pc() { a("after.callType_pc.namedIf()"); }
    after(): named_execution_pc() { a("after.execution_pc.namedIf()"); }
    after(): named_initialization_pc() { a("after.initialization_pc.namedIf()"); }

    static void a(String msg) {
        TestContext.signal(msg);
    }
}

/** anonymous pointcuts including if(expr) - call, execution only */
aspect Aspect2 {

	/** @testTarget ifpcd.compile.pcds.unnamed.set.before */
    before() : if(true) && set(int BaseApp.i) {
		a("before.set_pc.anonymous");
	}
	/** @testTarget ifpcd.compile.pcds.unnamed.get.before */
    before() : if(true) && get(int BaseApp.i) {
		a("before.get_pc.anonymous");
	}
	/** @testTarget ifpcd.compile.pcds.unnamed.call.before */
    before() : if(true) && call(void *.uncountedCall()) {
		a("before.call_pc.anonymous");
	}
	/** @testTarget ifpcd.compile.pcds.unnamed.call.before with Type */
    before() : if(true) && call(void BaseApp.uncountedCall()) {
		a("before.callType_pc.anonymous");
	}
	/** @testTarget ifpcd.compile.pcds.unnamed.execution.before */
    before() : if(true) && execution(void BaseApp.uncountedCall()) {
		a("before.execution_pc.anonymous");
	}
	/** @testTarget ifpcd.compile.pcds.unnamed.initialization.before */
    before() : if(true) && initialization(BaseApp.new(..)) {
		a("before.initialization_pc.anonymous");
	}

	/** @testTarget ifpcd.compile.pcds.unnamed.set.around */
    Object around() : if(true) && set(int BaseApp.i) {
		a("around.set_pc.anonymous");
		return proceed();
	}
	/** @testTarget ifpcd.compile.pcds.unnamed.get.around */
    int around() : if(true) && get(int BaseApp.i) {
		a("around.get_pc.anonymous");
		return proceed();
	}
	/** @testTarget ifpcd.compile.pcds.unnamed.call.around */
    void around() : if(true) && call(void *.uncountedCall()) {
		a("around.call_pc.anonymous");
		proceed();
	}
	/** @testTarget ifpcd.compile.pcds.unnamed.call.around with Type */
    void around() : if(true) && call(void BaseApp.uncountedCall()) {
		a("around.callType_pc.anonymous");
		proceed();
	}
	/** @testTarget ifpcd.compile.pcds.unnamed.execution.around */
    void around() : if(true) && execution(void BaseApp.uncountedCall()) {
		a("around.execution_pc.anonymous");
		proceed();
	}
	/** @testTarget ifpcd.compile.pcds.unnamed.initialization.around */
	//XXX
//    void around() : if(true) && initialization(BaseApp.new(..)) {
//		a("around.initialization_pc.anonymous");
//		proceed();
//	}

	/** @testTarget ifpcd.compile.pcds.unnamed.set.after */
    after() : if(true) && set(int BaseApp.i) {
		a("after.set_pc.anonymous");
	}

	/** @testTarget ifpcd.compile.pcds.unnamed.get.after */
    after() : if(true) && get(int BaseApp.i) {
		a("after.get_pc.anonymous");
	}

	/** @testTarget ifpcd.compile.pcds.unnamed.call.after */
    after() : if(true) && call(void *.uncountedCall()) {
		a("after.call_pc.anonymous");
	}

	/** @testTarget ifpcd.compile.pcds.unnamed.call.after with Type */
    after() : if(true) && call(void BaseApp.uncountedCall()) {
		a("after.callType_pc.anonymous");
	}

	/** @testTarget ifpcd.compile.pcds.unnamed.execution.after */
    after() : if(true) && execution(void BaseApp.uncountedCall()) {
		a("after.execution_pc.anonymous");
	}
	/** @testTarget ifpcd.compile.pcds.unnamed.initialization.after */
    after() : if(true) && initialization(BaseApp.new(..)) {
		a("after.initialization_pc.anonymous");
	}


    static void a(String msg) {
        TestContext.signal(msg);
    }
}
