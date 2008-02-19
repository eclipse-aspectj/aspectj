package mypackage;

import java.util.Collection;
import junit.framework.TestCase;

/**
 * A test case depicting the scenario where a parameterized interface includes a method 
 * that takes a parameterized object. A interface-based pointcut (that does not include
 * '+') fails to select such a method.   
 * 
 * @author Ramnivas Laddad
 *
 */
public class GenericInterfaceWithGenericArgumentPointcutBug extends TestCase {
	private GenericInterface<String> testObject = new GenericImpl<String>();

	public static void main(String[] args) throws Exception {
		GenericInterfaceWithGenericArgumentPointcutBug instance = new GenericInterfaceWithGenericArgumentPointcutBug();
		instance.setUp();
		instance.testGenericInterfaceGenericArgExecution();
		instance.setUp();
		instance.testGenericInterfaceNonGenericArgExecution();
		instance.setUp();
		instance.testgenericInterfaceSubtypeGenericArgExecution();
	}
	
	@Override
	protected void setUp() throws Exception {
		TestAspect.aspectOf().genericInterfaceNonGenericArgExecutionCount = 0;
		TestAspect.aspectOf().genericInterfaceGenericArgExecutionCount = 0;
		TestAspect.aspectOf().genericInterfaceSubtypeGenericArgExecutionCount = 0;
	}

	public void testGenericInterfaceNonGenericArgExecution() {
		testObject.save("");
		assertEquals(1, TestAspect.aspectOf().genericInterfaceNonGenericArgExecutionCount);
	}

	public void testGenericInterfaceGenericArgExecution() {
		testObject.saveAll(null);
		assertEquals(1, TestAspect.aspectOf().genericInterfaceGenericArgExecutionCount);
	}

	public void testgenericInterfaceSubtypeGenericArgExecution() {
		testObject.saveAll(null);
		assertEquals(1, TestAspect.aspectOf().genericInterfaceSubtypeGenericArgExecutionCount);
	}

	static interface GenericInterface<T> {
		public void save(T bean);
		public void saveAll(Collection<T> beans);
	}
	
	static class GenericImpl<T> implements GenericInterface<T> {
		public void save(T bean) {}
		public void saveAll(Collection<T> beans) {}
	}

	static aspect TestAspect {
		int genericInterfaceNonGenericArgExecutionCount;
		int genericInterfaceGenericArgExecutionCount;
		int genericInterfaceSubtypeGenericArgExecutionCount;
		
		pointcut genericInterfaceNonGenericArgExecution() 
			: execution(* GenericInterface.save(..));

		pointcut genericInterfaceGenericArgExecution() 
			: execution(* GenericInterface.saveAll(..));

		pointcut genericInterfaceSubtypeGenericArgExecution() 
			: execution(* GenericInterface+.saveAll(..));
		
		before() : genericInterfaceNonGenericArgExecution() {
			genericInterfaceNonGenericArgExecutionCount++;
		}

		before() : genericInterfaceGenericArgExecution() {
			genericInterfaceGenericArgExecutionCount++;
		}

		before() : genericInterfaceSubtypeGenericArgExecution() {
			genericInterfaceSubtypeGenericArgExecutionCount++;
		}
	}
}



