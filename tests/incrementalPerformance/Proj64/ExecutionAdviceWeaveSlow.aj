// Auto-generated

public aspect ExecutionAdviceWeaveSlow {

	before() : execution(void *(..)) {
		System.out.println("In the aspect");
	}
}
