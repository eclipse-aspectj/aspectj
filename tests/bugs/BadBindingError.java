// Bugzilla Bug 30663  
//lame error message: "negation doesn't allow binding" 

import org.aspectj.testing.Tester;

public aspect BadBindingError {
  pointcut p(int i): call(void f(i));
}
