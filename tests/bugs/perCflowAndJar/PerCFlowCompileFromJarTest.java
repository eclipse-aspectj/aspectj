
public class PerCFlowCompileFromJarTest  {

    public static void main(String[] args) throws Exception {
		PerCFlowTestHelper c1 = new PerCFlowTestHelper();
		PerCFlowTestHelper c2 = new PerCFlowTestHelper();
		PerCFlowTestHelper c3 = new PerCFlowTestHelper();
		c1.startNewPerCFlow();
		c2.startNewPerCFlow();
		c3.startNewPerCFlow();
	}
}

class PerCFlowTestHelper {
	public void startNewPerCFlow()throws Exception{
		//do nothing		
	}	
}

aspect MyTestPerCFlowEntryPoint extends PerCFlowCompileFromJar {
	protected pointcut entryPoint():
		execution( public void PerCFlowTestHelper.startNewPerCFlow() );	
}