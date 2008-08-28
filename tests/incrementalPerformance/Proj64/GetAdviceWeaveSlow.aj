// Auto-generated

public aspect GetAdviceWeaveSlow {

	before() : get(int *) {
		System.out.println("In the aspect");
	}
}
