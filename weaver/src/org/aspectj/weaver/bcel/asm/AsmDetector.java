package org.aspectj.weaver.bcel.asm;

import java.lang.reflect.Method;

/**
 * Determines if a version of asm is around that will enable us to add
 * stack map attributes to classes that we produce.
 * 
 * @author Andy Clement
 */
public class AsmDetector {

	public static boolean isAsmAround;
	
	static {
		try {
			Class reader = Class.forName("org.objectweb.asm.ClassReader");
			Class visitor = Class.forName("org.objectweb.asm.ClassVisitor");
			Method m = reader.getMethod("accept",new Class[]{visitor,Integer.TYPE});
			isAsmAround = m!=null;
		} catch (Exception e ) {
			isAsmAround = false;
		}
		// System.out.println(isAsmAround?"ASM detected":"No ASM found");
	}
}
