// todo: need header

import org.aspectj.testing.Tester;

/**
 * "Coverage" tests for 
 * PR#476:
 * Member initializations are run after explicit 
 * constructor calls ("this()" or "super()") when they should be run beforehand.
 * <p>Status: 
 * 10 additional variants are defined, 5 of which fail, emitting 30 errors.
 * <p>background: 
 * <br>The effective order of operation during initialization should be:
 * <ol>
 * <ol>superclass static initialization</ol>
 * <ol>selfclass static initialization</ol>
 * <ol>superclass member initialization</ol>
 * <ol>superclass constructor</ol>
 * <ol>selfclass member initialization</ol>
 * <ol>selfclass constructor</ol>
 * </ol>
 * Other relevant rules:
 * <li>this() or super() if present must be the first statement in a constructor</li>
 * <li>Cannot use this (and hence this.member) in either this() or super()
 *     (checked by javac, not ajc) </li>
 * <li>Cannot refer to parent instance members in either this() or super()
 *     (checked by javac, not ajc) </li>
 * <li>an enclosing instance is accessible only in the body of an instance 
 *     method, constructor (after the explicit constructor invocation, if any), 
 *     initializer block, or in the initializer expression of an instance variable.</li>
 * <p>fault model: 
 *  the compiler is inserting member initialization after the explicit 
 *  constructor call in the intermediate code. I.e., it produces:
 *  <pre>ThisCall() {
 *   this("correctValue");
 *   {
 *     this.initString = "INIT";
 *     this.initNull = null;    
 *   }</pre>
 * when it should produce:
 *  <pre>ThisCall() {
 *   this("correctValue");</pre>
 *
 * <p>fix model: 
 * Since member initialization must occur before this() call, 
 *  and this() must be first in the constructor,
 *  I see no way to implement before advice on member initializers
 *  using preprocessing to produce source code except to put them only
 *  (and always) in the constructors without this() calls.
 *
 * <p>Variants tested in this coverage extension of the original test case:
 * <li>{type}[Object, String, Primitive]: Different member types</li>
 * <li>location[top, bottom, mixed]: location of the member initializer in the class declaration -
 *     before constructor, after constructor</li>
 * <li>initializer[simpleExpression, blockExpression, none]: 
 *     type of member initialization 
 *     (<code>Member m = x;<code> or <code>Member m; { m = x; }<code>) 
 *     with location variants. </li>
 * <li>initializerLocus[this (default), super, enclosing ]: 
 *     fields being initialized - this instance, superclass, enclosing class 
 * <li>{enclosingClass}[none, Outer, ]: Different member types</li>
 *
 * <p>Variants not (yet?) tested: 
 * <li>static variants</li>
 * <li>{super}[{default},Child]: Calling <code>super()</code> rather than <code>this()</code> </li>
 *
 * <p>Untestable variants: 
 * <li>Illegal to use this or super member values in explicit constructor call parameter
 *     evaluation: <code>super("result: " + member)</code>
 *     or <code>this("result: " + member)</code>.
 *     or <code>this("result: " + super.member)</code>.
 *
 * $Id: MemberInitializationsAfterExplicitConstructorCallsCoverage.java,v 1.2 2001/08/03 22:38:49 isberg Exp $
 */
public class MemberInitializationsAfterExplicitConstructorCallsCoverage {
	public static final String INPUT = "input";
	public static final String INIT = "INIT";
	public static void main(String[] args) {
		test();
	}
 
