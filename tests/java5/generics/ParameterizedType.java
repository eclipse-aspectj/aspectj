import java.util.*;

/*
 * test case fodder for basic member matching with parameterized types
 */
 public class ParameterizedType<T,S> {
 
 	T aTField;
 	S anSField;
 	
 	T giveMeAT() { return null; }
 	S giveMeAnS() { return null; }
 	
 	S sComesBeforeT(T t) { return null; }
 	
 	void theMadMapper(Map<T,S> aMap) {}
 
    static T convert(S s) { return null; }
 }