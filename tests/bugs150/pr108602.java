/*
 * GenericsError.java
 *
 * Created on September 1, 2005, 9:36 AM
 *
 */

import java.util.Collection;

interface GenericsError {
  public Collection<String> test(
      boolean arg1,
      boolean arg2,
      Object arg3) ;
}


/*
 * GenericsErrorImpl.java
 *
 * Created on September 1, 2005, 9:37 AM
 *
 */

class GenericsErrorImpl implements GenericsError {

  public Collection<String> test(
      boolean arg1,
      boolean arg2,
      Object arg3)  { return null; }
 
}

aspect ForceUnpacking {
	
	before() : execution(* *(..)) {}
	
}