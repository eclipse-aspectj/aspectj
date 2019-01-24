package org.aspectj.matcher.tools;

import org.aspectj.weaver.World;
import org.aspectj.weaver.reflect.ReflectionWorld;

/**
 * Run all the pointcut parsing/matching tests against a ReflectionWorld.
 * 
 * @author Andy Clement
 */
public class ReflectionWorldPointcutExpressionTest extends CommonPointcutExpressionTests {

	protected World getWorld() {
		return new ReflectionWorld(true, getClass().getClassLoader());
	}

}
