// Auto-generated

public aspect ExecutionAdviceWeaveMedium {

	before() : args(out.C0) && execution(void m0(..)) {
		System.out.println("In the aspect");
	}
}
