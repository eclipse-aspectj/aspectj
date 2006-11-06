public aspect Aspect {

	declare @method : public static void main(String[]) : @Annotation;

	before () : execution(public Class*.new()) {
		System.out.println("? Aspect.before()");
	}
	
}