/*
 * Modified this test case to reflect the fact that types in the default package
 * can only be used in named packages if they are imported.
 * 
 * I believe that according to the 1.1 interpretation of the JLS that this import
 * is also disallowed and there is no way to refer to types in the default package
 * from a named package.
 */

package the.deep.pkg;

import org.aspectj.testing.Tester;
import EachObjectTarget;

aspect Aspect pertarget(target(EachObjectTarget)) {
  before(): call(void foo(..)) {
  	EachObjectTarget t = null;
	Tester.check(true, "Dummy test");
  }
}
