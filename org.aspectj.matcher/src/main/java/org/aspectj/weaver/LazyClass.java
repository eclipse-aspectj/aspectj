package org.aspectj.weaver;

import java.util.List;

import org.aspectj.weaver.UnwovenClassFile.ChildClass;

// Abstract parent for LazyClassGen initially, should not have any bcel dependencies
// needs a better name, let's see what the methods/state reveal themselves to be to help determine that
public abstract class LazyClass {

	public abstract String getFileName();

	public abstract String getClassName();

	protected abstract List<ChildClass> getChildClasses(World world);

	public abstract boolean isWoven();

	protected abstract void addGeneratedInner(LazyClass interfaceGen);

	protected abstract boolean isInterface();

	protected abstract Clazz getJavaClass(World world);

	protected abstract byte[] getJavaClassBytesIncludingReweavable(World world);

}