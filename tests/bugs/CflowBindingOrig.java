// for Bugzilla Bug 34858  
//   Weaver crash 

import org.aspectj.testing.Tester;

/**
 * Almost an exact duplicate of the reported issue
 */
public class CflowBindingOrig {
	public static void main(String[] args) {
		new Bar().foo();
	}
	
    static aspect MockProcessing {
        pointcut testFlow(final Thread thread) : 
            cflow(execution(void run()) && this(thread) && within(Thread)); //  the within is an optimization

        Object around() :
                call(* DummyConfiguration.createRootApplicationModule(..)) &&  testFlow(Thread)
        {
            return null;
        }
    }
}

class Bar {
    void foo() {
        DummyConfiguration.createRootApplicationModule();
    }
}

class DummyConfiguration {
    static Object createRootApplicationModule() {
        return null;
    }
}
