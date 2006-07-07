
public aspect Aspect {

	before () : call(public * method(..)) && target(Interface) {
		System.out.println("Aspect.before()");
	}
}
