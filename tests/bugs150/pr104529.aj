import java.util.*;
public class pr104529 {
	
	/*
	 * AJDT is ignoring the @SuppressWarnings( "unchecked" ) annotation.  It is giving 
     * me a type safety warning when I don't specify the type when declaring a generic 
     * even though I have the @SuppressWarnings( "unchecked" ) annotation specified.
	 */
	
	void unsuppressed() {
		List<String> l = new ArrayList();
	}
	
	@SuppressWarnings("unchecked")
	void suppressed() {
		List<Double> l = new ArrayList();
	}
	
}
