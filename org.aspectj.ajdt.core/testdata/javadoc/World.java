// In this class we use all the constructs and attach javadoc to them all - checking
// that the compiler doesnt complain that any javadoc is missing.

/**
 * A comment
 * @see AspectJavadocComment
 */
public aspect World {
    
    public void test0() {}
    
    /**
     * A comment
     * @see PointcutJavadocComment1
     */
	pointcut firstPC() : execution(* *.sayHello(..));
	
    public void test1() {}
    
	/**
	 * A comment
     * @see AfterReturningJavadocComment
	 */
	after() returning : firstPC() {
		System.out.println("world");
	}

    public void test2(){}
    
    /**
     * comment2
     * @see PointcutJavadocComment2
     */   
    public pointcut secondPC(): execution(* *(..));
    
    public void test3(){}
    
    /**
     * I am a comment attached to a warning
     * @see declarewarningJavadocComment
     */
    declare warning: call(* *elephant*(..)) : "I am a warning";
    
    public void test4() {}
    
    /**
     * comment attached to around advice
     * @see AroundAdviceJavadocComment
     */
    void around(): call(* *abc*(..)) {
    }

    public void test5() {}
   
    /**
     * ITD method attached comment
     * @see IntertypeMethodComment
     */
    public void X.method() {       }
    
    public void test6() {}
    
    /**
     * ITD field attached comment
     * @see IntertypeFieldComment
     */
    public int X.i;
    
    public int test7;
    
    static class X {
        
    }
   
}

// to keep the javadoc processor happy ...
class AspectJavadocComment {}
class PointcutJavadocComment1 {}
class PointcutJavadocComment2 {}
class AfterReturningJavadocComment {}
class AroundAdviceJavadocComment {}
class IntertypeMethodComment {}
class IntertypeFieldComment {}