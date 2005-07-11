public class GenericImplementingClass<N extends Number>
 implements GenericInterface<N> {
	 
	 public int asInt(N aNumber) {
		 return aNumber.intValue();
	 }
	 
 }