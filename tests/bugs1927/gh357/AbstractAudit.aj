// Not public: concrete aspects extending it inherit a public method from a non-public class
abstract aspect AbstractAudit {

	protected abstract pointcut audited();

	private Object interceptor;

	before(): audited() {
		if (interceptor != null) {
			System.out.println("audited");
		}
	}

	public void setInterceptor(Object interceptor) {
		this.interceptor = interceptor;
	}
}
