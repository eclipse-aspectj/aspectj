package com.test2;

import com.test2.PolicyProviderConstants.OpenInPolicyValues;

public class PolicyManager {
	public static OpenInPolicyValues getOpenInPolicy (boolean showDialog)
	{
		OpenInPolicyValues value = PolicyProviderInterface.getOpenInPolicy();
		
		return value;
	}
	
	public static void main(String[] args) {
		new PolicyManager().foo();
	}
	
	public String foo() {
		return "";
	}
}