public aspect Aspect {
	before () : execution(public static void main(String[])) {
		System.out.println(thisJoinPoint);
	}
}