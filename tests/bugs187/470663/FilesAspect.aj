package com.safedk.android.aspects;

import org.aspectj.lang.JoinPoint.StaticPart;

public aspect FilesAspect {

	pointcut fileCreateNewFile() : ( call(boolean java.io.File.createNewFile (..)) );

	boolean around()  : fileCreateNewFile() {
			return proceed();
	}
}
