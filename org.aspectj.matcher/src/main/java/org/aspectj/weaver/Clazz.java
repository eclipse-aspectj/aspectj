package org.aspectj.weaver;

/**
 * @author Andy Clement
 */
public interface Clazz {

	String getClassName();

	boolean isGeneric();

	String getSourceFileName();

	boolean isJavaLangObject();

}
