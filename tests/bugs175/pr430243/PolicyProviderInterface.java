package com.test2;

import com.test2.PolicyProviderConstants.OpenInPolicyValues;

//import android.content.ContentResolver;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.Binder;
//import android.text.TextUtils;
//import android.util.Log;


// Interface to calls to ManagedAppInfoProvider. this queries the policies and exposes interfaces for checks
public class PolicyProviderInterface
{
	private static final String TAG = "PolicyProviderInterface";

	private static final String STR_SECURITY_GROUP = "SecurityGroup";
	private static final String STR_BLOCKED = "blocked";
	private static final String VALUE_SECUREBROWSE = "SecureBrowse";
	
//	private static final String STR_PROVIDER_URI = PolicyProviderColumns.AUTHORITY + "." + PolicyManager.getPackageName() + "/" + PolicyProviderColumns.POLICY_INFO;
//	private static final Uri 	providerURI = Uri.parse("content://"+ STR_PROVIDER_URI);


	
	public static OpenInPolicyValues getOpenInPolicy()
	{
		return OpenInPolicyValues.RESTRICTED;
	}
	
}
