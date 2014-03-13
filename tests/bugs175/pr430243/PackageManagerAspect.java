package com.test;

import java.util.ArrayList;
import java.util.List;

import com.test2.PolicyManager;

public aspect PackageManagerAspect {
	pointcut intentquery () : execution(* foo(..));
	private static final String TAG = "PackageManagerAspect";
	
	Object around(): intentquery() {
//		return compute();
		switch (PolicyManager.getOpenInPolicy(false)) {
			case UNRESTRICTED:			break;
			case RESTRICTED:			break;
			case BLOCKED:
			default:			break;
		}
		return "";
	}
	
	public Object compute() {
		switch (PolicyManager.getOpenInPolicy(false)) {
		case UNRESTRICTED:			break;
		case RESTRICTED:			break;
		case BLOCKED:
		default:			break;
		}
		return "";
	}
}
