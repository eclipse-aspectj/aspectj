import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * In this test, a cflow() pointcut is named and then reused.  It refers to state and so
 * we must manage it with a CFlowStack - can we share the stacks?
 */
public class CounterTest03 {

	public static void main(String []argv) {
		new CounterTest03().sayMessage();
		int ctrs = ReflectionHelper.howManyCflowCounterFields(Cflow1.aspectOf());
		if (ctrs!=0) {
			throw new RuntimeException("Should be zero cflow counters, but found: "+ctrs);
		}
		int stacks = ReflectionHelper.howManyCflowStackFields(Cflow1.aspectOf());
		if (stacks!=1) {
			throw new RuntimeException("Should be one cflow stacks, but found: "+stacks);
		}
	}
	
	public void sayMessage() {
		printmsg("Hello "); printmsg("World\n");
	}
	
	public void printmsg(String msg) {
		System.out.print(msg);
	}
}

aspect Cflow1 {
	
	// CflowCounter created for this pointcut should be shared below!
	pointcut p1(Object o): cflow(execution(* main(..)) && args(o));
	
	before(Object o): call(* print(..)) && p1(o) {
		// Managed by a CflowCounter
	}
	
	before(Object o): call(* print(..)) && p1(o) {
		// Managed by a CflowCounter
	}
	
//	before(Object o): execution(* print(..)) && cflow(execution(* main(..)) && target(o)) {
//		// Managed by a CflowStack - since state is exposed
//	}
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
