package com;

import org.aspectj.lang.annotation.*;

@Aspect
public class TestAspect {

	    @DeclareParents(value = "com.TargetClass", defaultImpl =
			    NameManagerImpl.class)
		        private NameManager nameManager;
}

