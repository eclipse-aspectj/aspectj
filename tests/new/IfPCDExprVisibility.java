
aspect AspectForIfPCDExprVisibility {
	// See IfPCDExprJoinPoint* for join-point error cases 

	pointcut stringLArgs(String[] args)  
		: args (args);
	pointcut targetTarget(IfPCDExprVisibility target)  
		: target(target);
	pointcut thisItem(IfPCDExprVisibility thisItem)  
		: this(thisItem);

	pointcut callDo() 
		: call(void IfPCDExprVisibility());

	pointcut callMain(String[] args) 
		: args(args) && call(static void *..main(String[])) ;

	// ok: anonymous pointcut 
	/**
	 *@testTarget ifpcd.compile.visibility.tjp
	 *@testTarget ifpcd.compile.visibility.tjpsp
	*/
	before () 
		: if (thisJoinPoint != null) 
			&& if (thisJoinPointStaticPart != null) 
			&& call(void IfPCDExprJoinPointVisibleCE.main(..)) {
			System.err.println("before main + " + thisJoinPoint);
		}
	// ok: anonymous pointcut, name composition, arg state
	/**
	*/
	before (String[] args) 
		: if (thisJoinPointStaticPart != null) 
			&& if (null != args)
			&& callMain (args){
			String m = "before main" 
				+ " join point: " + thisJoinPoint
				+ " args: " + args ;
			System.err.println(m);
			if (null == thisJoinPointStaticPart) 
				throw new Error("impossible null thisJoinPointStaticPart");
			// actually, it is possible to directly invoke main with null args...
			if (null == args) throw new Error("null args");
		}
	/**
	 *@testTarget ifpcd.compile.visibility.args.named
	 *@testTarget ifpcd.compile.visibility.this.named
	 *@testTarget ifpcd.compile.visibility.target.named
	*/
	Object around (String[] _args
			, IfPCDExprVisibility _target
			, IfPCDExprVisibility _thisItem)
		: targetTarget(_target)
		&& thisItem(_thisItem)
		&& call(* IfPCDExprVisibility.exec(..))
		&& args(_args) 
		&& if(null != _args) 
		&& if(null != _target) 
		&& if(null != _thisItem) 
		{
		String m = "around main - start " 
			+ " join point: " + thisJoinPoint
			+ " static join point: " + thisJoinPointStaticPart
			+ " this: " + _thisItem 
			+ " target: " + _target 
			+ " args: " + _args 
			;
		System.err.println(m);
		// note: no compile error unless around is actually woven in
		proceed(_args, _target, _thisItem); 
		m = "around main - end " 
			+ " join point: " + thisJoinPoint
			+ " static join point: " + thisJoinPointStaticPart
			+ " this: " + _thisItem 
			+ " target: " + _target 
			+ " args: " + _args 
			;
		System.err.println(m);
		return null;
	} 
}

/**
 * @author wes
 */
public class IfPCDExprVisibility {
	void exec(String[] args) {
		if (null == args) {
			System.err.println("exec running with null args");
		} else {
			System.err.println("exec running with args: " + args);
			System.err.println("exec calling itself with null args: " + args);
			// only this call is captured by around - from/to this object
			exec(null); 
		}
	}
	public static void main(String[] args) {
		if (null != args) {
			System.err.println("main calling itself with null args");
			new IfPCDExprVisibility().main(null); // self-call
			System.err.println("main done calling itself with null args");
			
			new IfPCDExprVisibility().exec(args); 
		} else {
			System.err.println("ok - main running with null args");
		}
	}
} 

