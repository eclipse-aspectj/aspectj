
/** Currently there are no messages emitted for 
 * compile-time errors from advice bodies if the 
 * advice is not woven into the base class.
 * This can lead to silent failures.
 */
class UnwovenAdviceNotCheckedCE {
	public static void main(String[] args) {
		System.err.println("main");
	}
}

aspect Aspect {
  void around (String[] args) 
	  : args(args)
	  && call(void UnwovenAdviceNotCheckedCE.main()) { // forgot (..), so unwoven
	  System.err.println("before main");
	  proceed() ; // CE: should get compile error here - need (args)
	  System.err.println("after main");
  }
}

