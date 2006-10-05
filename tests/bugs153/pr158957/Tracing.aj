public aspect Tracing {
	
	before () : PointcutLibrary.println() {
		System.out.println("? " + thisJoinPointStaticPart.getSignature().getName());
	}
}