public aspect ExceptionHandler {
	void around() : execution(public void main(String[])) {
		try {
			proceed();
		}
		catch (Exception ex) {
		}
	}
}