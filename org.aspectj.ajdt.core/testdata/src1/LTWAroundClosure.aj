import java.util.List;

public aspect LTWAroundClosure {

	pointcut println (List list) :
		execution(* println()) && this(list);
	
	void around (final List list) : println (list) {

		Runnable runnable = new Runnable() {
			public void run () {
				System.err.println("LTWAroundClosure.run(" + thisJoinPointStaticPart + ")");
				proceed(list);
			}
		};
		runnable.run();
		list.add("LTWAroundClosure");				
	}

}
