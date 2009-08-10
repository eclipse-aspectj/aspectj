
public aspect AnnotatedMethodImpl {

	@PresentAnnotation
	public String AnnotatedMethodInterface.getSomething() {
		return "meth";
	}
	
}