	public static void test() {
		boolean doPassingTests = true;
		boolean doFailingTests = true;
		//--------- proof that test code is correct
		{ ThisCallTopSimple thisCall = new ThisCallTopSimple(1); thisCall.go(); }
		//--------- passing test cases
    //--- this duplicates original test case
		// ThisCall thisCall;
		// no constructor call to this
		// thisCall = new ThisCall(INPUT);
		// thisCall.go();
    //--- new coverage tests - 5 tests, 6 errors each, 30 errors
		if (doPassingTests) {
			{ ThisCallTopSimple thisCall = new ThisCallTopSimple(INPUT); thisCall.go(); }
			{ ThisCallTopBlock thisCall = new ThisCallTopBlock(INPUT);  thisCall.go(); }
			{ ThisCallBottomSimple thisCall = new ThisCallBottomSimple(INPUT); thisCall.go(); }
			{ ThisCallBottomBlock thisCall = new ThisCallBottomBlock(INPUT); thisCall.go(); }
			{ ThisCallMixed thisCall = new ThisCallMixed(INPUT); thisCall.go(); }
			// all super cases pass
			{ ThisCallChild thisCall = new ThisCallChild(); thisCall.go(); }
			{ ThisCallChild thisCall = new ThisCallChild(2); thisCall.go(); }
			{ ThisCallChild thisCall = new ThisCallChild(INPUT); thisCall.go(); }
			// enclosed inner class initializer can access enclosing members
			{ ThisCallEnclosing.ThisCallEnclosed thisCall 
					= (new ThisCallEnclosing("ignored")).new ThisCallEnclosed(); }
		}
		// { ThisCallChild thisCall = new ThisCallChild(); thisCall.go(); }

		//--------- failing test cases
    //--- duplicate original test case
		// fails - constructor call to this
		//thisCall = new ThisCall();
		//thisCall.go();
    //--- new coverage tests
		if (doFailingTests) {
			{ ThisCallTopSimple thisCall = new ThisCallTopSimple(); thisCall.go(); }
			{ ThisCallTopBlock thisCall = new ThisCallTopBlock();  thisCall.go(); }
			{ ThisCallBottomSimple thisCall = new ThisCallBottomSimple(); thisCall.go(); }
			{ ThisCallBottomBlock thisCall = new ThisCallBottomBlock(); thisCall.go(); }
			{ ThisCallMixed thisCall = new ThisCallMixed(); thisCall.go(); }
		}
		
		//--------- impossible test cases
		//---- unable to test superclass initialization before instance
		// { ThisCallChild thisCall = new ThisCallChild((long)1l); thisCall.go(); }
	}
    
	/** variant: location top, initializer simpleExpression */
	static class ThisCallTopSimple {
		/** type primitive, location top, initializer simpleExpression */
		int initOne = 1;
		/** type String, location top, initializer simpleExpression */
		String initString = "INIT";
		/** type Object, location top, initializer simpleExpression */
		Object initNull = null;
		/** type String, location top, initializer none */
		String initNone;

		/** no bug when calling this directly */
		ThisCallTopSimple (String input) { 
			checkMembersHaveInitializedValues("constructor ThisCallTopSimple(\" + input + \")");
			setValues(input);
			checkMembersHaveSetValues("constructor ThisCallTopSimple.ThisCallTopSimple(\" + input + \")");
		}
		void setValues(String input) {
			this.initString = input; 
			this.initNull = input; 
			this.initNone = input; 
			this.initOne = 2; 
		}

		/** proof that test code is correct */
		ThisCallTopSimple (int ignored) { 
			checkMembersHaveInitializedValues("constructor ThisCallTopSimple.ThisCallTopSimple(int)");
			setValues(INPUT); 
			checkMembersHaveSetValues("constructor ThisCallTopSimple.ThisCallTopSimple(int)");
		}

		/** bug when calling this which calls ThisCall(String) */
		ThisCallTopSimple () {  
			this(INPUT); 
			checkMembersHaveSetValues("constructor ThisCallTopSimple.ThisCallTopSimple()");
		}

		/** redundant check - same check at end of constructors */
		void go() { 
			checkMembersHaveSetValues("method ThisCallTopSimple.go()");
		}
		/** the same method for all variants */
		protected void checkMembersHaveInitializedValues(String label) {
			Tester.checkEqual("INIT", initString, label + " initialized ");
			Tester.checkEqual((Object) null, initNull, label + " initialized ");
			Tester.checkEqual((Object) null, initNone, label + " initialized ");
			Tester.checkEqual(1, initOne, label + " initialized ");
		}

		/** the same method for all variants */
		protected void checkMembersHaveSetValues(String label) {
			Tester.checkEqual(2, initOne, label + " set ");
			Tester.checkEqual("input", initString, label + " set ");
			Tester.checkEqual("input", initNone, label + " set ");
			// Object uses strict/reference identity - input dependency
			Tester.checkEqual(INPUT, initNull, label + " set ");
		}
	} // ThisCallTopSimple 
    
	/** variant: location top, initializer blockExpression */
	static class ThisCallTopBlock {
		/** top declarations */
		/** type primitive, location top, initializer blockExpression */
		int initOne;
		/** type String, location top, initializer blockExpression */
		String initString;
		/** type Object, location top, initializer blockExpression */
		Object initNull;
		/** type String, location top, initializer none */
		String initNone;

		/** top initializer block */
		{
			initOne = 1;
			initString = "INIT";
			initNull = null;
		}

		/** no bug when calling this directly */
		ThisCallTopBlock (String input) { 
			checkMembersHaveInitializedValues("constructor ThisCallTopBlock(\" + input + \")");
			this.initString = input; 
			this.initNull = input; 
			this.initNone = input; 
			this.initOne = 2; 
			checkMembersHaveSetValues("constructor ThisCallTopSimple.ThisCall(\" + input + \")");
		}

