import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * In this testcase we create a pointcut p1() which uses cflow and then we reference
 * it in two other anonymous pointcuts attached to advice.  The cflow() should be managed
 * by a counter (as no state is maintained) and the reused pointcut should not mean two
 * counters are created.  One counter should be created and shared.
 */
public class CounterTest02 {

	public static void main(String []argv) {
		new CounterTest02().sayMessage();
		int ctrs = ReflectionHelper.howManyCflowCounterFields(Cflow1.aspectOf());
		int stacks = ReflectionHelper.howManyCflowStackFields(Cflow1.aspectOf());
		if (ctrs!=1) 
			throw new RuntimeException("Should be one cflow counter, but found: "+ctrs);
		if (stacks!=1) 
			throw new RuntimeException("Should be one cflow stacks, but found: "+stacks);
		if (Cflow1.stackAdvice!=2) 
			throw new RuntimeException("Expected two piece of stack advice to run: "+Cflow1.stackAdvice);
		if (Cflow1.counterAdvice!=4) 
			throw new RuntimeException("Expected four pieces of counter advice to run: "+Cflow1.counterAdvice);
		
	}
	
	public void sayMessage() {
		printmsg("Hello "); printmsg("World\n");
	}
	
	public void printmsg(String msg) {
		System.out.print(msg);
	}
}

aspect Cflow1 {
	public static int stackAdvice = 0;
	public static int counterAdvice = 0;
	
	// CflowCounter created for this pointcut should be shared below!
	pointcut p1(): cflow(execution(* main(..)));
	
	before(): call(* print(..)) && p1() {
		// Managed by a CflowCounter
		Cflow1.counterAdvice++;
	}
	
	before(): call(* print(..)) && p1() {
		// Managed by a CflowCounter
		Cflow1.counterAdvice++;
	}
	
	before(Object o): call(* print(..)) && cflow(execution(* main(..)) && args(o)) {
		// Managed by a CflowStack - since state is exposed
		Cflow1.stackAdvice++;
	}
}

class ReflectionHelper {
  public static List getCflowfields(Object o,boolean includeCounters,boolean includeStacks) {
  	List res = new ArrayList();
  	Class clazz = o.getClass();
  	Field[] fs = clazz.getDeclaredFields();
  	for (int i = 0; i < fs.length; i++) {
		Field f = fs[i];
		if ((f.getType().getName().endsWith("CFlowCounter") && includeCounters) ||
			(f.getType().getName().endsWith("CFlowStack") && includeStacks)) {
			res.add(f.getType().getName()+":"+f.getName());
		}
	}
  	return res;
  }
  
  public static int howManyCflowCounterFields(Object o) {
    return getCflowfields(o,true,false).size();
  }
  
  public static int howManyCflowStackFields(Object o) {
    return getCflowfields(o,false,true).size();
  }
  
}
