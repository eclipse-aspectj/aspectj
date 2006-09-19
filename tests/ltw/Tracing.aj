public aspect Tracing {
	
	before () : execution(public static void main(String[])) {
		System.out.println("? " + thisJoinPointStaticPart.getSignature().getName());
	}
}