		/** bug when calling this which calls ThisCallTopBlock(String) */
		ThisCallTopBlock () { 
			this(INPUT); 
			checkMembersHaveSetValues("constructor ThisCallTopSimple.ThisCallTopBlock()");
		}

		/** redundant check - same check at end of constructors */
		void go() { 
			checkMembersHaveSetValues("method ThisCallTopBlock.go()");
		}

		/** the same method for all variants */
		protected void checkMembersHaveInitializedValues(String label) {
			Tester.checkEqual("INIT", initString, label + " initialized ");
			Tester.checkEqual((Object) null, initNull, label + " initialized ");
			Tester.checkEqual((Object) null, initNone, label + " initialized ");
			Tester.checkEqual(1, initOne, label + " initialized ");
		}

		/** the same method for all variants */
		protected void checkMembersHaveSetValues(String label) {
			Tester.checkEqual(2, initOne, label + " set ");
			Tester.checkEqual("input", initString, label + " set ");
			Tester.checkEqual("input", initNone, label + " set ");
			// Object uses strict/reference identity - input dependency
			Tester.checkEqual(INPUT, initNull, label + " set ");
		}
	} // ThisCallTopBlock 
    
	/** variant: location bottom, initializer simpleExpression */
	static class ThisCallBottomSimple {
		/** no bug when calling this directly */
		ThisCallBottomSimple (String input) { 
			checkMembersHaveInitializedValues("constructor ThisCallBottomSimple(\" + input + \")");
			this.initString = input; 
			this.initNull = input; 
			this.initNone = input; 
			this.initOne = 2; 
			checkMembersHaveSetValues("constructor ThisCallBottomSimple.ThisCallBottomSimple(\" + input + \")");
		}

		/** bug when calling this which calls ThisCallBottomSimple(String) */
		ThisCallBottomSimple () { 
			this(INPUT); 
			checkMembersHaveSetValues("constructor ThisCallBottomSimple.ThisCallBottomSimple()");
		}

		/** redundant check - same check at end of constructors */
		void go() { 
			checkMembersHaveSetValues("method ThisCallBottomSimple.go()");
		}

		/** the same method for all variants */
		protected void checkMembersHaveInitializedValues(String label) {
			Tester.checkEqual("INIT", initString, label + " initialized ");
			Tester.checkEqual((Object) null, initNull, label + " initialized ");
			Tester.checkEqual((Object) null, initNone, label + " initialized ");
			Tester.checkEqual(1, initOne, label + " initialized ");
		}

		/** the same method for all variants */
		protected void checkMembersHaveSetValues(String label) {
			Tester.checkEqual(2, initOne, label + " set ");
			Tester.checkEqual("input", initString, label + " set ");
			Tester.checkEqual("input", initNone, label + " set ");
			// Object uses strict/reference identity - input dependency
			Tester.checkEqual(INPUT, initNull, label + " set ");
		}
		/** type primitive, location bottom, initializer simpleExpression */
		int initOne = 1;
		/** type String, location bottom, initializer simpleExpression */
		String initString = "INIT";
		/** type Object, location bottom, initializer simpleExpression */
		Object initNull = null;
		/** type String, location bottom, initializer none */
		String initNone;
	} // ThisCallBottomSimple 
    
	/** variant: location bottom, initializer blockExpression */
	static class ThisCallBottomBlock {
		/** no bug when calling this directly */
		ThisCallBottomBlock (String input) { 
			checkMembersHaveInitializedValues("constructor ThisCallBottomBlock(\" + input + \")");
			this.initString = input; 
			this.initNull = input; 
			this.initNone = input; 
			this.initOne = 2; 
			checkMembersHaveSetValues("constructor ThisCallBottomBlock.ThisCallBottomBlock(\" + input + \")");
		}

		/** bug when calling this which calls ThisCallBottomBlock(String) */
		ThisCallBottomBlock () { 
			this(INPUT); 
			checkMembersHaveSetValues("constructor ThisCallBottemBlock.ThisCallBottomBlock()");
		}

		/** redundant check - same check at end of constructors */
		void go() { 
			checkMembersHaveSetValues("method ThisCallBottomBlock.go()");
		}

		/** the same method for all variants */
		protected void checkMembersHaveInitializedValues(String label) {
			Tester.checkEqual("INIT", initString, label + " initialized ");
			Tester.checkEqual((Object) null, initNull, label + " initialized ");
			Tester.checkEqual((Object) null, initNone, label + " initialized ");
			Tester.checkEqual(1, initOne, label + " initialized ");
		}

