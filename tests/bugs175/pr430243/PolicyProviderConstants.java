package com.test2;

public interface PolicyProviderConstants 
{
	public static final String featureSMS = "Sms";
	public static final String featureCamera = "Camera";
	public static final String featureAudio = "Microphone";
	public static final String featureLocation = "Location";
	public static final String featureSensor = "Sensor";
	public static final String featureSnapshot = "ScreenCapture";
	public static final String featureNFC = "nfc";
	
	public static final String featureClipboard = "CutAndCopy";
	public static final String featureOpenIn = "DocumentExchange";
	
	public static final String policySecurityGroup = "SecurityGroup";
	
	public static final String featureNetworkAccess = "NetworkAccess";

	public static final String featureAuthSupport = "AuthSupport";
	public static final String featureNETWORKACCESSMODE = "PreferredVpnMode";
	public static final String policyOpenInExclusionList = "OpenInExclusionList";
	
	public enum CutCopyPolicyValues
	{
		BLOCKED,
		RESTRICTED,
		UNRESTRICTED,
	};

	public enum OpenInPolicyValues
	{
		BLOCKED,
		RESTRICTED,
		UNRESTRICTED,
	};
	
}
