public aspect PointcutLibrary {

	public static pointcut doIt () :
		execution(public void doIt()) && this(Missing);
	
	public static pointcut println () :
		execution(public void println(..));
	
}