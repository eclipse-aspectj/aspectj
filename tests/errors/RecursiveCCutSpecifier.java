package errors;

import org.aspectj.testing.Tester;

/**
 * Test for: PR #95
 */

aspect RecursiveCCutSpecifier { 
  pointcut setFile(LocalFile f): receptions(* *(..)) || setFile(f) && instanceof(f);
  
  /*static*/ before(LocalFile f): setFile(f) {
		// nop
  } 
} 

class LocalFile { 
    public LocalFile() {}
}
