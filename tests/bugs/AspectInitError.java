

/** Bugzilla Bug 34206  
   before():execution(new(..)) does not throw NoAspectBoundException   */
public class AspectInitError {
	public static void main(String[] args) {
		//Watchcall.aspectOf();
              AspectInitError c = new AspectInitError();
          }

}

aspect Watchcall {
      pointcut myConstructor(): execution(new(..));

  before(): myConstructor() {
          System.err.println("Entering Constructor");
  }

  after(): myConstructor() {
      System.err.println("Leaving Constructor");
  }
}