		/** the same method for all variants */
		protected void checkMembersHaveSetValues(String label) {
			Tester.checkEqual(2, initOne, label + " set ");
			Tester.checkEqual("input", initString, label + " set ");
			Tester.checkEqual("input", initNone, label + " set ");
			// Object uses strict/reference identity - input dependency
			Tester.checkEqual(INPUT, initNull, label + " set ");
		}
		/** bottom declarations */
		/** type primitive, location bottom, initializer blockExpression */
		int initOne;
		/** type String, location bottom, initializer blockExpression */
		String initString;
		/** type Object, location bottom, initializer blockExpression */
		Object initNull;
		/** type String, location bottom, initializer none */
		String initNone;

		/** bottom initializer block */
		{
			initOne = 1;
			initString = "INIT";
			initNull = null;
		}
	} // ThisCallBottomBlock 

	/** variant: location mixed, initializer mixed */
	static class ThisCallMixed {
		/** type primitive, location top, initializer simpleExpression */
		int initOne = 1;
		/** type String, location top, initializer simpleExpression */
		String initString;

		/** no bug when calling this directly */
		ThisCallMixed (String input) { 
			checkMembersHaveInitializedValues("constructor ThisCallMixed(\" + input + \")");
			this.initString = input; 
			this.initNull = input; 
			this.initNone = input; 
			this.initOne = 2; 
			checkMembersHaveSetValues("constructor ThisCallMixed.ThisCallMixed(\" + input + \")");
		}

		/** bug when calling this which calls ThisCallMixed(String) */
		ThisCallMixed () { 
			this(INPUT); 
			checkMembersHaveSetValues("constructor ThisCallMixed.ThisCallMixed()");
		}

		/** redundant check - same check at end of constructors */
		void go() { 
			checkMembersHaveSetValues("method ThisCallMixed.go()");
		}

		/** the same method for all variants */
		protected void checkMembersHaveInitializedValues(String label) {
			Tester.checkEqual("INIT", initString, label + " initialized ");
			Tester.checkEqual((Object) null, initNull, label + " initialized ");
			Tester.checkEqual((Object) null, initNone, label + " initialized ");
			Tester.checkEqual(1, initOne, label + " initialized ");
		}

		/** the same method for all variants */
		protected void checkMembersHaveSetValues(String label) {
			Tester.checkEqual(2, initOne, label + " set ");
			Tester.checkEqual("input", initString, label + " set ");
			Tester.checkEqual("input", initNone, label + " set ");
			// Object uses strict/reference identity - input dependency
			Tester.checkEqual(INPUT, initNull, label + " set ");
		}
		/** bottom declarations */
		/** type String, location bottom, initializer none */
		String initNone;
		/** type Object, location bottom, initializer blockExpression */
		Object initNull;

		/** bottom (partial) initializer block */
		{
			initString = "INIT";
			initNull = null;
		}
	} // ThisCallMixed 

	static class ThisCallChild extends ThisCallParent {
		/** type primitive, location top, initializer simpleExpression */
		int initOne = 1;
		/** type String, location top, initializer simpleExpression */
		String initString = "INIT";
		/** type Object, location top, initializer simpleExpression */
		Object initNull = null;
		/** type String, location top, initializer none */
		String initNone;

		/** no bug when calling this directly */
		ThisCallChild (String input) { 
			checkMembersHaveInitializedValues("constructor ThisCallChild(\" + input + \")");
			setValues(input);
			checkMembersHaveSetValues("constructor ThisCallChild.ThisCallChild((\" + input + \")");;
			Tester.checkEqual(parentObject, INPUT, "ThisCallChild.ThisCallChild(int ignored)");
		}
		void setValues(String input) {
			this.initString = input; 
			this.initNull = input; 
			this.initNone = input; 
			this.initOne = 2; 
		}

		/** 
		 * @param correctResult 
		 * @param actual
		 * @param expected
		 * @param failedResult
		 * @param testerMessage the String to use for Tester on failure -
		 *        Tester unused if null
		 * @return correctResult if expected.equals(actual), failedResult otherwise 
		 */
		static private String checkObject(String correctResult 
																			, Object expected
																			, Object actual
																			, String failedResult
																			, String testerMessage) {
			if (null == expected) {
				if (null == actual) {
					return correctResult;
				} //  else failures fall through
			} else if ((null != actual) && (expected.equals(actual))) {
				return correctResult;
			}
			// failures
			if (null != testerMessage) {
				Tester.checkEqual(actual, expected, testerMessage);
			}
			return failedResult;
		}

