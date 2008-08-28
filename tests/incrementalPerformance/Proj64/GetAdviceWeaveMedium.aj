// Auto-generated

public aspect GetAdviceWeaveMedium {

	before() : target(out.C0) && get(int i0) {
		System.out.println("In the aspect");
	}
}
