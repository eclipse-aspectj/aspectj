public abstract aspect AbstractTracing {
	
	protected abstract pointcut scope ();
	
	before () : execution(public static void main(String[])) && scope() {
		System.out.println("? " + thisJoinPointStaticPart.getSignature().getName());
	}
}