		/** proof that test code is correct */
		ThisCallChild (int ignored) { 
			checkMembersHaveInitializedValues("constructor ThisCallChild.ThisCallChild(int)");
			setValues(INPUT); 
			checkMembersHaveSetValues("constructor ThisCallChild.ThisCallChild(int)");
			Tester.checkEqual(parentObject, INPUT, "ThisCallChild.ThisCallChild(int ignored)");
		}

		/** no bug when calling this which calls ThisCall(String) */
		ThisCallChild () {  
			super(INPUT); 
			checkMembersHaveInitializedValues("constructor ThisCallChild.ThisCallChild()");
			setValues(INPUT);
			checkMembersHaveSetValues("constructor ThisCallChild.ThisCallChild()");
			Tester.checkEqual(parentObject, INPUT, "ThisCallChild.ThisCallChild()");
		}

			private static final String tccsuperlabel = 
				"ThisCallChild.ThisCallChild(long)/* parent initialization complete before child */"; 
		/** unable to access superclass member state before explicitly invoking constructor */
		ThisCallChild (long ignored) {  
			// this would do the check, but it is illegal
			// this(checkObject(INPUT, INPUT, parentObject, tccsuperLabel + "_FAILED", tccsuperlabel));
			// this(checkObject(INPUT, INPUT, this$.getParentObject(), tccsuperlabel + "_FAILED", tccsuperlabel));
			checkMembersHaveInitializedValues("constructor ThisCallChild.ThisCallChild()");
			setValues(INPUT);
			checkMembersHaveSetValues("constructor ThisCallChild.ThisCallChild()");
			Tester.checkEqual(parentObject, INPUT, "ThisCallChild.ThisCallChild()");
		}

		/** redundant check - same check at end of constructors */
		void go() { 
			checkMembersHaveSetValues("method ThisCallChild.go()");
			Tester.checkEqual(parentObject, INPUT, "ThisCallChild.go()");
		}

		/** the same method for all variants */
		protected void checkMembersHaveInitializedValues(String label) {
			Tester.checkEqual("INIT", initString, label + " initialized ");
			Tester.checkEqual((Object) null, initNull, label + " initialized ");
			Tester.checkEqual((Object) null, initNone, label + " initialized ");
			Tester.checkEqual(1, initOne, label + " initialized ");
		}

		/** the same method for all variants */
		protected void checkMembersHaveSetValues(String label) {
			Tester.checkEqual(2, initOne, label + " set ");
			Tester.checkEqual("input", initString, label + " set ");
			Tester.checkEqual("input", initNone, label + " set ");
			// Object uses strict/reference identity - input dependency
			Tester.checkEqual(INPUT, initNull, label + " set ");
		}
	}
	static class ThisCallParent { 
		/** not available to in child explicit constructor parameter expression */
		protected Object parentObject = INIT;
		/** not available to in child explicit constructor parameter expression */
		protected Object getParentObject() { return parentObject; }
		/** no bug here */
		ThisCallParent() {
			Tester.checkEqual(parentObject, INIT, "ThisCallParent.ThisCallParent()");
			parentObject = INPUT;
		}
		/** no bug here */
		ThisCallParent(String input) {
			Tester.checkEqual(parentObject, INIT, "ThisCallParent.ThisCallParent(\"" + input + "\")");
			parentObject = input;
		}
	}	
} // MemberInitializationsAfterExplicitConstructorCallsCoverage 
/** variant: location enclosing */
class ThisCallEnclosing {
	public static final String INPUT = "input";
	public static final String INIT = "INIT";
	String initString = INIT;
	String constructedString;
	public ThisCallEnclosed getEnclosed() {
		return new ThisCallEnclosed();
	}
	/** no bug when calling this directly */
	ThisCallEnclosing (String ignored) { 
		constructedString = INPUT;
		initString = INPUT;
	}

	public class ThisCallEnclosed {
		boolean didCheck;
		{
			// check enclosing instance in initializer
			Tester.checkEqual(INPUT, initString, "ThisCallEnclosed.<initializer> initString");
			Tester.checkEqual(INPUT, constructedString, "ThisCallEnclosed.<initializer> constructedString");
			didCheck = true;
		}
		public ThisCallEnclosed()  {
			this("init: " + initString + " constructed: " + constructedString);
			Tester.check(didCheck, "initializer ran before ThisCallEnclosed() body");
			
		}
		public ThisCallEnclosed(String s)  {
			Tester.checkEqual(INPUT, initString, "ThisCallEnclosed(String) initString");
			Tester.checkEqual(INPUT, constructedString, "ThisCallEnclosed(String) constructedString");
			Tester.check(didCheck, "initializer ran before ThisCallEnclosed(String) body");
		}
	}
} // ThisCallEnclosing 
