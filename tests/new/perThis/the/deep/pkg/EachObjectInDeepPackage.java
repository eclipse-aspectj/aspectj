package the.deep.pkg;

import org.aspectj.testing.Tester;
import p.EachObjectTarget;

aspect Aspect pertarget(target(EachObjectTarget)) {
  before(): call(void foo(..)) {
  	EachObjectTarget t = null;
	Tester.check(true, "Dummy test");
  }
}
