package the.deep.pkg;

import org.aspectj.testing.Tester;

aspect Aspect pertarget(target(EachObjectTarget)) {
  before(): call(void foo(..)) {
	Tester.check(true, "Dummy test");
  }
}
