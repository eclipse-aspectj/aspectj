package org.aspectj.systemtest.ajc150.ataspectj.coverage;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;


public class CoverageTests extends org.aspectj.testing.XMLBasedAjcTestCase {


 public static Test suite() {
   return XMLBasedAjcTestCase.loadSuite(CoverageTests.class);
 }

 protected File getSpecFile() {
   return new File("../tests/src/org/aspectj/systemtest/ajc150/ataspectj/coverage/coverage.xml");
 }


 public void test001(){
   runTest("@Aspect extending Aspect");
 }
 
 public void test002(){
	   runTest("@Aspect with codestyle pointcut");
 }
 
 public void test003(){
	   runTest("Codestyle Aspect with @Pointcut");
}

 public void test004(){
	   runTest("@Pointcut declared on codestyle advice");
}

 public void test005(){
	   runTest("@Aspect class extending @Aspect class");
}
 
 public void test006(){
	   runTest("class with @Before extending @Aspect class");
}
 
 public void test007(){
	   runTest("@Before declared on codestyle advice");
}
 
 public void test008(){
	   runTest("@Pointcut not returning void");
}
 
 public void test009(){
	   runTest("@Pointcut on @Aspect class constructor");
}
 
 public void test010(){
	   runTest("@Aspect on interface");
}
 
 public void test011(){
	   runTest("@Pointcut on non-aspect class method");
}
 
 public void test012(){
	   runTest("@Before on non-aspect class method");
}
 
 public void test013(){
	   runTest("@Pointcut on Interface method");
}
 
 public void test014(){
	   runTest("@Pointcut with garbage string");
}
 
 public void test015(){
	   runTest("@Pointcut with non-empty method body");
}
 
 
 public void test016(){
	   runTest("@Pointcut with throws clause");
}
 
 public void test017(){
	   runTest("@Aspect used badly");
}
 
 public void test018(){
	   runTest("@Before declared on @Aspect class constructor");
}
 
 public void test019(){
	   runTest("@AfterReturning with wrong number of args");
}
 
 public void test020(){
	   runTest("@Before on non-public method");
}
 
 public void test021(){
	   runTest("@Before on method not returning void");
}
 public void test022(){
	   runTest("@Pointcut with wrong number of args");
}
 public void test023(){
	   runTest("@DeclareParents with interface extending interface");
}
 public void test024(){
	   runTest("@DeclareParents implementing more than one interface");
}
 public void test025(){
	   runTest("@DeclareParents used outside of an Aspect");
}
 public void test026(){
	   runTest("@DeclareParents on an @Aspect");
}
 public void test027(){
	   runTest("@DeclareParents on an @Aspect with @DeclarePrecidence");
}
 public void test028(){
	   runTest("@DeclareWarning with a non-final String");
}
 public void test029(){
	   runTest("@DeclareWarning with a static final Object (that is a String)");
}
 public void test030(){
	   runTest("@DeclareWarning with a static final Integer");
}
 public void test031(){
	   runTest("@Around given an extension of ProceedingJoinPoint");
}
 public void test032(){
	   runTest("calling @Before advice explicitly as a method");
}
 public void test033(){
	   runTest("@Before on Interface method");
}
 public void test034(){
	   runTest("@Aspect Aspect double declaration");
}
 public void test035(){
	   runTest("@Before and @After on one method");
}
 public void test036(){
	   runTest("@Before twice on one method");
}
 public void test037(){
	   runTest("@Before advice with empty string");
}
public void test038(){
	   runTest("isPrivileged=truu misspelling");
}

public void test039(){
	   runTest("@Pointcut with an empty string");
}

public void test040(){
	   runTest("@Before with && in string");
}

public void test041(){
	   runTest("@AdviceName given an empty string");
}

public void test042(){
	   runTest("@AdviceName used on @Before advice");
}

public void test043(){
	   runTest("The Moody example");
}

public void test044(){
	   runTest("@DeclareWarning");
}



}
