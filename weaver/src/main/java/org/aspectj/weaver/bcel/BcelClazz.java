package org.aspectj.weaver.bcel;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.weaver.Clazz;

/**
 * Wrap the Bcel {@link JavaClass} so it can be treated as a {@link Clazz}.
 */
public class BcelClazz implements Clazz {
	
	private final JavaClass javaClass;

	public BcelClazz(JavaClass javaClass) {
		this.javaClass = javaClass;
	}

	public static BcelClazz asBcelClazz(JavaClass javaClass) {
		return new BcelClazz(javaClass);
	}
	
	public JavaClass getJavaClass() {
		return javaClass;
	}

	@Override
	public String getClassName() {
		return javaClass.getClassName();
	}

	@Override
	public boolean isGeneric() {
		return javaClass.isGeneric();
	}

	@Override
	public String getSourceFileName() {
		return javaClass.getSourceFileName();
	}

	@Override
	public boolean isJavaLangObject() {
		return javaClass.getSuperclassNameIndex() == 0;
	}
}
