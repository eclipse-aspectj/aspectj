package org.aspectj.matcher.tools;

import org.aspectj.matcher.tools.CommonAdvancedPointcutExpressionTests;
import org.aspectj.weaver.World;
import org.aspectj.weaver.reflect.ReflectionWorld;

/**
 * Run all the pointcut parsing/matching tests against a ReflectionWorld.
 * 
 * @author Andy Clement
 */
public class ReflectionWorldAdvancedPointcutExpressionTests extends CommonAdvancedPointcutExpressionTests {

	protected World getWorld() {
		World w = new ReflectionWorld(false, getClass().getClassLoader());
		w.setBehaveInJava5Way(true);
		return w;
	}

}
