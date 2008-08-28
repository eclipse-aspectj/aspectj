// Auto-generated

public aspect ExecutionAdviceWeaveFast {

	before() : within(out.C0) && execution(void m0(..)) {
		System.out.println("In the aspect");
	}
}
