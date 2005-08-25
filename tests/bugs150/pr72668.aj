public aspect pr72668 {
	
	Number[] getThoseInts() {
		return new Integer[0];
	}
	
	declare warning : execution(Object[] *(..)) : "should not match";
	
	@org.aspectj.lang.annotation.SuppressAjWarnings("adviceDidNotMatch")
	Object[] around() : execution(*[] *(..)) {
		Object[] ret = proceed();
		return (Object[]) ret.clone();
	}
	
	Integer[] around() : execution(*[] *(..)) {
		Number[] ret = proceed();
		return (Integer[]) ret.clone();
	}
	
}